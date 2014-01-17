package org.asoem.greyfish.core.agent;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Used to identify agents as being clones of the same prototype. Can be shared.
 */
public class PrototypeGroup implements Comparable<PrototypeGroup>, Serializable {

    private final String name;

    private PrototypeGroup(final String name) {
        this.name = name;
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
    public int compareTo(final PrototypeGroup o) {
        return name.compareTo(o.name);
    }

    public static PrototypeGroup named(final String asexualPopulation) {
        return new PrototypeGroup(asexualPopulation);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PrototypeGroup that = (PrototypeGroup) o;

        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private void readObject(final ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (name == null) {
            throw new InvalidObjectException("Name must be null");
        }
    }

    private static final long serialVersionUID = 0;
}
