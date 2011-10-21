package org.asoem.greyfish.core.individual;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.asoem.greyfish.utils.base.HasName;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.awt.*;

import static com.google.common.base.Preconditions.checkArgument;
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
        checkArgument(!Strings.isNullOrEmpty(name));
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

    @Override
    public boolean hasName(String s) {
        return Objects.equal(name, s);
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
}
