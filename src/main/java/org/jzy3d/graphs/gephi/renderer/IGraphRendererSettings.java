package org.jzy3d.graphs.gephi.renderer;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.jzy3d.colors.Color;

public interface IGraphRendererSettings {
    public Color getNodeColor(Node n);
    public Color getNodeWireframeColor(Node n);
    public Color getNodeLabelColor(Node n);
    public Color getNodePointColor(Node n);

    public Color getEdgeColorSource(Edge e);
    public Color getEdgeColorTarget(Edge e);

    public boolean isNodeLabelDisplayed();
    public boolean isNodePointDisplayed();
    public void setNodeLabelDisplayed(boolean status);
    public void setNodePointDisplayed(boolean status);

    public void setSelected(Node n, boolean value);
    public boolean isSelected(Node n);
    public void resetSelection();


}

