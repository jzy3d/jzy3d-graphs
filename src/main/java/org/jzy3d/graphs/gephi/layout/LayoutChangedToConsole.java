package org.jzy3d.graphs.gephi.layout;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.jzy3d.maths.Coord3d;



public class LayoutChangedToConsole implements IOnStepDoneListener {
  public LayoutChangedToConsole(GraphModel g) {
    this.g = g;
  }

  @Override
  public void stepDone(Layout layout) {
    Graph graph = g.getGraph();

    for (Node n : graph.getNodes()) {

      Coord3d center = new Coord3d(n.x(), n.y(), n.z());
      /*
       * NodeData nd = n.getNodeData(); float radius = n.getRadius(); String label = nd.getLabel();
       */

      System.out.println(n.getLabel() + " " + center);
    }

  }

  protected GraphModel g;
}
