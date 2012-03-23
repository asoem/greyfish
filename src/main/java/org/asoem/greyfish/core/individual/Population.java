package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.base.HasName;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.awt.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used to identify agents as being clones of the same prototype. Can be shared.
 */
public class Population implements HasName, Comparable<Population> {

	@Attribute(name="name")
	private final String name;

	@Element(name="color")
	private final Color color;

    public static Population newPopulation(String name, Color color) {
        return new Population(name, color);
    }

    public Population(
            @Attribute(name="name") String name,
            @Element(name="color") Color color) {
        checkNotNull(name);
        this.name = name;
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
    public int compareTo(Population o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Population that = (Population) o;

        return color.equals(that.color) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + color.hashCode();
        return result;
    }

    public static Population named(String asexualPopulation) {
        return newPopulation(asexualPopulation, Color.black);
    }
}
