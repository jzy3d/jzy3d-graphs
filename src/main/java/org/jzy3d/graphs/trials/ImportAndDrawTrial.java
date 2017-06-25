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
