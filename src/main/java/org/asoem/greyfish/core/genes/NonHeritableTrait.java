package org.asoem.greyfish.core.genes;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

/**
 * User: christoph
 * Date: 24.04.13
 * Time: 14:32
 */
public class NonHeritableTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> {

    private final T value;

    private NonHeritableTrait(NonHeritableTrait<A, T> trait, DeepCloner cloner) {
        this.value = trait.value;
    }

    @Override
    public TypeToken<T> getValueType() {
        return (Class<? super T>) value.getClass();
    }

    @Override
    public T createInitialValue() {
        return value;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new NonHeritableTrait<A, T>(this, cloner);
    }

    @Override
    public T get() {
        return value;
    }
}
