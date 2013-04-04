package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.asoem.greyfish.core.eval.*;
import org.asoem.greyfish.core.eval.impl.CachedGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.eval.impl.CommonsJEXLEvaluator;
import org.asoem.greyfish.core.eval.impl.DefaultGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.utils.AgentComponentClassFinder;
import org.asoem.greyfish.core.utils.AnnotatedAgentComponentClassFinder;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persister;

/**
 * User: christoph
 * Date: 11.01.12
 * Time: 14:37
 */
public class CoreModule extends AbstractModule {

    private final RandomGenerator randomGenerator;

    public CoreModule() {
        this.randomGenerator = new Well19937c();
    }

    public CoreModule(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    @Override
    protected void configure() {
        // Utils
        bind(AgentComponentClassFinder.class)
                .to(AnnotatedAgentComponentClassFinder.class).asEagerSingleton();

        // Persister
        bind(Persister.class).to(JavaPersister.class);

        // GreyfishExpression
        bind(EvaluatorFactory.class).toInstance(new EvaluatorFactory() {
            @Override
            public Evaluator createEvaluator(String expression) {
                return new CommonsJEXLEvaluator(expression);
            }
        });
        bind(GreyfishVariableAccessorFactory.class).toInstance(
                new CachedGreyfishVariableAccessorFactory(
                        new DefaultGreyfishVariableAccessorFactory()));
        requestStaticInjection(GreyfishVariableFactory.class);
        requestStaticInjection(GreyfishExpressionFactoryHolder.class);

        bind(RandomGenerator.class).toInstance(randomGenerator);
        requestStaticInjection(RandomUtils.class);
    }
}
