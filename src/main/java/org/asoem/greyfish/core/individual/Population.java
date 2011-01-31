package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.Rainbow;
import org.asoem.greyfish.utils.RandomUtils;
import org.simpleframework.xml.Element;

import java.awt.*;
import java.io.Serializable;
import java.util.Collection;


public class Population implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4989452649218269512L;

	@Element(name="name", required = false)
	private String name;

	@Element(name="color")
	private Color color;

	public Population(String name) {
		this.name = name;
		setColor(Rainbow.getInstance().createRainbow(256)[RandomUtils.nextInt(256)]);
	}

	public Population() {
		this("");
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Population that = (Population) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public static Population[] fromIndiviuduals(Collection<Individual> individuals) {
		return fromIndiviuduals((Individual[]) individuals.toArray(new Individual[individuals.size()]));
	}

	public static Population[] fromIndiviuduals(Individual ...individuals) {
		final Population[] ret = new Population[individuals.length];
		for (int i = 0; i < individuals.length; i++) {
			ret[i] = individuals[i].getPopulation();
		}
		return ret;
	}
}
