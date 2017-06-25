package org.jzy3d.graphs.gephi.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.gephi.preview.api.PreviewController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GephiLayoutController {
    public GephiLayoutController(){
        init();
    }
    
    public void init(){
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();        
        //graphController = new NetlightGraphController();
    }
    
    /****************/

    public Workspace newWorkspace(){
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        
        Workspace w = projectController.newWorkspace(projectController.getCurrentProject());
        
        workspaces.add(w);
        return w;
    }
    
    public List<Workspace> getWorkspaces() {
        return workspaces;
    }

    /****************/
    
    public GraphModel getGraphModel(){
        return getGraphModel(null);
    }
    
    public GraphModel getGraphModel(Workspace workspace){
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        
        if(workspace!=null)
            projectController.openWorkspace(workspace);
        //new NetlightGraphController().getModel();
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }
    
    /*public GraphSheet getGraphSheet(){
        return getGraphSheet(null);
    }*/
    
    /*public GraphSheet getGraphSheet(Workspace workspace){
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        
        //if(workspace!=null)
            projectController.openWorkspace(workspace);
        
        PreviewControllerImpl controller;
        if(!workspacePreview.containsKey(workspace)){
            controller = (PreviewControllerImpl)Lookup.getDefault().lookup(PreviewController.class);
            workspacePreview.put(workspace, controller);
        }
        else{
            controller = (PreviewControllerImpl)workspacePreview.get(workspace);
        }
        controller.buildGraph();
        //System.out.println(controller);
        
        return controller.getGraphSheet();
    }*/
    
    /****************/
    
    public void setWorkspaceLayout(Workspace workspace, Layout layout){
        layoutMap.put(workspace, layout);
    }
    
    public Layout getLayout(Workspace workspace){
        return layoutMap.get(workspace);
    }
    
    /****************/

    public ProjectController getProjectController() {
        return projectController;
    }

    /*public GraphController getGraphController() {
        return graphController;
    }

    public PreviewController getPreviewController() {
        return vc;
    }*/
    
    /****************/
    
    protected ProjectController projectController;
    protected PreviewController vc;
    protected GraphController graphController;
    
    protected Map<Workspace, Layout> layoutMap = new HashMap<Workspace, Layout>();
    
    protected List<Workspace> workspaces = new ArrayList<Workspace>(); 
    
    protected Map<Workspace, PreviewController> workspacePreview = new HashMap<Workspace, PreviewController>();
}
