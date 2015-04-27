/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
     * @throws IllegalStateException if something prevents the builder from building a valid object
     */
    @Override
    public final T build() {
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

