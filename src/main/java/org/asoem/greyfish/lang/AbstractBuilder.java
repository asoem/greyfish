package org.asoem.greyfish.lang;

/**
 * User: christoph
 * Date: 19.01.11
 * Time: 14:04
 */
public abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
    protected abstract T self();
    protected void checkBuilder() throws IllegalStateException {}
    protected final T checkedSelf() throws IllegalStateException { checkBuilder(); return self(); }
}
