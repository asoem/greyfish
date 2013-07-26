package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.space.Object2D;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 16:18
 */
public class Avatar<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, ?>, P extends Object2D> extends ForwardingSpatialAgent<A, S, P> implements Serializable {

    private final SpatialAgent<A, S, P> delegate;
    private P projection;

    public Avatar(final SpatialAgent<A, S, P> delegate) {
        this(delegate, null);
    }

    @SuppressWarnings("unchecked") // casting a clone is safe
    private Avatar(final Avatar<A, S, P> avatar, final DeepCloner cloner) {
        cloner.addClone(avatar, this);
        this.delegate = cloner.getClone(avatar.delegate);
        this.projection = avatar.projection;
    }

    public Avatar(final SpatialAgent<A, S, P> delegate, final P projection) {
        this.delegate = checkNotNull(delegate);
        this.projection = checkNotNull(projection);
    }

    @Override
    protected SpatialAgent<A, S, P> delegate() {
        return delegate;
    }

    @Override
    public Avatar<A, S, P> deepClone(final DeepCloner cloner) {
        return new Avatar<A, S, P>(this, cloner);
    }

    @Override
    public void setProjection(final P projection) {
        this.projection = checkNotNull(projection);
    }

    @Override
    public P getProjection() {
        return projection;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Avatar)) return false;

        final Avatar avatar = (Avatar) o;

        if (delegate != null ? !delegate.equals(avatar.delegate) : avatar.delegate != null) return false;
        if (projection != null ? !projection.equals(avatar.projection) : avatar.projection != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = delegate != null ? delegate.hashCode() : 0;
        result = 31 * result + (projection != null ? projection.hashCode() : 0);
        return result;
    }

    private Object writeReplace() {
        return new SerializedForm<A, S, P>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, ?>, P extends Object2D> implements Serializable {
        private final SpatialAgent<A, S, P> delegate;
        private final P projection;

        public SerializedForm(final Avatar<A, S, P> avatar) {
            this.delegate = avatar.delegate;
            this.projection = avatar.projection;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new Avatar<A, S, P>(delegate, projection);
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }
}
