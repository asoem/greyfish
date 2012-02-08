package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.ComponentList;
import org.asoem.greyfish.core.individual.MutableComponentList;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 15:21
 */
public class MutableGenome<E extends Gene<?>> extends AbstractGenome<E> {

    private final ComponentList<E> delegate = new MutableComponentList<E>();

    public MutableGenome() {
    }

    public MutableGenome(Iterable<? extends E> genome) {
        Iterables.addAll(delegate(), genome);
    }

    protected MutableGenome(MutableGenome<E> parent, final DeepCloner cloner) {
        cloner.addClone(this);
        Iterables.addAll(delegate, Iterables.transform(parent.delegate, new Function<E, E>() {
            @SuppressWarnings("unchecked") // its a save downcast
            @Override
            public E apply(@Nullable E e) {
                assert e != null;
                return cloner.cloneField(e, (Class<E>) e.getClass());
            }
        }));
    }

    @Override
    protected ComponentList<E> delegate() {
        return delegate;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableGenome<E>(this, cloner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MutableGenome that = (MutableGenome) o;

        if (!delegate.equals(that.delegate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + delegate.hashCode();
        return result;
    }
}
