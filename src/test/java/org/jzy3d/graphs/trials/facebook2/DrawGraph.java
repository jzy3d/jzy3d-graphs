package org.jzy3d.graphs.trials.facebook2;


import org.gephi.layout.spi.Layout;
import org.jzy3d.chart.Chart;
import org.jzy3d.graphs.gephi.layout.GephiLayoutFactory;
import org.jzy3d.graphs.gephi.layout.GephiLayoutRunner;
import org.jzy3d.graphs.gephi.renderer.GraphColorMapper;
import org.jzy3d.graphs.gephi.renderer.GraphRenderer;
import org.jzy3d.graphs.gephi.renderer.GraphRendererSettings;
import org.jzy3d.graphs.gephi.workspace.GephiController;
import org.jzy3d.plot3d.rendering.canvas.Quality;

/**
 * Draws the Kaggle Facebook challenge graph #1
 * Some satellite nodes have been manually removed with Gephi to focus
 * on the core graph.
 *
 * @see ConvertToGraphML to convert other challenge files
 *
 * @author Martin
 */
public class DrawGraph extends GephiController{
    static Chart c;
    public static int LAYOUT_STEPS = 5000;
    public static void main(String[] args){
        GephiController gephi = new GephiController();
        gephi.init();
        //gephi.importGraph("data/competition/train1-no0.graphml");
        gephi.importGraph("data/competition/reduced.graphml");
        gephi.printInfo();

        // layout
        Layout layout = GephiLayoutFactory.createOpenOrd3d(gephi.getGraph());

        // renderer
        GraphColorMapper graphColorMapper = new GraphColorMapper();

        GraphRendererSettings settings = new GraphRendererSettings();
        settings.setNodeLabelDisplayed(false);
        settings.setNodeSphereDisplayed(false);
        settings.setNodePointDisplayed(false);
        settings.setEdgeColor(null);
        settings.setGraphColorMapper(graphColorMapper);

        GraphRenderer renderer = GraphRenderer.create(gephi.getGraph(), settings, Quality.Intermediate, "awt", "chart");
        graphColorMapper.setView(renderer.getChart().getView());
        graphColorMapper.setGraph(renderer.getChart().getScene().getGraph());

        renderer.setLayoutStepRatio(1);
        renderer.getChart().getView().setAxeBoxDisplayed(false);
        renderer.openChart();

        GephiLayoutRunner runner = new GephiLayoutRunner();
        runner.run(layout, LAYOUT_STEPS, renderer);
    }
}
