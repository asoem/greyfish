package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 01.10.12
 * Time: 13:27
 */
public final class Initializers {

    private Initializers() {}

    public static <T> Initializer<T> combine(final Initializer<? super T> i1, final Initializer<? super T> i2) {
        return new Initializer<T>() {
            @Override
            public void initialize(T initializable) {
                i1.initialize(initializable);
                i2.initialize(initializable);
            }
        };
    }

    private enum EmptyInitializer implements Initializer<Object> {
        INSTANCE;

        @Override
        public void initialize(Object initializable) {
        }
    }

    @SuppressWarnings("unchecked") // safe
    public static <T> Initializer<T> emptyInitializer() {
        return (Initializer<T>) EmptyInitializer.INSTANCE;
    }
}
