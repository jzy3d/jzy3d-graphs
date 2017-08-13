package org.jzy3d.graphs.gephi.renderer;

import java.util.HashMap;
import java.util.Map;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;

public class GraphRendererSettings implements IGraphRendererSettings{
    protected boolean nodeSphereDisplayed = false;
    protected boolean nodeLabelDisplayed = false;
    protected boolean nodePointDisplayed = true;

    protected Color edgeColor = new Color(.5f,.5f,.5f,.1f);
    protected Color nodePointColor = new Color(.0f, 1f, .0f, 0.6f);
    protected Color nodeFaceColor = Color.BLUE;
    protected Color nodeWireColor = new Color(.0f,.0f,.0f,.1f);
    protected Color nodeLabelColor = Color.BLACK;

    protected GraphColorMapper nodeColorMapper = null;
    protected ColorMapper edgeColorMapper = null;

    protected Color nodeFaceSelectedColor = Color.RED;
    protected Color nodeWireSelectedColor = new Color(1.0f,.0f,.0f,.1f);
    protected Color nodeLabelSelectedColor = Color.RED;

    protected float radiusMultiplier = 1;

    public GraphRendererSettings() {
        edgeColor.alphaSelf(0.1f);
    }

    public GraphColorMapper getNodeColorMapper() {
		return nodeColorMapper;
	}

	public void setGraphColorMapper(GraphColorMapper nodeColorMapper) {
		this.nodeColorMapper = nodeColorMapper;
	}

	public ColorMapper getEdgeColorMapper() {
		return edgeColorMapper;
	}



	public void setEdgeColorMapper(ColorMapper edgeColorMapper) {
		this.edgeColorMapper = edgeColorMapper;
	}



	public Color getNodeFaceSelectedColor() {
		return nodeFaceSelectedColor;
	}



	public void setNodeFaceSelectedColor(Color nodeFaceSelectedColor) {
		this.nodeFaceSelectedColor = nodeFaceSelectedColor;
	}



	public Color getNodeWireSelectedColor() {
		return nodeWireSelectedColor;
	}



	public void setNodeWireSelectedColor(Color nodeWireSelectedColor) {
		this.nodeWireSelectedColor = nodeWireSelectedColor;
	}



	public Color getNodeLabelSelectedColor() {
		return nodeLabelSelectedColor;
	}



	public void setNodeLabelSelectedColor(Color nodeLabelSelectedColor) {
		this.nodeLabelSelectedColor = nodeLabelSelectedColor;
	}



	public Map<Node, Boolean> getSelection() {
		return selection;
	}



	public void setSelection(Map<Node, Boolean> selection) {
		this.selection = selection;
	}



	public Color getEdgeColor() {
		return edgeColor;
	}



	public Color getNodePointColor() {
		return nodePointColor;
	}



	public Color getNodeFaceColor() {
		return nodeFaceColor;
	}



	public Color getNodeWireColor() {
		return nodeWireColor;
	}



	public Color getNodeLabelColor() {
		return nodeLabelColor;
	}



	public boolean isNodeSphereDisplayed() {
        return nodeSphereDisplayed;
    }

    public void setNodeSphereDisplayed(boolean nodeSphereDisplayed) {
        this.nodeSphereDisplayed = nodeSphereDisplayed;
    }

    @Override
    public void setNodeLabelDisplayed(boolean nodeLabelDisplayed) {
        this.nodeLabelDisplayed = nodeLabelDisplayed;
    }

    @Override
    public void setNodePointDisplayed(boolean nodePointDisplayed) {
        this.nodePointDisplayed = nodePointDisplayed;
    }

    public void setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
    }

    public void setNodePointColor(Color nodePointColor) {
        this.nodePointColor = nodePointColor;
    }

    public void setNodeFaceColor(Color nodeFaceColor) {
        this.nodeFaceColor = nodeFaceColor;
    }

    public void setNodeWireColor(Color nodeWireColor) {
        this.nodeWireColor = nodeWireColor;
    }

    public void setNodeLabelColor(Color nodeLabelColor) {
        this.nodeLabelColor = nodeLabelColor;
    }

    @Override
    public boolean isNodeLabelDisplayed(){
        return nodeLabelDisplayed;
    }
    @Override
    public boolean isNodePointDisplayed(){
        return nodePointDisplayed;
    }

    public float getRadiusMultiplier() {
        return radiusMultiplier;
    }

    public void setRadiusMultiplier(float radiusMultiplier) {
        this.radiusMultiplier = radiusMultiplier;
    }

    @Override
    public Color getNodeColor(Node n) {
        if(isSelected(n))
            return nodeFaceSelectedColor;
        else
            return nodeFaceColor;
    }

    @Override
    public Color getNodeWireframeColor(Node n) {
        if(isSelected(n))
            return nodeWireSelectedColor;
        else
            return nodeWireColor;
    }

    @Override
    public Color getNodePointColor(Node n) {
        return nodePointColor;
    }

    @Override
    public Color getNodeLabelColor(Node n) {
        return nodeLabelColor;
    }

    @Override
    public Color getEdgeColorSource(Edge e) {
        return edgeColor;
    }

    @Override
    public Color getEdgeColorTarget(Edge e) {
        return edgeColor;
    }



    @Override
    public void setSelected(Node n, boolean value) {
        selection.put(n, value);
    }

    @Override
    public boolean isSelected(Node n) {
        if(n==null)
            return false;
        Boolean b = selection.get(n);
        if(b==null)
            return false;
        else
            return b;
    }

    @Override
    public void resetSelection() {
        selection.clear();
    }

    Map<Node,Boolean> selection = new HashMap<Node, Boolean>();
}
