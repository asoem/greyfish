package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;


/**
 * A decorator class for {@code Agent}s. The goal of this class is to remove the equality ({@link #equals(Object)}) of the decorated {@code Agent},
 * so that the same {@code Agent} can be used as a key in a {@code HashMap}
 */
public class Placeholder extends ForwardingAgent {

    @Element(name = "delegate")
    private final Agent delegate;

    public Placeholder(@Element(name = "delegate") Agent prototype) {
        this.delegate = prototype;
    }

    public Placeholder(Placeholder placeholder, DeepCloner cloner) {
        cloner.addClone(this);
        delegate = cloner.cloneField(placeholder.delegate, Agent.class);
    }

    public static Placeholder newInstance(Agent prototype) {
        return new Placeholder(prototype);
    }

    @Override
    public Agent delegate() {
        return delegate;
    }

    public Agent getAgent() {
        return delegate();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new Placeholder(this, cloner);
    }
}
