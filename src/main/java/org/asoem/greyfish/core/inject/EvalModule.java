package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.asoem.greyfish.core.eval.*;

/**
 * User: christoph
 * Date: 14.11.11
 * Time: 13:28
 */
public class EvalModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Evaluator.class).to(MvelEvaluator.class);
    }

    @Provides
    GreyfishVariableAccessorFactory provideGreyfishVariableAccessorFactory() {
        return new CachedGreyfishVariableAccessorFactory(new DefaultGreyfishVariableAccessorFactory());
    }
}
