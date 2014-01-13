package org.asoem.greyfish.utils.base;

public abstract class InheritableBuilder<T, B extends Builder<T>> implements Builder<T> {
    /**
     * @return this builder itself
     */
    protected abstract B self();

    /**
     * Create a new instance of {@code T} based on this  builder of type {@code B}
     *
     * @return a new instance of {@code T}
     * @throws IllegalStateException if {@link #checkBuilder()} throws one
     */
    @Override
    public final T build() {
        checkBuilder();
        return checkedBuild();
    }

    protected abstract T checkedBuild();

    /**
     * Check if the state of this builder allows a The basic implementation will never throw an exception. Overwritten
     * implementations should call the {@code super} method of their super class unless if their direct base class is
     * not {@link InheritableBuilder}.
     *
     * @throws IllegalStateException if the {@code InheritableBuilder} is in a State which prevents this builder to
     *                               {@link Builder#build()}
     */
    protected void checkBuilder() {
    }
}
