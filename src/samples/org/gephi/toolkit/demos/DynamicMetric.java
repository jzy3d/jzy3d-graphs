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

import java.util.Random;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicDouble;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.Interval;
import org.gephi.datalab.api.AttributeColumnsMergeStrategiesController;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicGraph;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Degree;
import org.openide.util.Lookup;

/**
 * This demo main aim is to show how to execute a metric on a dynamic graph.
 * <p>
 * It does the following steps:
 * <ul>
 * <li>Create a project and a workspace, it is mandatory.</li>
 * <li>Generate a random graph into a container.</li>
 * <li>Append this container to the main graph structure.</li>
 * <li>Create a 'date' column (a simple INT between 1990 and 2010) and set
 * random values for each node.</li>
 * <li>Use the Data Laboratory merge strategy to convert this column to a real
 * time interval column.</li>
 * <li>Get a <code>DynamicGraph</code> and count the number of nodes for each
 * year. That shows how to get the subgraph for a particular period.</li>
 * <li>Put the result into a dynamic data structure, that shows how to store result
 * associated to a particular period.</li>
 * <li>Create a <code>InOutDegree</code> metric, execute it for each year and
 * collect the results in a dynamic data structure.</li>
 * </ul>
 * Instead of generating a random 'static' graph and add a fake date column, a
 * dynamic network can be imported with the GEXF file format.
 * <p>
 * The demo shows how to get a <code>DynamicGraph</code> instance, and get
 * sub graphs for a particular time period. Note that the dynamic graph maintains
 * only one sub graph at one time, so keeping multiple <code>Graph</code> instances
 * for further analysis won't work.
 * <p>
 * @author Mathieu Bastian
 */
public class DynamicMetric {

    public void script() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Generate a new random graph into a container
        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        RandomGraph randomGraph = new RandomGraph();
        randomGraph.setNumberOfNodes(500);
        randomGraph.setWiringProbability(0.005);
        randomGraph.generate(container.getLoader());

        //Append container to graph structure
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        importController.process(container, new DefaultProcessor(), workspace);

        //Add a fake 'Date' column to nodes
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeColumn dateColumn = attributeModel.getNodeTable().addColumn("date", AttributeType.INT);

        //Add a random date to all nodes - between 1990 and 2010
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = graphModel.getGraph();
        Random random = new Random();
        for (Node n : graph.getNodes()) {
            Integer randomDataValue = new Integer(random.nextInt(21) + 1990);
            n.getNodeData().getAttributes().setValue(dateColumn.getIndex(), randomDataValue);
        }

        //Use the Data laboratory merge strategy to convert this Integer column to the TimeInterval column
        AttributeColumnsMergeStrategiesController dataLabController = Lookup.getDefault().lookup(AttributeColumnsMergeStrategiesController.class);
        dataLabController.mergeNumericColumnsToTimeInterval(attributeModel.getNodeTable(), dateColumn, null, 1990, 2010);

        //Use the DynamicModel dynamic graph factory
        DynamicModel dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel();
        DynamicGraph dynamicGraph = dynamicModel.createDynamicGraph(graph);

        //Get the number of nodes for each period of one year - and store it in a proper data structure
        DynamicInteger numberofNodes = new DynamicInteger();
        for (int i = 1990; i < 2009; i++) {
            int low = i;
            int high = i + 1;
            Graph subGraph = dynamicGraph.getSnapshotGraph(low, high);
            int count = subGraph.getNodeCount();
            numberofNodes = new DynamicInteger(numberofNodes, new Interval<Integer>(low, high, count));   //DynamicInteger is immutable
        }

        //Get all intervals and print values
        System.out.println("Number of nodes:");
        for (Interval<Integer> interval : numberofNodes.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
            int low = (int) interval.getLow();
            int high = (int) interval.getHigh();
            System.out.println(low + "-" + high + "  ->  " + interval.getValue());
        }

        //Create a Degree metric to get the average degree for each time interval
        Degree degree = new Degree();

        //Compute the metric for each subgraph and put result in a DynamicDouble
        DynamicDouble averageDegree = new DynamicDouble();
        for (int i = 1990; i < 2009; i++) {
            int low = i;
            int high = i + 1;
            Graph subGraph = dynamicGraph.getSnapshotGraph(low, high);
            degree.execute(subGraph.getGraphModel(), attributeModel);
            double result = degree.getAverageDegree();
            averageDegree = new DynamicDouble(averageDegree, new Interval<Double>(low, high, result));
        }

        //Get all intervals and print values
        System.out.println("Average degree:");
        for (Interval<Double> interval : averageDegree.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
            int low = (int) interval.getLow();
            int high = (int) interval.getHigh();
            System.out.println(low + "-" + high + "  ->  " + interval.getValue());
        }
    }
}
