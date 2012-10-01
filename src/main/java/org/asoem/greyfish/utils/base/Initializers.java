package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 01.10.12
 * Time: 13:27
 */
public class Initializers {
    private enum EmptyInitializer implements Initializer<Object> {
        INSTANCE;

        @Override
        public void initialize(Object initializable) {
        }
    }

    public static <T> Initializer<T> emptyInitializer() {
        return (Initializer<T>) EmptyInitializer.INSTANCE;
    }
}
