package org.asoem.greyfish.lang;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 19.01.11
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
    protected abstract T self();
}
