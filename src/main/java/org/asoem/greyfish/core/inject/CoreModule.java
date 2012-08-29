package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.eval.GreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.eval.GreyfishVariableFactory;
import org.asoem.greyfish.core.eval.impl.CachedGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.eval.impl.CommonsJEXLEvaluator;
import org.asoem.greyfish.core.eval.impl.DefaultGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.io.persistence.SimpleXMLPersister;
import org.asoem.greyfish.core.utils.AgentComponentClassFinder;
import org.asoem.greyfish.core.utils.AnnotatedAgentComponentClassFinder;
import org.asoem.greyfish.utils.persistence.Persister;

/**
 * User: christoph
 * Date: 11.01.12
 * Time: 14:37
 */
public class CoreModule extends AbstractModule {
    @Override
    protected void configure() {
        // Utils
        bind(AgentComponentClassFinder.class)
                .to(AnnotatedAgentComponentClassFinder.class).asEagerSingleton();

        // Persister
        bind(Persister.class).to(SimpleXMLPersister.class);

        // GreyfishExpression
        bind(Evaluator.class).to(CommonsJEXLEvaluator.class);
        bind(GreyfishVariableAccessorFactory.class).toInstance(
                new CachedGreyfishVariableAccessorFactory(
                        new DefaultGreyfishVariableAccessorFactory()));
        requestStaticInjection(GreyfishVariableFactory.class);
        requestStaticInjection(GreyfishExpressionFactoryHolder.class);
    }
}
