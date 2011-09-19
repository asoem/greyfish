package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.DeepCloneable;

import java.util.List;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 11:16
 */
public interface ComponentList<E extends GFComponent> extends List<E> {
    <T extends E> T get(String name, Class<T> clazz);
}
