package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.ComponentList;
import org.asoem.greyfish.core.individual.MutableComponentList;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 15:21
 */
public class MutableChromosome<E extends Gene<?>> extends AbstractChromosome<E> {

    @Element(name = "genes")
    private final ComponentList<E> delegate;

    @SimpleXMLConstructor
    private MutableChromosome(@Element(name = "genes") ComponentList<E> genes) {
        delegate = genes;
    }

    public MutableChromosome(Iterable<? extends E> genome) {
        delegate = new MutableComponentList<E>(genome);
    }

    protected MutableChromosome(MutableChromosome<E> parent, final DeepCloner cloner) {
        cloner.addClone(this);
        delegate = new MutableComponentList<E>(Iterables.transform(parent.delegate, new Function<E, E>() {
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
        return new MutableChromosome<E>(this, cloner);
    }
}
