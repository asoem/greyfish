package org.asoem.greyfish.core.individual;

import com.google.common.base.Strings;
import org.asoem.greyfish.lang.HasName;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.awt.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public final class Population implements HasName {

	@Attribute(name="name")
	private final String name;

	@Element(name="color")
	private final Color color;

    public static Population newPopulation(String name, Color color) {
        return new Population(name, color);
    }

    private Population(
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
}
