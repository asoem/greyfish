package org.asoem.greyfish.utils;

import javolution.lang.MathLib;

public class PolarPoint {

	float phi;
	float distance;

	public PolarPoint(float phi, float distance) {
		super();
		this.phi = phi;
		this.distance = distance;
	}

	public PolarPoint() {
	}

	public float getPhi() {
		return phi;
	}

	public void setPhi(double phi) {
		if (this.phi != phi) {
			this.phi = (float) (phi % MathLib.TWO_PI);
		}
	}

	public void addToPhi(double alpha) {
		if (alpha != 0) {
			setPhi(phi+alpha);
		}
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}
}
