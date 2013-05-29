package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.core.eval.*;
import org.asoem.greyfish.core.eval.impl.CachedGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.eval.impl.CommonsJEXLEvaluator;
import org.asoem.greyfish.core.eval.impl.DefaultGreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.utils.AgentComponentClassFinder;
import org.asoem.greyfish.core.utils.AnnotatedAgentComponentClassFinder;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 11.01.12
 * Time: 14:37
 */
public class CoreModule extends AbstractModule {

    private final RandomGenerator randomGenerator;

    public CoreModule() {
        this.randomGenerator = RandomGenerators.rng();
    }

    public CoreModule(RandomGenerator randomGenerator) {
        checkNotNull(randomGenerator);
        this.randomGenerator = randomGenerator;
    }

    @Override
    protected void configure() {
        // Utils
        bind(AgentComponentClassFinder.class)
                .to(AnnotatedAgentComponentClassFinder.class).asEagerSingleton();

        // Persister
        bind(Persister.class).toInstance(Persisters.javaSerialization());

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
        requestStaticInjection(RandomGenerators.class);
    }
}
