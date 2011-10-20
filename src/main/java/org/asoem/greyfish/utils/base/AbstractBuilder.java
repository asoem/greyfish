package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 19.01.11
 * Time: 14:04
 */
public abstract class AbstractBuilder<E, T extends AbstractBuilder<? super E, T>> implements Builder<E> {
    /**
     * @return this builder itself
     */
    protected abstract T self();

    /**
     * Create a new instance of {@code E} based on this  builder of type {@code T}
     * @return a new instance of {@code E}
     * @throws IllegalStateException if {@link #checkBuilder()} throws one
     */
    @Override
    public final E build() throws IllegalStateException {
        checkBuilder();
        return checkedBuild();
    }

    protected abstract E checkedBuild();

    /**
     * Check if the state of this builder allows a
     * The basic implementation will never throw an exception.
     * Overwritten implementations should call the {@code super} method of their super class
     * unless if their direct base class is not {@link AbstractBuilder}.
     * @throws IllegalStateException if the {@code AbstractBuilder} is in a State
     * which prevents this builder to {@link #build()}
     */
    protected void checkBuilder() throws IllegalStateException {}
}
