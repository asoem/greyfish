package org.asoem.greyfish.core.traits;

import com.google.common.collect.Ordering;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 24.04.13
 * Time: 14:32
 */
public class AcquiredDiscreteTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> implements DiscreteTrait<A, T> {

    private final TypeToken<T> typeToken;
    private final Callback<? super AcquiredDiscreteTrait<A, T>, T> initializationKernel;
    private final Set<T> states;
    private T value;
    private final Ordering<T> ordering;

    private AcquiredDiscreteTrait(AcquiredDiscreteTrait<A, T> trait, DeepCloner cloner) {
        this.value = trait.value;
        this.typeToken = trait.typeToken;
        this.initializationKernel = trait.initializationKernel;
        this.states = trait.states;
        ordering = trait.ordering;
    }

    @Override
    public TypeToken<T> getValueType() {
        return typeToken;
    }

    @Override
    public T createInitialValue() {
        return Callbacks.call(initializationKernel, this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new AcquiredDiscreteTrait<A, T>(this, cloner);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        checkArgument(states.contains(value));
        this.value = value;
    }

    @Override
    public boolean isHeritable() {
        return false;
    }

    @Override
    public Set<T> getStates() {
        return states;
    }

    @Override
    public int size() {
        return getStates().size();
    }

    @Override
    public Ordering<T> getOrdering() {
        return ordering;
    }
}
