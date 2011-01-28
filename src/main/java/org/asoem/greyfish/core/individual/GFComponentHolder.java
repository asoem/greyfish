package org.asoem.greyfish.core.individual;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 28.01.11
 * Time: 12:02
 * To change this template use File | Settings | File Templates.
 */
public interface GFComponentHolder {
    Iterable<? extends GFComponent> getComponents();
}
