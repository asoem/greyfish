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
public class Avatar<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, ?, P>, P extends Object2D> extends ForwardingSpatialAgent<A, S, P> implements Serializable {

    private final SpatialAgent<A, S, P> delegate;
    private P projection;

    public Avatar(SpatialAgent<A, S, P> delegate) {
        this(delegate, null);
    }

    @SuppressWarnings("unchecked") // casting a clone is safe
    private Avatar(Avatar<A, S, P> avatar, DeepCloner cloner) {
        cloner.addClone(avatar, this);
        this.delegate = (SpatialAgent<A, S, P>) cloner.getClone(avatar.delegate);
        this.projection = avatar.projection;
    }

    public Avatar(SpatialAgent<A, S, P> delegate, P projection) {
        this.delegate = checkNotNull(delegate);
        this.projection = checkNotNull(projection);
    }

    @Override
    protected SpatialAgent<A, S, P> delegate() {
        return delegate;
    }

    @Override
    public Avatar<A, S, P> deepClone(DeepCloner cloner) {
        return new Avatar<A, S, P>(this, cloner);
    }

    @Override
    public void setProjection(P projection) {
        this.projection = checkNotNull(projection);
    }

    @Override
    public P getProjection() {
        return projection;
    }

    private Object writeReplace() {
        return new SerializedForm<A, S, P>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, ?, P>, P extends Object2D> implements Serializable {
        private final SpatialAgent<A, S, P> delegate;
        private final P projection;

        public SerializedForm(Avatar<A, S, P> avatar) {
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
