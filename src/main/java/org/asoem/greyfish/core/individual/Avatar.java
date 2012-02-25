package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 16:18
 */
public class Avatar extends ForwardingAgent {

    @Element(name = "delegate")
    private final Agent delegate;

    @Element(required = false)
    private Object2D projection;

    public Avatar(@Element(name = "delegate") Agent delegate) {
        this.delegate = checkNotNull(delegate);
    }

    private Avatar(Avatar avatar, DeepCloner cloner) {
        cloner.addClone(this);
        this.delegate = cloner.cloneField(avatar.delegate, Agent.class);
        this.projection = avatar.projection;
    }

    @Override
    protected Agent delegate() {
        return delegate;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new Avatar(this, cloner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Avatar avatar = (Avatar) o;

        return delegate.equals(avatar.delegate);

    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public void setProjection(Object2D projection) {
        this.projection = projection;
    }

    @Override
    public Object2D getProjection() {
        return projection;
    }
}
