package org.jzy3d.graphs.trials.facebook2;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.jzy3d.graphs.gephi.workspace.GephiController;
import org.jzy3d.io.FileReader;


public class ConvertToGraphML {
  public static String TRAIN = "data/train/";
  public static String GRAPH = "data/graphs/";

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    GephiController gephi = new GephiController();
    gephi.init();

    processGraph(gephi, "data/competition/train1.txt", "data/competition/train1-no0.graphml");
    gephi.printInfo();

    File[] trains = new File(TRAIN).listFiles();
    for (File train : trains) {

    }
  }


  private static void processGraph(GephiController gephi, String train, String graph)
      throws IOException {
    List<String> lines = FileReader.read(train);
    System.out.println("done read " + train);
    for (String line : lines) {
      String[] split = Pattern.compile("|", Pattern.LITERAL).split(line);
      if (split.length == 3) {
        String left = split[0].trim();
        String right = split[1].trim();
        String value = split[2].trim();
        int v = (int) Float.parseFloat(value);
        boolean onlyIfNon0 = true;
        gephi.addEdge(left, right, v, onlyIfNon0);

        // System.out.println(left + " > " + right + " > " + value);
      } else
        System.err.println("no read: " + split.length + " | " + line);
    }
    gephi.save(graph);
    System.out.println("saved " + train);
  }
}
