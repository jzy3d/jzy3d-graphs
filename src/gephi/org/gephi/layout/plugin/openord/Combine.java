/*
Copyright 2007 Sandia Corporation. Under the terms of Contract
DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government retains
certain rights in this software.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
 * Neither the name of Sandia National Laboratories nor the names of
its contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gephi.layout.plugin.openord;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.gephi.graph.api.HierarchicalGraph;

/**
 *
 * @author Mathieu Bastian
 */
public class Combine implements Runnable {

    private final OpenOrdLayout layout;
    private final Object lock = new Object();
    private final Control control;

    public Combine(OpenOrdLayout layout) {
        this.layout = layout;
        this.control = layout.getControl();
    }

    public void run() {
        //System.out.println("Combine results");

        Worker[] workers = layout.getWorkers();

        //Gather positions
        Node[] positions = null;
        for (Worker w : workers) {
            if (positions == null) {
                positions = w.getPositions();
            } else {
                Node[] workerPositions = w.getPositions();
                for (int i = w.getId(); i < positions.length; i += workers.length) {
                    positions[i] = workerPositions[i];
                }
            }
        }

        //Unfix positions if necessary
        if (!control.isRealFixed()) {
            for (Node n : positions) {
                n.fixed = false;
            }
        }

        //Combine density
        for (Worker w : workers) {
            DensityGrid densityGrid = w.getDensityGrid();
            boolean fineDensity = w.isFineDensity();
            boolean firstAdd = w.isFirstAdd();
            boolean fineFirstAdd = w.isFineFirstAdd();
            Node[] wNodes = w.getPositions();
            for (Worker z : workers) {
                if (w != z) {
                    Node[] zNodes = w.getPositions();
                    for (int i = z.getId(); i < wNodes.length; i += workers.length) {
                        densityGrid.substract(wNodes[i], firstAdd, fineFirstAdd, fineDensity);
                        densityGrid.add(zNodes[i], fineDensity);
                    }
                }
            }
        }

        //Redistribute positions to workers
        if (workers.length > 1) {
            for (Worker w : workers) {
                Node[] positionsCopy = new Node[positions.length];
                for (int i = 0; i < positions.length; i++) {
                    positionsCopy[i] = positions[i].clone();
                }
                w.setPositions(positionsCopy);
            }
        }

        float totEnergy = getTotEnergy();
        boolean done = !control.udpateStage(totEnergy);

        //Params
        for (Worker w : layout.getWorkers()) {
            control.initWorker(w);
        }

        //Write positions to nodes
        HierarchicalGraph graph = layout.getGraph();
        for (org.gephi.graph.api.Node n : graph.getNodes()) {
            if (n.getNodeData().getLayoutData() != null && n.getNodeData().getLayoutData() instanceof OpenOrdLayoutData) {
                OpenOrdLayoutData layoutData = n.getNodeData().getLayoutData();
                Node node = positions[layoutData.nodeId];
                n.getNodeData().setX(node.x * 10f);
                n.getNodeData().setY(node.y * 10f);
            }
        }

        //Finish
        if (!layout.canAlgo() || done) {
            for (Worker w : layout.getWorkers()) {
                w.setDone(true);
            }
            layout.setRunning(false);
        }

        //Synchronize with layout goAlgo()
        synchronized (lock) {
            lock.notify();
        }
    }

    private void printPositions(Node[] nodes) {
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMaximumFractionDigits(2);
        for (int i = 0; i < nodes.length; i++) {
            String xStr = formatter.format((double) nodes[i].x);
            String yStr = formatter.format((double) nodes[i].y);
            System.out.print("(" + xStr + "-" + yStr + "),");
        }
        System.out.println();
    }

    public float getTotEnergy() {
        float totEnergy = 0;
        for (Worker w : layout.getWorkers()) {
            totEnergy += w.getTotEnergy();
        }
        return totEnergy;
    }

    public void waitForIteration() {
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
