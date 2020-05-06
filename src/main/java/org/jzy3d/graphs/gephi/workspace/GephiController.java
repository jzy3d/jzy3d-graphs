package org.jzy3d.graphs.gephi.workspace;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GephiController {
    Workspace workspace;

    /**Init a project - and therefore a workspace*/
    public Workspace init() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        workspace = pc.getCurrentWorkspace();
        pc.openWorkspace(workspace);
        return workspace;
    }

    /** random init required to have z!=0*/
    public void randomizeGraphLayout(GraphModel g){
        for(Node n: g.getGraph().getNodes()){
            n.setX((float)Math.random());
            n.setY((float)Math.random());
            n.setZ((float)Math.random());
        }
    }

    public void sizeWithNodeNeighbourCount(GraphModel g){
        for(Node n: g.getGraph().getNodes()){
            int c = getNeighbourCount(g,n);
            //System.out.println("n:"+c);
            n.setSize(Math.min(c,1));
        }
    }

    public int getNeighbourCount(GraphModel g, Node n){
        NodeIterable neigh = g.getGraphVisible().getNeighbors(n);
        int k = 0;
        for(Node o: neigh){
            k++;
        }
        return k;
    }

    public GraphModel getGraph(){
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }

    public static void layoutAuto(GraphModel graphModel){
        AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
        autoLayout.setGraphModel(graphModel);
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("Adjust by Sizes", Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("Repulsion strength", new Double(500.), 0f);//500 for the complete period
        autoLayout.addLayout(firstLayout, 0.5f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        autoLayout.execute();

        //Export
    }

    public void importGraph(String f){
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file
        Container container;
        try {
            File file = new File(f);
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);   //Force DIRECTED
            //container.setAllowAutoNode(false);  //Don't create missing nodes

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);
    }


    /**
     * "gexf"
     * "graphml"
     * @param filename
     */
    public void save(String filename){
        save(filename, "graphml");
    }

    public void save(String filename, String format){
        File out = new File(filename);
        if(!out.getParentFile().exists())
            out.getParentFile().mkdirs();
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        GraphExporter exporter = (GraphExporter) ec.getExporter(format);     //Get GraphML exporter
        exporter.setWorkspace(workspace);
     // exporter.setExportVisible(true);  //Only exports the visible (filtered) graph
        try {
            ec.exportFile(out, exporter);
        //    ec.exportFile(new File(filename));

        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }



    public void exportPdf(String file) throws IOException {
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        ec.exportFile(new File(file));


        //PDF Exporter config and export to Byte array
       /* PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
        pdfExporter.setPageSize(PageSize.A0);
        pdfExporter.setWorkspace(workspace);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ec.exportStream(baos, pdfExporter);
        byte[] pdf = baos.toByteArray();*/

        //StringWriter stringWriter = new StringWriter();
        //ec.exportWriter(stringWriter, (CharacterExporter) exporterGraphML);
        //System.out.println(stringWriter.toString());   //Uncomment this line

    }

    /* EDIT CURRENT GRAPH */

    int nn = 0;
    int ne = 0;

    Map<String,Node> nodes = new HashMap<String,Node>();

    /**
     * memorize store a map of name->node to allow building edges by node string names.
     */
    public Node addNode(String name, boolean memorize) {
        GraphModel graph = getGraph();
        Node node = graph.factory().newNode("n" + nn);
        node.setLabel("Node " + nn);
        graph.getDirectedGraph().addNode(node);
        if(memorize)
            nodes.put(name, node);
        nn++;
        return node;
    }

    public void addEdge(String n1, String n2, int value) {
        addEdge(n1, n2, value, false);
    }

    public void addEdge(String n1, String n2, int value, boolean onlyIfNon0) {
        Node node1 = nodes.get(n1);
        Node node2 = nodes.get(n2);
        if(node1==null){
            node1 = addNode(n1, true);
        }
        if(node2==null){
            node2 = addNode(n2, true);
        }
        if(onlyIfNon0){
            if(value!=0)
                addEdge(node1, node2, value);
        }
        else{
            addEdge(node1, node2, value);
        }
    }

    public Node addNode(String name) {
        return addNode(name, false);
    }

    public void addEdge(Node n1, Node n2, int value) {
        GraphModel graph = getGraph();
        Edge e1 = graph.factory().newEdge(n1, n2, value, true);
        graph.getDirectedGraph().addEdge(e1);
        ne++;
    }

    public void printInfo(){
        GraphModel graph = getGraph();
        DirectedGraph directedGraph = graph.getDirectedGraph();
        //Count nodes and edges
        System.out.println("Nodes: "+directedGraph.getNodeCount()+" Edges: "+directedGraph.getEdgeCount());

        //Get a UndirectedGraph now and count edges
        //UndirectedGraph undirectedGraph = graph.getUndirectedGraph();
        //System.out.println("Edges: "+undirectedGraph.getEdgeCount());   //The mutual edge is automatically merged

    }

    public GraphModel editCurrentGraph() {
        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        //Create three nodes
        Node n0 = graphModel.factory().newNode("n0");
        n0.setLabel("Node 0");
        Node n1 = graphModel.factory().newNode("n1");
        n1.setLabel("Node 1");
        Node n2 = graphModel.factory().newNode("n2");
        n2.setLabel("Node 2");

        //Create three edges
        Edge e1 = graphModel.factory().newEdge(n1, n2, 1, true);
        Edge e2 = graphModel.factory().newEdge(n0, n2, 2, true);
        Edge e3 = graphModel.factory().newEdge(n2, n0, 2, true);   //This is e2's mutual edge

        //Append as a Directed Graph
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        directedGraph.addNode(n0);
        directedGraph.addNode(n1);
        directedGraph.addNode(n2);
        directedGraph.addEdge(e1);
        directedGraph.addEdge(e2);
        directedGraph.addEdge(e3);

        //Count nodes and edges
        System.out.println("Nodes: "+directedGraph.getNodeCount()+" Edges: "+directedGraph.getEdgeCount());

        //Get a UndirectedGraph now and count edges
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        System.out.println("Edges: "+undirectedGraph.getEdgeCount());   //The mutual edge is automatically merged

        //Iterate over nodes
        for(Node n : directedGraph.getNodes()) {
            Node[] neighbors = directedGraph.getNeighbors(n).toArray();
            System.out.println(n.getLabel()+" has "+neighbors.length+" neighbors");
        }

        //Iterate over edges
        for(Edge e : directedGraph.getEdges()) {
            System.out.println(e.getSource().getId()+" -> "+e.getTarget().getId());
        }

        //Find node by id
        Node node2 = directedGraph.getNode("n2");

        //Get degree
        System.out.println("Node2 degree: "+directedGraph.getDegree(node2));

        //Modify the graph while reading
        //Due to locking, you need to use toArray() on Iterable to be able to modify
        //the graph in a read loop
        /*for(Node n : directedGraph.getNodes().toArray()) {
            directedGraph.removeNode(n);
        }*/

        return graphModel;
    }
}

