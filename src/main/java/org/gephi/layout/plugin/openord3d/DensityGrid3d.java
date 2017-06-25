/*
Copyright 2007 Sandia Corporation. Under the terms of Contract
DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government retains
certain rights in this software.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
 * Neither the name of Sandia National Laboratories nor the names of
its contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gephi.layout.plugin.openord3d;

import java.util.ArrayDeque;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Pernollet (added z+few params adjustement)
 */
public class DensityGrid3d implements Cloneable {

	private static final int GRID_SIZE = 100; // size of Density grid // Original 2d version was 1000
	private static final float VIEW_SIZE = 4000; // actual physical size of
													// layout plane
	private static final int RADIUS = 1; // div 10 // radius for density fall-off: // Original version was 10
	private static final int HALF_VIEW = 2000;
	private static final float VIEW_TO_GRID = 0.025f; // original version was 0.05
	private float[][][] density;
	private float[][][] fallOff;
	private ArrayDeque<Node3d>[][][] bins;

	public void init() {
		density = new float[GRID_SIZE][GRID_SIZE][GRID_SIZE];
		fallOff = new float[RADIUS * 2 + 1][RADIUS * 2 + 1][RADIUS * 2 + 1];
		bins = new ArrayDeque[GRID_SIZE][GRID_SIZE][GRID_SIZE];

		for (int i = -RADIUS; i <= RADIUS; i++) {
			for (int j = -RADIUS; j <= RADIUS; j++) {
				for (int k = -RADIUS; k <= RADIUS; k++) {
					fallOff[i + RADIUS][j + RADIUS][k + RADIUS] = (float) ((RADIUS - Math
							.abs((float) i)) / RADIUS)
							* (float) ((RADIUS - Math.abs((float) j)) / RADIUS)
							* (float) ((RADIUS - Math.abs((float) k)) / RADIUS);
				}
			}
		}

		/*
		 * for (int i = 0; i < GRID_SIZE; i++) { for (int j = 0; j < GRID_SIZE;
		 * j++) { bins[i][j] = new ArrayDeque<Node>(); } }
		 */
	}

	public float getDensity(float nX, float nY, float nZ, boolean fineDensity) {
		int xGrid, yGrid, zGrid;
		float xDist, yDist, zDist, distance, density = 0;
		int boundary = 10; // boundary around plane

		xGrid = (int) ((nX + HALF_VIEW + .5) * VIEW_TO_GRID);
		yGrid = (int) ((nY + HALF_VIEW + .5) * VIEW_TO_GRID);
		zGrid = (int) ((nZ + HALF_VIEW + .5) * VIEW_TO_GRID);

		// Check for edges of density grid (10000 is arbitrary high density)
		if (xGrid > GRID_SIZE - boundary || xGrid < boundary) {
			return 10000;
		}
		if (yGrid > GRID_SIZE - boundary || yGrid < boundary) {
			return 10000;
		}
		if (zGrid > GRID_SIZE - boundary || zGrid < boundary) {
			return 10000;
		}

		if (fineDensity) {
			for (int i = yGrid - 1; i <= yGrid + 1; i++) {
				for (int j = xGrid - 1; j <= xGrid + 1; j++) {
					for (int k = zGrid - 1; k <= zGrid + 1; k++) {
						ArrayDeque<Node3d> deque = bins[i][j][k];
						if (deque != null) {
							for (Node3d bi : deque) {
								xDist = nX - bi.x;
								yDist = nY - bi.y;
								zDist = nZ - bi.z;
								distance = xDist * xDist + yDist * yDist
										+ zDist * zDist;
								density += 1e-4 / (distance + 1e-50);
							}
						}
					}
				}
			}
		} else {
			density = this.density[yGrid][xGrid][zGrid];
			density *= density;
		}
		return density;
	}

	public void add(Node3d n, boolean fineDensity) {
		if (fineDensity) {
			fineAdd(n);
		} else {
			add(n);
		}
	}

	public void substract(Node3d n, boolean firstAdd, boolean fineFirstAdd,
			boolean fineDensity) {
		if (fineDensity && !fineFirstAdd) {
			fineSubstract(n);
		} else if (!firstAdd) {
			substract(n);
		}
	}

	private void substract(Node3d n) {
		int xGrid, yGrid, zGrid, diam;

		xGrid = (int) ((n.subX + HALF_VIEW + 0.5f) * VIEW_TO_GRID);
		yGrid = (int) ((n.subY + HALF_VIEW + 0.5f) * VIEW_TO_GRID);
		zGrid = (int) ((n.subZ + HALF_VIEW + 0.5f) * VIEW_TO_GRID);
		xGrid -= RADIUS;
		yGrid -= RADIUS;
		zGrid -= RADIUS;
		diam = 2 * RADIUS;

		for (int i = 0; i <= diam; i++) {
			int oldXGrid = xGrid;
			for (int j = 0; j <= diam; j++) {
				// density[yGrid][xGrid] -= fallOff[i][j];

				int oldZGrid = zGrid;
				for (int k = 0; k <= diam; k++) {
					density[yGrid][xGrid][zGrid] -= fallOff[i][j][k];
					zGrid++;
				}
				zGrid = oldZGrid;
				xGrid++;
			}
			yGrid++;
			xGrid = oldXGrid;
		}
	}

	private void add(Node3d n) {
		int xGrid, yGrid, zGrid, diam;

		xGrid = (int) ((n.x + HALF_VIEW + .5) * VIEW_TO_GRID);
		yGrid = (int) ((n.y + HALF_VIEW + .5) * VIEW_TO_GRID);
		zGrid = (int) ((n.y + HALF_VIEW + .5) * VIEW_TO_GRID);

		n.subX = n.x;
		n.subY = n.y;
		n.subZ = n.z;

		xGrid -= RADIUS;
		yGrid -= RADIUS;
		zGrid -= RADIUS;
		diam = 2 * RADIUS;

		if ((xGrid + RADIUS >= GRID_SIZE) || (xGrid < 0)
				|| (yGrid + RADIUS >= GRID_SIZE) || (yGrid < 0)
				|| (zGrid + RADIUS >= GRID_SIZE) || (zGrid < 0)) {
			throw new RuntimeException("Error: Exceeded density grid with "
					+ "xGrid = " + xGrid + " and yGrid = " + yGrid
					+ " and zGrid = " + zGrid);
		}

		for (int i = 0; i <= diam; i++) {
			int oldXGrid = xGrid;
			for (int j = 0; j <= diam; j++) {
				// density[yGrid][xGrid] += fallOff[i][j];
				int oldZGrid = zGrid;
				for (int k = 0; k <= diam; k++) {
					density[yGrid][xGrid][zGrid] += fallOff[i][j][k];
					zGrid++;
				}
				zGrid = oldZGrid;
				xGrid++;
			}
			yGrid++;
			xGrid = oldXGrid;
		}
	}

	private void fineSubstract(Node3d n) {
		int xGrid, yGrid, zGrid;

		xGrid = (int) ((n.subX + HALF_VIEW + .5) * VIEW_TO_GRID);
		yGrid = (int) ((n.subY + HALF_VIEW + .5) * VIEW_TO_GRID);
		zGrid = (int) ((n.subZ + HALF_VIEW + .5) * VIEW_TO_GRID);
		ArrayDeque<Node3d> deque = bins[yGrid][xGrid][zGrid];
		if (deque != null) {
			deque.pollFirst();
		}
	}

	private void fineAdd(Node3d n) {
		int xGrid, yGrid, zGrid;

		xGrid = (int) ((n.x + HALF_VIEW + .5) * VIEW_TO_GRID);
		yGrid = (int) ((n.y + HALF_VIEW + .5) * VIEW_TO_GRID);
		zGrid = (int) ((n.z + HALF_VIEW + .5) * VIEW_TO_GRID);

		n.subX = n.x;
		n.subY = n.y;
		n.subZ = n.z;
		ArrayDeque<Node3d> deque = bins[yGrid][xGrid][zGrid];
		if (deque == null) {
			deque = new ArrayDeque<Node3d>();
			bins[yGrid][xGrid][zGrid] = deque;
		}
		deque.addLast(n);
	}

	public static float getViewSize() {
		return (VIEW_SIZE * 0.8f) - (RADIUS / 0.25f) * 2f;
	}

	/*
	 * @Override protected DensityGrid clone() { DensityGrid densityGrid = new
	 * DensityGrid(); densityGrid.fallOff = this.fallOff; densityGrid.density =
	 * new float[GRID_SIZE][GRID_SIZE]; densityGrid.bins = new
	 * ArrayDeque[GRID_SIZE][GRID_SIZE]; for (int i = 0; i < GRID_SIZE; i++) {
	 * System.arraycopy(this.density[i], 0, densityGrid.density[i], 0,
	 * GRID_SIZE); for (int j = 0; j < GRID_SIZE; j++) { densityGrid.bins[i][j]
	 * = bins[i][j].clone(); } } return densityGrid; }
	 */
}
