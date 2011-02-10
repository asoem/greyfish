package org.asoem.greyfish.core.individual;

/**
 * User: christoph
 * Date: 28.01.11
 * Time: 12:02
 */
public interface GFComponentHolder {
    Iterable<? extends GFComponent> getComponents();
}
