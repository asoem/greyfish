/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
