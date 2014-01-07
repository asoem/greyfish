package org.asoem.greyfish.core.agent;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Avatar<A extends SpatialAgent<A, C, P>, S extends SpatialSimulation2D<A, ?>, P extends Object2D, C extends BasicSimulationContext<S, A>>
        extends ForwardingSpatialAgent<A, C, P>
        implements Serializable {

    private final SpatialAgent<A, C, P> delegate;
    private P projection;

    public Avatar(final SpatialAgent<A, C, P> delegate) {
        this(delegate, null);
    }

    public Avatar(final SpatialAgent<A, C, P> delegate, final P projection) {
        this.delegate = checkNotNull(delegate);
        this.projection = checkNotNull(projection);
    }

    @Override
    protected SpatialAgent<A, C, P> delegate() {
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

    @Override
    public <T> T getPropertyValue(final String traitName, final Class<T> valueType) {
        return delegate().getPropertyValue(traitName, valueType);
    }

    @Override
    public Set<Integer> getParents() {
        return delegate().getParents();
    }

    @Override
    public Iterable<ACLMessage<A>> getMessages(final MessageTemplate template) {
        return delegate().getMessages(template);
    }

    private static class SerializedForm<A extends SpatialAgent<A, C, P>, S extends SpatialSimulation2D<A, ?>, P extends Object2D, C extends BasicSimulationContext<S, A>> implements Serializable {
        private final SpatialAgent<A, C, P> delegate;
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
