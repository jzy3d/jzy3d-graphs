package org.jzy3d.graphs.gephi.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.controllers.mouse.picking.IObjectPickedListener;
import org.jzy3d.chart.controllers.mouse.picking.PickingSupport;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.graphs.GraphChart;
import org.jzy3d.colors.Color;
import org.jzy3d.graphs.gephi.layout.IOnStepDoneListener;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Pair;
import org.jzy3d.maths.TicToc;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.primitives.pickable.PickableSphere;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.text.DrawableTextWrapper;
import org.jzy3d.plot3d.text.align.Halign;
import org.jzy3d.plot3d.text.drawable.DrawableTextBillboard;
import org.jzy3d.plot3d.text.drawable.DrawableTextBitmap;

/**
 * Renders the graph according to visual settings enables interactive settings
 * of the chart (mouse, keyboard, etc). Listen for graph layout change to update
 * display Support peeking facilities (TODO add newMousePicking())
 *
 */
public class GraphRenderer implements IOnStepDoneListener, IGraphRenderer, IObjectPickedListener {
    public static GraphRenderer create(GraphModel g, Quality quality, String wt, String chart) {
        GraphRenderer rep = new GraphRenderer(g, newChart(g, quality, wt, chart));
        return rep;
    }

    public static GraphRenderer create(GraphModel g, GraphRendererSettings settings, Quality quality, String wt, String chart) {
        GraphRenderer renderer = new GraphRenderer(g, settings, newChart(g, quality, wt, chart));

        /*
         * view.addViewLifecycleChangedListener(new
         * IViewLifecycleEventListener() {
         * 
         * @Override public void viewWillRender(ViewLifecycleEvent e) {
         * System.out.println("alpha"); updatePointAlpha(); }
         * 
         * @Override public void viewHasInit(ViewLifecycleEvent e) { } });
         */

        return renderer;
    }

    // Quality.Advanced, "awt"
    protected static Chart newChart(GraphModel g, Quality quality, String wt, String chart) {
        Chart c;// "awt" failed constructor
        if (chart == null || "".equals(chart) || "chart".equals(chart))
            c = AWTChartFactory.chart(quality);//, wt);
        else if ("graphchart".equals(chart)) {
            c = new GraphChart(quality);
        } else
            throw new IllegalArgumentException("Failed to find the chart type '" + chart + "'");
        c.getView().setSquared(false);
        return c;
    }

    /* */

    public GraphRenderer(GraphModel g, Chart c) {
        this(g, new GraphRendererSettings(), c);
    }

    public GraphRenderer(GraphModel g, GraphRendererSettings settings, Chart c) {
        this.g = g;
        this.chart = c;
        this.settings = settings;
        c.addMouseCameraController();
        /*
         * c.getFactory().ne c.addMouseController();
         * c.addScreenshotKeyController(); c.addKeyController();
         */
    }

    /* LAYOUT UPDATE QUERIES */

    int k = 0;

    @Override
    public void stepDone(Layout layout) {
        Graph graph = g.getGraph();
        for (Node n : graph.getNodes())
            createOrUpdateNode(n);
        for (Edge e : graph.getEdges())
            createOrUpdateEdge(e);
        refreshChart();

        if ((k % layoutStepRatio) == 0) {
            if (layoutStepRatio > 1)
                System.out.println(k + ":" + getLastRenderTime() + "ms");
        }
        k++;
    }

    public int getLayoutStepRatio() {
        return layoutStepRatio;
    }

    /**
     * Step ratio is the number of layout steps required to update the chart.
     * Sayed differently, if R is number of rendering occurence, and L number of
     * layout steps, the resulting referesh ratio is R/L.
     * 
     * @param stepRatio
     */
    public void setLayoutStepRatio(int stepRatio) {
        this.layoutStepRatio = stepRatio;
    }

    /* PICKING */

    @Override
    public void objectPicked(List<? extends Object> vertices, PickingSupport picking) {
        System.out.println("picking processing time: " + picking.getLastPickPerfMs() + "ms");
        System.out.println("---");
        for (Object vertex : vertices) {
            Node n = (Node) vertex;
            getSettings().setSelected(n, true);
            Sphere s = getNodeRepresentation(n);
            s.setColor(getSettings().getNodeColor(n));
            s.setWireframeColor(getSettings().getNodeWireframeColor(n));
            System.out.println("picked: " + vertex);
        }
        // refreshChart();
        getChart().render();
    }

    @Override
    public void createOrUpdateNode(Node n) {
        // see also:
        // PreviewController controller =
        // Lookup.getDefault().lookup(PreviewController.class);
        // GraphSheet viewGraph = controller.getGraphSheet();
        // for(org.gephi.preview.api.Node n: viewGraph.getNodes()){
        // Coord2d c = new Coord2d(n.getPosition().getX(),
        // n.getPosition().getY());
        if (hasNodeId(n))
            updateNode(n);
        else
            createNode(n);
    }

    @Override
    public void createOrUpdateEdge(Edge e) {
        if (hasEdgeRepresentation(e))
            updateEdge(e);
        else
            createEdge(e);
    }

    protected String formatNodeLabel(Node nd) {
        return "[" + nd.getId() + "]" + nd.getLabel();
    }

    protected String formatNodeLabel2(Node nd) {
        String label = nd.getLabel();
        int id = label.lastIndexOf(".");
        if (id == -1)
            return label;
        else {
            return label.substring(id + 1);
        }
    }

    /* NODE EDITION */

    protected void createNode(Node n) {
        storeNodeId(n);

        // node information
        //NodeData nd = n.getNodeData();
        Coord3d center = createCoordinate(n);
        float radius = n.size() * settings.getRadiusMultiplier();
        String label = formatNodeLabel(n);

        // sphere
        if (settings.isNodeSphereDisplayed()) {
            Sphere s = createNodeSphere(center, settings.getNodeColor(n), settings.getNodeWireframeColor(n), radius, 15);
            // s.setDisplayed(false);
            addToSceneGraph(s);
            storeNodeSphere(n, s);
        }

        // label
        if (settings.isNodeLabelDisplayed()) {
            Coord3d centerLabel = center.clone();
            DrawableTextWrapper txt = createNodeLabel(label, centerLabel, settings.getNodeLabelColor(n));
            addToSceneGraph(txt);
            storeNodeLable(n, txt);
        }

        // point
        if (settings.isNodePointDisplayed()) {
            Coord3d centerPoint = center.clone();
            Point p = createNodePoint(centerPoint, settings.getNodePointColor(n), (int) radius);
            addToSceneGraph(p);
            storeNodePoint(n, p);
        }

        // picking
        if (!didWarn) {
            System.err.println("WARNING !!! PICKING DISABLED");
            didWarn = true;
        }
        // registerNodeRepresentationPicking(n);
    }

    boolean didWarn = false;

    private Coord3d createCoordinate(Node nd) {
        return new Coord3d(Float.isNaN(nd.x()) ? 0 : nd.x(), Float.isNaN(nd.y()) ? 0 : nd.y(), Float.isNaN(nd.z()) ? 0 : nd.z());
    }

    protected void storeNodeId(Node n) {
        nodeIds.add(n.getId());
    }

    protected void updateNode(Node n) {
        if (settings.isNodeSphereDisplayed())
            updateNodeSphere(n, getNodeRepresentation(n));
        if (settings.isNodeLabelDisplayed())
            updateNodeLabel(n, getNodeLabel(n));
        if (settings.isNodePointDisplayed())
            updateNodePoint(n, getNodePoint(n));
    }

    /* EDGE EDITION */

    protected void createEdge(Edge e) {
        Pair<Coord3d, Coord3d> edgeCoordinates = readEdgeCoords(e);
        LineStrip ls = createEdgeLine(edgeCoordinates.a, edgeCoordinates.b, settings.getEdgeColorSource(e), 1);
        addToSceneGraph(ls);
        storeEdgeRepresentation(e, ls);
    }

    protected Pair<Coord3d, Coord3d> readEdgeCoords(Edge e) {
        Node n1 = e.getSource();
        Node n2 = e.getTarget();
        Coord3d c1 = createCoordinate(n1);
        Coord3d c2 = createCoordinate(n2);
        return new Pair<Coord3d, Coord3d>(c1, c2);
    }

    protected void updateEdge(Edge e) {
        LineStrip ls = getEdgeRepresentation(e);
        updateEdgeLineSourceCoordinate(e, ls);
        updateEdgeLineTargetCoordinate(e, ls);
    }

    public LineStrip getEdgeRepresentation(Edge e) {
        return edgeRepresentation.get(e.getId());
    }

    /* 3D Object creation */

    @Override
    public Sphere createNodeSphere(Coord3d c, Color color, Color wireframe, float radius, int slicing) {
        Sphere s = new PickableSphere(c, radius, slicing, color);
        s.setWireframeColor(wireframe);
        s.setWireframeDisplayed(true);
        s.setWireframeWidth(1);
        s.setFaceDisplayed(true);
        // s.setMaterialAmbiantReflection(materialAmbiantReflection)
        return s;
    }

    protected void updateNodeSphere(Node n, Sphere s) {
        Coord3d position = s.getPosition();
        //NodeData nd = n.getNodeData();
        position.x = n.x();
        position.y = n.y();
        position.z = n.z();
        // System.out.println("sphere:" + position);
        s.updateBounds();
    }

    public DrawableTextWrapper createNodeLabel(String text, Coord3d c, Color color) {
        DrawableTextWrapper txt;

        if (renderer == TextRendererType.BITMAP)
            txt = new DrawableTextBitmap(text, c, color);
        else if (renderer == TextRendererType.BILLBOARD)
            txt = new DrawableTextBillboard(text, c, color);
        else
            throw new RuntimeException("unknown text wrapper");
        txt.setHalign(Halign.LEFT); // TODO: invert left/right
        return txt;
    }

    TextRendererType renderer = TextRendererType.BITMAP;

    protected void updateNodeLabel(Node n, DrawableTextWrapper txt) {
        Coord3d position = txt.getPosition();
        //NodeData nd = n.getNodeData();
        position.x = n.x();
        position.y = n.y();
        position.z = n.z();
        // System.out.println("label:" + position);
        // txt.updateBounds();
    }

    public Point createNodePoint(Coord3d c, Color color, float width) {
        Point s = new Point(c, color);
        s.setWidth(width);
        return s;
    }

    protected void updateNodePoint(Node n, Point p) {
        //NodeData nd = n.getNodeData();
        p.xyz.x = n.x();
        p.xyz.y = n.y();
        p.xyz.z = n.z();
        p.updateBounds();
    }

    @Override
    public LineStrip createEdgeLine(Coord3d c1, Coord3d c2, Color color, float width) {
        List<Point> points = new ArrayList<Point>();
        points.add(new Point(c1));
        points.add(new Point(c2));
        LineStrip ls = new LineStrip(2);
        ls.setWidth(width);
        ls.setWireframeColor(color);
        ls.addAll(points);
        return ls;
    }

    protected void updateEdgeLineSourceCoordinate(Edge e, LineStrip ls) {
        Node n1 = e.getSource();
        //NodeData nd1 = n1.getNodeData();
        ls.get(0).xyz.x = n1.x();
        ls.get(0).xyz.y = n1.y();
        ls.get(0).xyz.z = n1.z();
        ls.updateBounds();
    }

    protected void updateEdgeLineTargetCoordinate(Edge e, LineStrip ls) {
        Node n2 = e.getTarget();
        //NodeData nd2 = n2.getNodeData();
        ls.get(1).xyz.x = n2.x();
        ls.get(1).xyz.y = n2.y();
        ls.get(1).xyz.z = n2.z();
        ls.updateBounds();
    }

    protected boolean updateViewAtCreateObject = false;

    /* UTILS */

    TicToc t = new TicToc();
    double lastRenderTime = 0;
    boolean refreshing = false;

    protected void refreshChart() {
        if (!refreshing) {
            refreshing = true;
            t.tic();
            BoundingBox3d bounds = chart.getScene().getGraph().getBounds();
            chart.getView().lookToBox(bounds);

            boolean isAnimated = true;
            if (!isAnimated) {
                chart.render(); // si pas d'animateur
            }
            refreshing = false;
            t.toc();
            lastRenderTime = t.elapsedMilisecond();
        }
    }

    public double getLastRenderTime() {
        return lastRenderTime;
    }

    protected void addToSceneGraph(AbstractDrawable s) {
        chart.getScene().getGraph().add(s, updateViewAtCreateObject);
    }

    protected void storeNodePoint(Node n, Point p) {
        nodePoint.put(n.getId(), p);
    }

    protected void storeNodeSphere(Node n, Sphere s) {
        nodeRepresentation.put(n.getId(), s);
    }

    protected void storeNodeLable(Node n, DrawableTextWrapper text) {
        nodeLabelRepresentation.put(n.getId(), text);
    }

    protected void storeEdgeRepresentation(Edge e, LineStrip ls) {
        edgeRepresentation.put(e.getId(), ls);
    }

    protected boolean hasNodeId(Node n) {
        return nodeIds.contains(n.getId());
    }

    protected boolean hasEdgeRepresentation(Edge e) {
        return edgeRepresentation.containsKey(e.getId());
    }

    public Point getNodePoint(Node n) {
        return nodePoint.get(n.getId());
    }

    public DrawableTextWrapper getNodeLabel(Node n) {
        return nodeLabelRepresentation.get(n.getId());
    }

    public Sphere getNodeRepresentation(Node n) {
        return nodeRepresentation.get(n.getId());
    }

    /* */

    public GraphRendererSettings getSettings() {
        return settings;
    }

    public Chart getChart() {
        return chart;
    }

    /*
     * public NewtMousePickingController<Node,String> getChartMouseController(){
     * return (NewtMousePickingController<Node,String>)
     * ((CanvasAWT)getChart().getCanvas()).getMouseListeners()[0]; }
     * 
     * public PickingSupport getPickingSupport(){ try{ return
     * getChartMouseController().getPickingSupport(); } catch(ClassCastException
     * e){ return null; } }
     * 
     * public void registerNodeRepresentationPicking(Node n){ Sphere s =
     * getNodeRepresentation(n); PickingSupport sp = getPickingSupport();
     * if(sp!=null) sp.registerDrawableObject(s, n); }
     */

    public void openChart() {
        ChartLauncher.openStaticChart(getChart());
    }

    /* */

    protected GraphModel g;

    protected Chart chart;
    protected GraphRendererSettings settings;

    protected int layoutStepRatio = 1;

    protected Set<Object> nodeIds = new HashSet<Object>();
    protected Map<Object, Point> nodePoint = new HashMap<Object, Point>();
    protected Map<Object, Sphere> nodeRepresentation = new HashMap<Object, Sphere>();
    protected Map<Object, DrawableTextWrapper> nodeLabelRepresentation = new HashMap<Object, DrawableTextWrapper>();
    protected Map<Object, LineStrip> edgeRepresentation = new HashMap<Object, LineStrip>();
}
