/*
SPDX-License-Identifier: BSD-3-Clause
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
package org.gephi.layout.plugin.openord3d;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntDoubleIterator;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author Mathieu Bastian
 */
public class Worker3d implements Runnable {

    //Thread
    private final int id;
    private final int numThreads;
    private final CyclicBarrier barrier;
    private boolean done = false;
    //Data
    private Node3d[] positions;
    private TIntDoubleHashMap[] neighbors;
    private DensityGrid3d densityGrid;
    private boolean firstAdd = true;
    private boolean fineFirstAdd = true;
    //Settings
    private float attraction;
    private int STAGE;
    private float temperature;
    private float dampingMult;
    private float minEdges;
    private float cutEnd;
    private float cutOffLength;
    private boolean fineDensity;
    protected Random random;

    public Worker3d(int id, int numThreads, CyclicBarrier barrier) {
        this.barrier = barrier;
        this.id = id;
        this.numThreads = numThreads;
        this.densityGrid = new DensityGrid3d();
        this.densityGrid.init();
    }

    @Override
    public void run() {
        while (!isDone()) {
            //System.out.println("Execute worker " + id);

            //Updates nodes
            for (int i = id; i < positions.length; i += numThreads) {
                updateNodePos(i);
            }

            //Execute one more random if other threads manage one more node
            if (positions.length % numThreads != 0 && id > positions.length % numThreads - 1) {
                getNextRandom();
                getNextRandom();
            }

            firstAdd = false;
            if (fineDensity) {
                fineFirstAdd = false;
            }

            try {
                barrier.await();
            } catch (InterruptedException ex) {
                return;
            } catch (BrokenBarrierException ex) {
                return;
            }
        }
    }

    private void updateNodePos(int nodeIndex) {
        Node3d n = positions[nodeIndex];
        if (n.fixed) {
            getNextRandom();
            getNextRandom();
            return;
        }

        float[] energies = new float[2];
        float[][] updatedPos = new float[2][3];
        float jumpLength = 0.01f * temperature;
        densityGrid.substract(n, firstAdd, fineFirstAdd, fineDensity);

        energies[0] = getNodeEnergy(nodeIndex);
        solveAnalytic(nodeIndex);
        updatedPos[0][0] = n.x;
        updatedPos[0][1] = n.y;
        updatedPos[0][2] = n.z;

        updatedPos[1][0] = updatedPos[0][0] + (.5f - getNextRandom()) * jumpLength;
        updatedPos[1][1] = updatedPos[0][1] + (.5f - getNextRandom()) * jumpLength;
        updatedPos[1][2] = updatedPos[0][2] + (.5f - getNextRandom()) * jumpLength;

        n.x = updatedPos[1][0];
        n.y = updatedPos[1][1];
        n.z = updatedPos[1][2];
        energies[1] = getNodeEnergy(nodeIndex);

        if (energies[0] < energies[1]) {
            n.x = updatedPos[0][0];
            n.y = updatedPos[0][1];
            n.z = updatedPos[0][2];
            n.energy = energies[0];
        } else {
            n.x = updatedPos[1][0];
            n.y = updatedPos[1][1];
            n.z = updatedPos[1][2];
            n.energy = energies[1];
        }

        densityGrid.add(n, fineDensity);
    }

    private float getNodeEnergy(int nodeIndex) {
        double attraction_factor = attraction * attraction
                * attraction * attraction * 2e-2;

        float xDis, yDis, zDis;
        float energyDistance;
        float nodeEnergy = 0;

        Node3d n = positions[nodeIndex];

        if (neighbors[nodeIndex] != null) {
            for (TIntDoubleIterator itr = neighbors[nodeIndex].iterator(); itr.hasNext();) {
                itr.advance();
                double weight = itr.value();
                Node3d m = positions[itr.key()];

                xDis = n.x - m.x;
                yDis = n.y - m.y;
                zDis = n.z - m.z;

                energyDistance = xDis * xDis + yDis * yDis + zDis * zDis;
                if (STAGE < 2) {
                    energyDistance *= energyDistance;
                }

                if (STAGE == 0) {
                    energyDistance *= energyDistance;
                }

                nodeEnergy += weight * attraction_factor * energyDistance;
            }
        }

        nodeEnergy += densityGrid.getDensity(n.x, n.y, n.z, fineDensity);

        return nodeEnergy;
    }

    private void solveAnalytic(int nodeIndex) {
        float totalWeight = 0;
        float xDis, yDis, zDis, xCen = 0, yCen = 0, zCen = 0;
        float x = 0, y = 0, z = 0;
        float damping;


        TIntDoubleHashMap map = neighbors[nodeIndex];
        if (map != null) {
            Node3d n = positions[nodeIndex];

            for (TIntDoubleIterator itr = map.iterator(); itr.hasNext();) {
                itr.advance();
                double weight = itr.value();
                Node3d m = positions[itr.key()];

                totalWeight += weight;
                x += weight * m.x;
                y += weight * m.y;
                z += weight * m.z;
            }


            if (totalWeight > 0) {
                xCen = x / totalWeight;
                yCen = y / totalWeight;
                zCen = z / totalWeight;
                damping = 1f - dampingMult;
                float posX = damping * n.x + (1f - damping) * xCen;
                float posY = damping * n.y + (1f - damping) * yCen;
                float posZ = damping * n.z + (1f - damping) * zCen;
                n.x = posX;
                n.y = posY;
                n.z = posZ;
            }

            if (minEdges == 99) {
                return;
            }
            if (cutEnd >= 39500) {
                return;
            }

            float maxLength = 0;
            int maxIndex = -1;
            int neighborsCount = map.size();
            if (neighborsCount >= minEdges) {
                for (TIntDoubleIterator itr = neighbors[nodeIndex].iterator(); itr.hasNext();) {
                    itr.advance();
                    Node3d m = positions[itr.key()];

                    xDis = xCen - m.x;
                    yDis = yCen - m.y;
                    zDis = zCen - m.z;
                    float dis = xDis * xDis + yDis * yDis + zDis * zDis;
                    dis *= Math.sqrt(neighborsCount);
                    if (dis > maxLength) {
                        maxLength = dis;
                        maxIndex = itr.key();
                    }
                }
            }

            if (maxLength > cutOffLength && maxIndex != -1) {
                map.remove(maxIndex);
            }
        }
    }

    public float getTotEnergy() {
        float myTotEnergy = 0;
        for (int i = id; i < positions.length; i += numThreads) {
            myTotEnergy += positions[i].energy;
        }
        return myTotEnergy;
    }

    public float getNextRandom() {
        float rand = 0;
        for (int i = 0; i < numThreads; i++) {
            if (i == id) {
                rand = random.nextFloat();
            } else {
                random.nextFloat(); //For other threads
            }
        }
        return rand;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setPositions(Node3d[] positions) {
        this.positions = positions;
    }

    public void setNeighbors(TIntDoubleHashMap[] neighbors) {
        this.neighbors = neighbors;
    }

    public Node3d[] getPositions() {
        return positions;
    }

    public boolean isFineDensity() {
        return fineDensity;
    }

    public boolean isFineFirstAdd() {
        return fineFirstAdd;
    }

    public boolean isFirstAdd() {
        return firstAdd;
    }

    public DensityGrid3d getDensityGrid() {
        return densityGrid;
    }

    public TIntDoubleHashMap[] getNeighbors() {
        return neighbors;
    }

    public void setSTAGE(int STAGE) {
        this.STAGE = STAGE;
    }

    public void setAttraction(float attraction) {
        this.attraction = attraction;
    }

    public void setCutOffLength(float cutOffLength) {
        this.cutOffLength = cutOffLength;
    }

    public void setDampingMult(float dampingMult) {
        this.dampingMult = dampingMult;
    }

    public void setMinEdges(float minEdges) {
        this.minEdges = minEdges;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public void setFineDensity(boolean fineDensity) {
        this.fineDensity = fineDensity;
    }

    public void setDensityGrid(DensityGrid3d densityGrid) {
        this.densityGrid = densityGrid;
    }

    public int getId() {
        return id;
    }
}

