package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is an implementation of {@link ComponentList} which behaves like an {@link com.google.common.collect.ImmutableList}.
 * Therefore it is guaranteed to be immutable, cannot be subclassed and does not permit null elements.
 *
 * <p><b>Note:</b> This implementation delegates to an {@link com.google.common.collect.ImmutableMap} internally to stepSize up name based retrieval.
 */
public class ImmutableComponentList<E extends AgentComponent> extends ForwardingList<E> implements ComponentList<E> {

    private static final ImmutableComponentList<AgentComponent> EMPTY_COMPONENT_LIST = new ImmutableComponentList<AgentComponent>(ImmutableList.<AgentComponent>of());

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final List<E> listDelegate;

    private final Map<String, E> indexMap;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableComponentList(@ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) List<E> components) {
        listDelegate = ImmutableList.copyOf(components);
        indexMap = Maps.uniqueIndex(listDelegate, new Function<E, String>() {
            @Override
            public String apply(E input) {
                return input.getName();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ImmutableComponentList(ImmutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(this);

        listDelegate = ImmutableList.copyOf(Iterables.transform(list.listDelegate, new Function<E, E>() {
            @Override
            public E apply(@Nullable E e) {
                return (E) cloner.getClone(e, AgentComponent.class);
            }
        }));

        indexMap = Maps.uniqueIndex(listDelegate, new Function<E, String>() {
            @Override
            public String apply(E input) {
                return input.getName();
            }
        });
    }

    @Override
    protected List<E> delegate() {
        return listDelegate;
    }

    public static <E extends AgentComponent> ImmutableComponentList<E> copyOf(Iterable<E> components) {
        checkNotNull(components);
        if (Iterables.size(components) == 0)
            return of();
        else
            return new ImmutableComponentList<E>(ImmutableList.copyOf(components));
    }

    @Nullable
    @Override
    public <T extends E> T find(final String name, final Class<T> clazz) {

        final E element = indexMap.get(name);
        if (element == null)
            throw new NoSuchElementException("Couldn't find " + clazz + " with name '" + name + "'. Possible candidates are: " + Joiner.on(", ").join(indexMap.keySet()));

        return clazz.cast(element);
        /*
        final E element = Iterables.find(listDelegate, new Predicate<E>() {
            @Override
            public boolean apply(@Nullable E e) {
                assert e != null;
                return Objects.equal(e.getName(), name) && clazz.isInstance(e);
            }
        }, null);
        if (element != null)
            return clazz.cast(element);
        else
            throw new NoSuchElementException("Couldn't find " + clazz + " with name " + name);
            */
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableComponentList<E>(this, cloner);
    }

    @SuppressWarnings("unchecked")
    public static <E extends AgentComponent> ImmutableComponentList<E> of() {
        return (ImmutableComponentList<E>) EMPTY_COMPONENT_LIST;
    }
}
