/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) Since 2017, Martin Pernollet
 * All rights reserved.
 *
 * Redistribution in binary form, with or without modification, is permitted.
 * Edition of source files is allowed.
 * Redistribution of original or modified source files is FORBIDDEN.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jzy3d.graphs.trials;

import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.jzy3d.chart.Chart;
import org.jzy3d.graphs.gephi.layout.GephiLayoutFactory;
import org.jzy3d.graphs.gephi.layout.GephiLayoutRunner;
import org.jzy3d.graphs.gephi.renderer.GraphRenderer;
import org.jzy3d.graphs.gephi.renderer.GraphRendererSettings;
import org.jzy3d.graphs.gephi.workspace.GephiController;
import org.jzy3d.plot3d.rendering.canvas.Quality;


public class ImportAndDrawTrial extends GephiController{
    static Chart c;
    public static int LAYOUT_STEPS = 5000;
    public static void main(String[] args){
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        // workspace
        GephiController controller = new GephiController();
        controller.init();

        // graph
        //controller.importGraph("data/airlines.graphml");
        controller.importGraph("data/LesMiserables.gexf");
        //controller.importGraph("data/devices.graphml");
        //controller.importGraph("data/Java.gexf");

        // layout
        GraphModel g = controller.getGraph();
        controller.randomizeGraphLayout(g);
        //controller.sizeWithNodeNeighbourCount(g);

        double repulsion = 1000.0;
        //Layout layout = GephiLayoutFactory.createForceAtlas1_2d(g, repulsion);
        Layout layout = GephiLayoutFactory.createForceAtlas1_3d(g, repulsion);
        //Layout layout = GephiLayoutFactory.createForceAtlas2_3d(g);
        //Layout layout = GephiLayoutFactory.createOpenOrd3d(g);

        // renderer
        GraphRendererSettings settings = new GraphRendererSettings();
        settings.setNodeLabelDisplayed(true);
        settings.setNodeSphereDisplayed(true);
        settings.setNodePointDisplayed(false);
        //settings.setNodeWireColor(settings.getNodeColor(null));

        GraphRenderer renderer = GraphRenderer.create(g, settings, Quality.Advanced, "awt", "chart");
        renderer.setLayoutStepRatio(1);
        renderer.getChart().setAxeDisplayed(true);
        renderer.openChart();

        GephiLayoutRunner runner = new GephiLayoutRunner();
        runner.run(layout, LAYOUT_STEPS, renderer);
    }
}

