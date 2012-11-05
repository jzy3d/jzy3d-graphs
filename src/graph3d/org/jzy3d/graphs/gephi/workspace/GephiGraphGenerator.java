package org.jzy3d.graphs.gephi.workspace;

import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.generator.plugin.WattsStrogatz;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GephiGraphGenerator {
    public void random(Workspace workspace, int n, double wiringProba){
        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        RandomGraph randomGraph = new RandomGraph();
        randomGraph.setNumberOfNodes(n);
        randomGraph.setWiringProbability(wiringProba);
        randomGraph.generate(container.getLoader());

        //Append container to graph structure
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        importController.process(container, new DefaultProcessor(), workspace);
    }
    
    public void wattsStrogatz(Workspace workspace, int n, int n2, double wiringProba){
        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        
        WattsStrogatz wattsStrogatz = new WattsStrogatz();
        wattsStrogatz.setNumberOfNodes(n);
        wattsStrogatz.setNumberOfNeighbors(n2);
        wattsStrogatz.setRewiringProbability(wiringProba);
        wattsStrogatz.generate(container.getLoader());
        
        //Append container to graph structure
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        importController.process(container, new DefaultProcessor(), workspace);
    }
}
