/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.toolkit.demos;

import java.io.File;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 * Shows how to manipulate attributes. Attributes are the data associated to nodes
 * and edges. The AttributeAPI's role is to store the different columns information.
 * <p>
 * The demo shows how to add columns and values to nodes and how to iterate.
 * 
 * @author Mathieu Bastian
 */
public class ManipulateAttributes {

    public void script() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file
        Container container;
        try {
            File file = new File(getClass().getResource("/org/gephi/toolkit/demos/resources/polblogs.gml").toURI());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
            container.setAllowAutoNode(false);  //Don't create missing nodes
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //List node columns
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel model = ac.getModel();
        for (AttributeColumn col : model.getNodeTable().getColumns()) {
            System.out.println(col);
        }

        //Add boolean column
        AttributeColumn testCol = model.getNodeTable().addColumn("test", AttributeType.BOOLEAN);

        //Write values to nodes
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        for (Node n : graphModel.getGraph().getNodes()) {
            n.getNodeData().getAttributes().setValue(testCol.getIndex(), Boolean.TRUE);
        }

        //Iterate values - fastest
        AttributeColumn sourceCol = model.getNodeTable().getColumn("source");
        for (Node n : graphModel.getGraph().getNodes()) {
            System.out.println(n.getNodeData().getAttributes().getValue(sourceCol.getIndex()));
        }

        //Iterate values - normal
        for (Node n : graphModel.getGraph().getNodes()) {
            System.out.println(n.getNodeData().getAttributes().getValue("source"));
        }
    }
}
