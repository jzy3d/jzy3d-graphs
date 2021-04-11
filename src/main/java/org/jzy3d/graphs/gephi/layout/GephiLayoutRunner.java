package org.jzy3d.graphs.gephi.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class GephiLayoutRunner {
  public void run(Layout layout, int steps) {
    layout.initAlgo();
    for (int i = 0; i < steps && layout.canAlgo(); i++)
      layout.goAlgo();
  }

  public void run(Layout layout, int steps, IOnStepDoneListener listener) {
    layout.initAlgo();
    for (int i = 0; i < steps /* && layout.canAlgo() */; i++) {
      layout.goAlgo();
      listener.stepDone(layout);
    }
  }

  /*******************/

  public void runAll(GephiLayoutController store) {
    runAll(store.getWorkspaces(), store);
  }

  public void runAll(Collection<Workspace> workspaces, GephiLayoutController store) {
    ExecutorService executor = Executors.newFixedThreadPool(workspaces.size());

    List<Future<?>> fs = createFutures(executor, workspaces, store);
    try {
      for (Future<?> f : fs)
        f.get();
    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }
    executor.shutdown();
  }

  protected List<Future<?>> createFutures(ExecutorService executor,
      Collection<Workspace> workspaces, GephiLayoutController store) {
    List<Future<?>> fs = new ArrayList<Future<?>>();
    for (Workspace w : workspaces)
      fs.add(executor.submit(createLayoutRunnable(w, store.getLayout(w))));
    return fs;
  }

  protected Runnable createLayoutRunnable(final Workspace workspace, final Layout layout) {
    return new Runnable() {
      @Override
      public void run() {
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        AutoLayout autoLayout = new AutoLayout(10, TimeUnit.SECONDS);
        autoLayout.setGraphModel(gm);
        autoLayout.addLayout(layout, 1f);
        autoLayout.execute();
      }
    };
  }
}
