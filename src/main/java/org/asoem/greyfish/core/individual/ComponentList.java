package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.DeepCloneable;

import java.util.List;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 11:16
 *
 * <p>A list to store {@link GFComponent}s and wrap common operations.
 * Implementations should not permit {@code null} elements or duplicates and should block addition of new components,
 * if it has the same name as a different component in this list.</p>
 */
public interface ComponentList<E extends GFComponent> extends List<E>, DeepCloneable {
    <T extends E> T get(String name, Class<T> clazz);
}
