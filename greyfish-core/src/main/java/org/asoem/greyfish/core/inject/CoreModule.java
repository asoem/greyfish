package org.asoem.greyfish.core.inject;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.EvaluatorFactory;
import org.asoem.greyfish.core.eval.impl.CommonsJEXLEvaluator;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreModule extends AbstractModule {

    private final RandomGenerator randomGenerator;
    private final EventBus eventBus;

    public CoreModule() {
        this(RandomGenerators.rng());
    }

    public CoreModule(final RandomGenerator randomGenerator) {
        this(randomGenerator, new EventBus());
    }

    public CoreModule(final RandomGenerator randomGenerator, final EventBus eventBus) {
        this.randomGenerator = checkNotNull(randomGenerator);
        this.eventBus = checkNotNull(eventBus);
    }

    @Override
    protected void configure() {
        // Persister
        bind(Persister.class).toInstance(Persisters.javaSerialization());
        bind(EventBus.class).toInstance(eventBus);

        // GreyfishExpression
        bind(EvaluatorFactory.class).toInstance(new EvaluatorFactory() {
            @Override
            public Evaluator createEvaluator(final String expression) {
                return new CommonsJEXLEvaluator(expression);
            }
        });
        //requestStaticInjection(GreyfishVariableFactory.class);
        //requestStaticInjection(GreyfishExpressionFactoryHolder.class);

        bind(RandomGenerator.class).toInstance(randomGenerator);
        requestStaticInjection(RandomGenerators.class);
    }
}
