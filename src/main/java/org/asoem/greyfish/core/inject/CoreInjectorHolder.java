package org.asoem.greyfish.core.inject;

import com.google.common.base.Supplier;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * User: christoph
 * Date: 14.11.11
 * Time: 13:25
 */
public enum CoreInjectorHolder implements Supplier<Injector> {
    INSTANCE;

    private Injector injector = Guice.createInjector(
            new CoreModule(),
            new EvalModule()
    );

    @Override
    public Injector get() {
        return this.injector;
    }

    public static Injector coreInjector() {
        return INSTANCE.get();
    }
}
