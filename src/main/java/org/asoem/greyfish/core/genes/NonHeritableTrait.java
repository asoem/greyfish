package org.asoem.greyfish.core.genes;

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
public class NonHeritableTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> {
    private final TypeToken<T> typeToken;
    private final Callback<? super NonHeritableTrait<A, T>, Boolean> valueConstraint;
    private final Callback<? super NonHeritableTrait<A, T>, T> initializationKernel;
    private T value;

    private NonHeritableTrait(NonHeritableTrait<A, T> trait, DeepCloner cloner) {
        this.value = trait.value;
        typeToken = trait.typeToken;
        valueConstraint = trait.valueConstraint;
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
        return new NonHeritableTrait<A, T>(this, cloner);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        checkArgument(Callbacks.call(valueConstraint, this));
        this.value = value;
    }
}
