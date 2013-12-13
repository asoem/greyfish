package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.EvaluatorFactory;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.eval.GreyfishVariableFactory;
import org.asoem.greyfish.core.eval.impl.CommonsJEXLEvaluator;
import org.asoem.greyfish.core.utils.AgentComponentClassFinder;
import org.asoem.greyfish.core.utils.AnnotatedAgentComponentClassFinder;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;

import static com.google.common.base.Preconditions.checkNotNull;

public class CoreModule extends AbstractModule {

    private final RandomGenerator randomGenerator;

    public CoreModule() {
        this.randomGenerator = RandomGenerators.rng();
    }

    public CoreModule(final RandomGenerator randomGenerator) {
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
            public Evaluator createEvaluator(final String expression) {
                return new CommonsJEXLEvaluator(expression);
            }
        });
        requestStaticInjection(GreyfishVariableFactory.class);
        requestStaticInjection(GreyfishExpressionFactoryHolder.class);

        bind(RandomGenerator.class).toInstance(randomGenerator);
        requestStaticInjection(RandomGenerators.class);
    }
}
