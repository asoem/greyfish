package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import org.asoem.greyfish.core.eval.*;
import org.asoem.greyfish.core.eval.impl.CachedGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.eval.impl.DefaultGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.eval.impl.JEXLEvaluator;

/**
 * User: christoph
 * Date: 14.11.11
 * Time: 13:28
 */
public class EvalModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Evaluator.class).to(JEXLEvaluator.class);
        bind(GreyfishVariableAccessorFactory.class).toInstance(
                new CachedGreyfishVariableAccessorFactory(
                        new DefaultGreyfishVariableAccessorFactory()));
        requestStaticInjection(GreyfishVariableFactory.class);
        requestStaticInjection(GreyfishExpressionFactory.class);
    }
}
