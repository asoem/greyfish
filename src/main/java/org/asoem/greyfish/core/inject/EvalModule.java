package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
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
        bind(GreyfishVariableAccessorFactory.class).toProvider(new Provider<GreyfishVariableAccessorFactory>() {
            @Override
            public GreyfishVariableAccessorFactory get() {
                return new CachedGreyfishVariableAccessorFactory(new DefaultGreyfishVariableAccessorFactory());
            }
        });
        requestStaticInjection(GreyfishVariableFactory.class);
        requestStaticInjection(GreyfishExpressionFactory.class);
    }
}
