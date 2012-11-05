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

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

/**
 * The demo shows how to do ranking, transform colors and size according to degree
 * or attribute values.
 * <p>
 * The following ranking are applied to the imported network:
 * <ul><li>Rank node colors by degree</li>
 * <li>Rank node size by Betweenness Centrality metric</li>
 * <li>Rank label size by Betweenness Centrality</li>
 * </ul>
 * An interpolation can be set to transformers, see <code>setInterpolator()</code>
 * on transformers.
 * 
 * @author Mathieu Bastian
 */
public class RankingGraph {

    public void script() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        //Import file
        Container container;
        try {
            File file = new File(getClass().getResource("/org/gephi/toolkit/demos/resources/lesmiserables.gml").toURI());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        DirectedGraph graph = graphModel.getDirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        //Rank color by Degree
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
        Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
        AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
        
        colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
        rankingController.transform(degreeRanking,colorTransformer);

        //Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel, attributeModel);

        //Rank size by centrality
        AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Ranking centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
        AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
        sizeTransformer.setMinSize(3);
        sizeTransformer.setMaxSize(20);
        rankingController.transform(centralityRanking,sizeTransformer);

        //Rank label size - set a multiplier size
        Ranking centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
        AbstractSizeTransformer labelSizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
        labelSizeTransformer.setMinSize(1);
        labelSizeTransformer.setMaxSize(3);
        rankingController.transform(centralityRanking2,labelSizeTransformer);

        //Set 'show labels' option in Preview - and disable node size influence on text size
        PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("ranking.pdf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
}
