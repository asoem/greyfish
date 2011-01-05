// Hyper-Point class supporting KDTree class

package org.asoem.sico.kdtree;

import java.util.Arrays;

import com.google.common.primitives.Doubles;

class HPoint {

	protected final double[] coord;

	protected HPoint(int n) {
		coord = new double[n];
	}

	protected HPoint(double[] x) {
		coord = Arrays.copyOf(x, x.length);
	}

	protected Object clone() {
		return new HPoint(coord);
	}

	protected boolean equals(HPoint p) {
		return Arrays.equals(coord, p.coord);
	}

	protected static double sqrdist(HPoint x, HPoint y) {
		return EuclideanDistance.sqrdist(x.coord, y.coord);
	}
	
	public String toString() {
		return Doubles.join(" ", coord);
	}

}
