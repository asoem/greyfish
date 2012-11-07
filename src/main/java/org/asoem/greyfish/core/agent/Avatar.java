package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.space.MotionObject2D;

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
public class Avatar extends ForwardingAgent implements Serializable {

    private final Agent delegate;
    private MotionObject2D projection;

    public Avatar(Agent delegate) {
        this(delegate, null);
    }

    private Avatar(Avatar avatar, DeepCloner cloner) {
        cloner.addClone(avatar, this);
        this.delegate = cloner.getClone(avatar.delegate, Agent.class);
        this.projection = avatar.projection;
    }

    public Avatar(Agent delegate, MotionObject2D projection) {
        this.delegate = checkNotNull(delegate);
        this.projection = checkNotNull(projection);
    }

    @Override
    protected Agent delegate() {
        return delegate;
    }

    @Override
    public Avatar deepClone(DeepCloner cloner) {
        return new Avatar(this, cloner);
    }

    @Override
    public void setProjection(MotionObject2D projection) {
        this.projection = checkNotNull(projection);
    }

    @Override
    public MotionObject2D getProjection() {
        return projection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Avatar avatar = (Avatar) o;

        return delegate.equals(avatar.delegate)
                && projection.equals(avatar.projection);
    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + projection.hashCode();
        return result;
    }

    private Object writeReplace() {
        return new SerializedForm(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm implements Serializable {
        private final Agent delegate;
        private final MotionObject2D projection;

        public SerializedForm(Avatar avatar) {
            this.delegate = avatar.delegate;
            this.projection = avatar.projection;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new Avatar(delegate, projection);
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }
}
