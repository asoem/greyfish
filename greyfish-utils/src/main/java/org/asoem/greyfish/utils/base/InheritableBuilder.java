package org.asoem.greyfish.utils.base;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class InheritableBuilder<T, B extends Builder<T>> implements Builder<T> {

    @Nullable
    private Verification verification;

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
        verify();
        return checkedBuild();
    }

    private void verify() {
        @Nullable Verification toVerify = this.verification;
        while (toVerify != null) {
            toVerify.verify();
            toVerify = toVerify.next;
        }
    }

    protected abstract T checkedBuild();

    /**
     * Check if the state of this builder allows a The basic implementation will never throw an exception. Overwritten
     * implementations should call the {@code super} method of their super class unless if their direct base class is
     * not {@link InheritableBuilder}.
     *
     * @throws IllegalStateException if the {@code InheritableBuilder} is in a State which prevents this builder to
     *                               {@link Builder#build()}
     * @deprecated Use {@link #addVerification(org.asoem.greyfish.utils.base.InheritableBuilder.Verification)}. Method
     * will be removed soon.
     */
    @Deprecated
    protected void checkBuilder() {
    }

    protected final void addVerification(final Verification verificationToAdd) {
        checkNotNull(verificationToAdd);
        if (this.verification != null) {
            verificationToAdd.next = this.verification;
        }
        this.verification = verificationToAdd;
    }

    /**
     * A verification object for verifying the state of the builder.
     */
    protected abstract class Verification {
        private Verification next;

        /**
         * This method should verify the builder of a specific subclass by throwing an {@link
         * java.lang.IllegalStateException} if something prevents the builder fom building a valid object.
         */
        protected abstract void verify();
    }
}

