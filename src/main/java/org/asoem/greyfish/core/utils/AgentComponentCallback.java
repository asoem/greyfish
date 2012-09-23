package org.asoem.greyfish.core.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.utils.base.Callback;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: christoph
 * Date: 17.09.12
 * Time: 20:15
 */
public abstract class AgentComponentCallback<C extends AgentComponent, R> implements Callback<C, R> {

    protected static <E extends GeneComponent<?>> Function<Agent, E> traitAccessor(final String name, final Class<E> clazz) {
        return new Function<Agent, E>() {
            public AtomicInteger index = new AtomicInteger(-1);

            @Override
            public E apply(Agent input) {
                int i = index.get();
                if (i == -1) {
                    final int newValue = Iterables.indexOf(input.getGeneComponentList(), new Predicate<GeneComponent<?>>() {
                        @Override
                        public boolean apply(GeneComponent<?> input) {
                            return input.getName().equals(name);
                        }
                    });
                    if (newValue == -1)
                        throw new IllegalArgumentException();
                    index.set(newValue);
                    i = newValue;
                }
                return clazz.cast(input.getGeneComponentList().get(i));
            }
        };
    }
}
