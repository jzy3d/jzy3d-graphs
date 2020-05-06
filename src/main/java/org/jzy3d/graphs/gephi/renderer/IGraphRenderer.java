package org.jzy3d.graphs.gephi.renderer;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Sphere;

public interface IGraphRenderer {
    public void createOrUpdateNode(Node n);
    public void createOrUpdateEdge(Edge e) ;

    public Sphere createNodeSphere(Coord3d c, Color color, Color wireframe, float radius, int slicing);
    public LineStrip createEdgeLine(Coord3d c1, Coord3d c2, Color color, float width);
}

