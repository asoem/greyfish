package org.asoem.greyfish.core.traits;

import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 24.04.13
 * Time: 14:32
 */
public class AcquiredTrait<A extends Agent<A, ?>, T extends Comparable<T>> extends AbstractTrait<A, T> implements AgentTrait<A,T> {

    private final TypeToken<T> typeToken;
    private final Callback<? super AcquiredTrait<A, T>, T> initializationKernel;
    private T value;
    private final Range<T> range;

    private AcquiredTrait(AcquiredTrait<A, T> trait, DeepCloner cloner) {
        this.value = trait.value;
        this.typeToken = trait.typeToken;
        this.range = trait.range;
        initializationKernel = trait.initializationKernel;
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
        return new AcquiredTrait<A, T>(this, cloner);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        checkArgument(range.contains(value));
        this.value = value;
    }

    @Override
    public boolean isHeritable() {
        return false;
    }

}
