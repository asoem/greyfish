package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.collect.Product2;

public interface Point2D extends Point, Object2D, Product2<Double, Double> {
	public double getX();
	public double getY();
}
