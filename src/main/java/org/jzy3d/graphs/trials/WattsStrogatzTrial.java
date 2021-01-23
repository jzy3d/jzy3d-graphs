package org.jzy3d.graphs.trials;
/*
 * Copyright 2008-2010 Gephi Authors : Mathieu Bastian <mathieu.bastian@gephi.org> Website :
 * http://www.gephi.org
 * 
 * This file is part of Gephi.
 * 
 * Gephi is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Gephi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with Gephi. If
 * not, see <http://www.gnu.org/licenses/>.
 */



import java.io.File;

import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.gephi.project.api.Workspace;
import org.jzy3d.graphs.gephi.layout.GephiLayoutFactory;
import org.jzy3d.graphs.gephi.layout.GephiLayoutRunner;
import org.jzy3d.graphs.gephi.renderer.GraphRenderer;
import org.jzy3d.graphs.gephi.renderer.GraphRendererSettings;
import org.jzy3d.graphs.gephi.workspace.GephiController;
import org.jzy3d.graphs.gephi.workspace.GephiGraphGenerator;
import org.jzy3d.plot3d.rendering.canvas.Quality;

// TODO : fixme (GephiGraphGenerator.wattsStrogatz)
public class WattsStrogatzTrial extends GephiController {
  public static int LAYOUT_STEPS = 1000;

  public static void main(String[] args) {
    System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

    // controller
    GephiController controller = new GephiController();
    Workspace w = controller.init();

    // generate
    GephiGraphGenerator generator = new GephiGraphGenerator();
    // generator.wattsStrogatz(w, 200, 15, 0.05);
    // generator.wattsStrogatz(w, 300, 100, 0.05);
    // controller.
    controller.save(new File("./data/watts.graphml").getAbsolutePath());

    // layout
    GraphModel g = controller.getGraph();
    controller.randomizeGraphLayout(g);
    controller.sizeWithNodeNeighbourCount(g);

    double repulsion = 10000.0;
    Layout layout = GephiLayoutFactory.createForceAtlas2_3d(g);

    // renderer
    GraphRendererSettings settings = new GraphRendererSettings();
    settings.setNodeLabelDisplayed(true);
    settings.setNodeSphereDisplayed(true);
    settings.setNodePointDisplayed(false);
    GraphRenderer representation =
        GraphRenderer.create(g, settings, Quality.Advanced, "awt", "chart");
    representation.getChart().setAxeDisplayed(false);
    representation.openChart();

    // runner
    GephiLayoutRunner runner = new GephiLayoutRunner();
    runner.run(layout, LAYOUT_STEPS, representation);
  }
}
