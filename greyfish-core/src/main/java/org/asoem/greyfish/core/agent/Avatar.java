package org.asoem.greyfish.core.agent;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Avatar<A extends SpatialAgent<A, P, C>, S extends SpatialSimulation2D<A, ?>, P extends Object2D, C extends BasicSimulationContext<S, A>>
        extends ForwardingSpatialAgent<A, P, C>
        implements Serializable {

    private final SpatialAgent<A, P, C> delegate;
    private P projection;

    public Avatar(final SpatialAgent<A, P, C> delegate) {
        this(delegate, null);
    }

    public Avatar(final SpatialAgent<A, P, C> delegate, final P projection) {
        this.delegate = checkNotNull(delegate);
        this.projection = checkNotNull(projection);
    }

    @Override
    protected SpatialAgent<A, P, C> delegate() {
        return delegate;
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
        return new SerializedForm<>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @Override
    public Optional<C> getContext() {
        return delegate().getContext();
    }

    private static class SerializedForm<A extends SpatialAgent<A, P, C>, S extends SpatialSimulation2D<A, ?>, P extends Object2D, C extends BasicSimulationContext<S, A>> implements Serializable {
        private final SpatialAgent<A, P, C> delegate;
        private final P projection;

        public SerializedForm(final Avatar<A, S, P, C> avatar) {
            this.delegate = avatar.delegate;
            this.projection = avatar.projection;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new Avatar<>(delegate, projection);
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }
}
