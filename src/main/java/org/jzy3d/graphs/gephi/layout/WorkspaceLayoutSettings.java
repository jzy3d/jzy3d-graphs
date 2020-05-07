package org.jzy3d.graphs.gephi.layout;

import java.util.HashMap;
import java.util.Map;

import org.gephi.project.api.Workspace;
import org.jzy3d.maths.Coord2d;

public class WorkspaceLayoutSettings {
    public void setLayoutScale(Workspace workspace, Coord2d scale){
        scales.put(workspace, scale);
    }

    public Coord2d getLayoutScale(Workspace workspace){
        return scales.get(workspace);
    }

    public void setLayoutCenter(Workspace workspace, Coord2d center){
        centers.put(workspace, center);
    }

    public Coord2d getLayoutCenter(Workspace workspace){
        return centers.get(workspace);
    }


    protected Map<Workspace,Coord2d> scales = new HashMap<Workspace,Coord2d>();
    protected Map<Workspace,Coord2d> centers = new HashMap<Workspace,Coord2d>();
}

