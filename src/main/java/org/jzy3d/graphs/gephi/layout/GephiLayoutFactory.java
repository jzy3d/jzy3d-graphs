package org.jzy3d.graphs.gephi.layout;

import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.forceAtlas23d.ForceAtlas23d;
import org.gephi.layout.plugin.forceAtlas23d.ForceAtlas2Builder;
import org.gephi.layout.plugin.forceAtlas3d.ForceAtlasLayout3d;
import org.gephi.layout.plugin.openord.OpenOrdLayout;
import org.gephi.layout.plugin.openord.OpenOrdLayoutBuilder;
import org.gephi.layout.plugin.openord3d.OpenOrd3dLayout;
import org.gephi.layout.plugin.openord3d.OpenOrd3dLayoutBuilder;
import org.gephi.layout.spi.Layout;

public class GephiLayoutFactory {
    public static ForceAtlas23d createForceAtlas2_3d(GraphModel g) {
        ForceAtlas23d layout= new ForceAtlas23d(new ForceAtlas2Builder());
        layout.resetPropertiesValues();
        layout.setGraphModel(g);
        layout.setAdjustSizes(true);
        return layout;
    }

    public static Layout createForceAtlas1_3d(GraphModel g, double repulsion) {
        ForceAtlasLayout3d layout = new ForceAtlasLayout3d(null);
        layout.resetPropertiesValues();
        layout.setGraphModel(g);
        layout.setAdjustSizes(true);
        layout.setRepulsionStrength(repulsion);
        return layout;
    }

    public static Layout createForceAtlas1_2d(GraphModel graph, double repulsion){
        ForceAtlasLayout layout = new ForceAtlasLayout(null);
        layout.setGraphModel(graph);
        layout.setAdjustSizes(true);
        layout.setRepulsionStrength(repulsion);
        return layout;
    }

    public static Layout createOpenOrd3d(GraphModel g) {
        OpenOrd3dLayout layout = new OpenOrd3dLayout(new OpenOrd3dLayoutBuilder());
        layout.resetPropertiesValues();
        layout.setGraphModel(g);
        return layout;
    }

    public static Layout createOpenOrd(GraphModel g) {
        OpenOrdLayout layout = new OpenOrdLayout(new OpenOrdLayoutBuilder());
        layout.resetPropertiesValues();
        layout.setGraphModel(g);
        //layout.set
        return layout;
    }
}

