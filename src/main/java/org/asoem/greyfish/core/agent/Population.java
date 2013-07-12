package org.asoem.greyfish.core.agent;

import java.awt.*;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used to identify agents as being clones of the same prototype. Can be shared.
 */
public class Population implements Comparable<Population>, Serializable {

	private final String name;
	private final Color color;

    public Population(String name) {
        this(name, Color.black);
    }

    public Population(String name, Color color) {
        this.name = checkNotNull(name);
        this.color = checkNotNull(color);
    }

    public Color getColor() {
		return color;
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

        if (!color.equals(that.color)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + color.hashCode();
        return result;
    }

    @Override
    public int compareTo(Population o) {
        return name.compareTo(o.name);
    }

    public static Population named(String asexualPopulation) {
        return newPopulation(asexualPopulation, Color.black);
    }

    public static Population newPopulation(String name, Color color) {
        return new Population(name, color);
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (name == null || color == null)
            throw new InvalidObjectException("Neither name nor color must be null");
    }

    private static final long serialVersionUID = 0;
}
