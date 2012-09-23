package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import org.asoem.greyfish.utils.base.DeepCloneable;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 11:16
 *
 * <p>A list to store {@link AgentComponent}s and wrap common operations.
 * Implementations should not permit {@code null} elements or duplicates and should block addition of new components,
 * if it has the same name as a different component in this list.</p>
 */
public interface ComponentList<E extends AgentComponent> extends List<E>, DeepCloneable {
    /**
     * Find the first element which is named {@code name} and is an instance of {@code clazz}.
     * @param name the name of the {@code AgentComponent} to search for
     * @param clazz the clazz of the {@code AgentComponent} to search for
     * @param <T> the type of clazz
     * @return the matching element in this {@code ComponentList}
     * @throws java.util.NoSuchElementException if none could be found
     * @throws ClassCastException if element could not be cast to {@code clazz}
     */
    <T extends E> T find(String name, Class<T> clazz) throws NoSuchElementException, ClassCastException;

    E find(Predicate<? super E> predicate) throws NoSuchElementException;

    E find(Predicate<? super E> predicate, E defaultValue) throws NoSuchElementException;
}
