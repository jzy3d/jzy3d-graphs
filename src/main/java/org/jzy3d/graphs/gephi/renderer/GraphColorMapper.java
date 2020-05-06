package org.jzy3d.graphs.gephi.renderer;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.ColorMapHotCold;
import org.jzy3d.events.IViewLifecycleEventListener;
import org.jzy3d.events.ViewLifecycleEvent;
import org.jzy3d.maths.Pair;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.ordering.BarycentreOrderingStrategy;
import org.jzy3d.plot3d.rendering.scene.Graph;
import org.jzy3d.plot3d.rendering.view.View;

public class GraphColorMapper {
	protected Color color;
	protected View view;
	protected Graph graph;
	protected BarycentreOrderingStrategy ordering;
	protected ColorMapHotCold colormap;

	public GraphColorMapper(){
		colormap = new ColorMapHotCold();
		colormap.setDirection(false);
	}
	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public View getView() {
		return view;
	}

	/**
	 * Enable a view listener to update points
	 * @param view
	 */
	public void setView(View view) {
		this.ordering = new BarycentreOrderingStrategy(view);
		this.ordering.setCamera(view.getCamera());

		this.view = view;
		view.addViewLifecycleChangedListener(new IViewLifecycleEventListener() {
			@Override
			public void viewWillRender(ViewLifecycleEvent e) {
				//System.out.println("alpha");
				updatePointAlpha();
				updateLines();
			}

			@Override
			public void viewHasInit(ViewLifecycleEvent e) {
			}
		});
	}

	public void updatePointAlpha(){
		double minDist = Double.POSITIVE_INFINITY;
		double maxDist = Double.NEGATIVE_INFINITY;
		List<AbstractDrawable> drawables = null;
		try{
			drawables = graph.getDecomposition();
		}
		catch(ConcurrentModificationException e){
			return;
		}

		List<Pair<Point,Double>> pointDistances = new ArrayList<Pair<Point,Double>>();
		for(AbstractDrawable d: drawables){
			if(d instanceof Point){
				Point p = (Point)d;
				double dist = ordering.score(p.xyz);
				if(dist>maxDist)
					maxDist = dist;
				if(dist<minDist)
					minDist = dist;
				pointDistances.add(new Pair<Point,Double>(p, dist));
			}
		}


		double offset = maxDist - minDist;

		for(Pair<Point,Double> pair: pointDistances){
			double dist = pair.b;

			// update alpha

			double ratio = (dist - minDist) / offset;
			updatePointAlpha(pair.a, ratio);

			//updatePointColor(pair.a, dist, minDist, maxDist);
		}
	}

	protected void updatePointAlpha(Point p, double ratio) {
		p.rgb.a = (float)(0.1 + (0.3*ratio));
	}

	protected void updatePointColor(Point p, double dist, double minDist, double maxDist){
		p.rgb = colormap.getColor(0, 0, dist, minDist, maxDist);
	}

	public void updateLines(){
		double minDist = Double.POSITIVE_INFINITY;
		double maxDist = Double.NEGATIVE_INFINITY;
		List<AbstractDrawable> drawables = graph.getDecomposition();

		List<Pair<Point,Double>> pointDistances = new ArrayList<Pair<Point,Double>>();
		for(AbstractDrawable d: drawables){
			if(d instanceof LineStrip){
				LineStrip line = (LineStrip)d;

				Point p1 = line.get(0);
				double dist = ordering.score(p1.xyz);
				if(dist>maxDist)
					maxDist = dist;
				if(dist<minDist)
					minDist = dist;
				pointDistances.add(new Pair<Point,Double>(p1, dist));

				Point p2 = line.get(1);
				dist = ordering.score(p2.xyz);
				if(dist>maxDist)
					maxDist = dist;
				if(dist<minDist)
					minDist = dist;
				pointDistances.add(new Pair<Point,Double>(p2, dist));
			}
		}


		double offset = maxDist - minDist;

		for(Pair<Point,Double> pair: pointDistances){
			updatePointColor(pair.a, pair.b, minDist, maxDist);

			// update alpha
			//double ratio = (pair.b - minDist) / offset;
			//updatePointAlpha(pair.a, ratio);
		}
	}
}

