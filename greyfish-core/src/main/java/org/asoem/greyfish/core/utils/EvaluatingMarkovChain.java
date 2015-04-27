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

package org.asoem.greyfish.core.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.asoem.greyfish.core.eval.Expression;
import org.asoem.greyfish.core.eval.ExpressionFactory;
import org.asoem.greyfish.core.eval.VariableResolver;
import org.asoem.greyfish.core.eval.VariableResolvers;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.math.ImmutableMarkovChain;
import org.asoem.greyfish.utils.math.MarkovChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.math.RandomGenerators.rng;


public class EvaluatingMarkovChain<S> implements MarkovChain<S> {

    private static final Logger logger = LoggerFactory.getLogger(ImmutableMarkovChain.class);

    private final Table<S, S, Expression> markovMatrix;
    private static final Pattern PATTERN = Pattern.compile("^\\s*([\\w\\s\\d_]+)\\s*->\\s*([\\w\\s\\d_]+)\\s*:\\s*(\\S+)\\s*$");

    private EvaluatingMarkovChain(final ChainBuilder<S> builder) {
        this.markovMatrix = ImmutableTable.copyOf(builder.table);
    }

    /**
     * Make a transition to the next state as defined by the markovMatrix
     *
     * @param state    the current state
     * @param resolver the context for the transition probability evaluation
     * @return the next state
     */
    public S apply(final S state, final VariableResolver resolver) {
        checkNotNull(state, "State must not be null");

        if (!markovMatrix.containsRow(state)) {
            if (markovMatrix.containsColumn(state)) {
                logger.debug("State is (implicitly) just self referent: {}", state);
                return state;
            } else {
                throw new IllegalArgumentException("State '" + state + "' does not match any of the defined states in set {" + Joiner.on(", ").join(getStates()) + "}");
            }
        }


        final Map<S, Expression> row = markovMatrix.row(state);

        if (row.isEmpty()) {
            logger.debug("No outgoing transition has been defined for state '{}'", state);
            return state;
        }

        double sum = 0;
        final double rand = rng().nextDouble();
        for (final Map.Entry<S, Expression> cell : row.entrySet()) {
            sum += cell.getValue().evaluate(resolver).asDouble();
            if (sum > rand) {
                return cell.getKey();
            }
        }

        if (sum < 1) {
            logger.debug("The sum of transition probabilities for state {} are < 1: {}." +
                    "Reminding fraction will be used by the identity transition", state, sum);
        } else if (sum > 1) {
            logger.warn("The sum of transition probabilities for state {} are > 1: {}." +
                    "Some states might never be reached.", state, sum);
        }

        return state;
    }

    public static <S> ChainBuilder<S> builder(final ExpressionFactory expressionFactory) {
        return new ChainBuilder<S>(expressionFactory);
    }

    public static EvaluatingMarkovChain<String> parse(final String rule, final ExpressionFactory factory) {
        checkNotNull(rule);
        checkNotNull(factory);

        final ChainBuilder<String> builder = builder(factory);

        final Splitter splitter = Splitter.onPattern("\r?\n|;").trimResults();
        final Iterable<String> lines = splitter.split(rule);

        for (final String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            final Matcher matcher = PATTERN.matcher(line);
            if (matcher.matches()) {
                final String state1 = matcher.group(1).trim();
                final String state2 = matcher.group(2).trim();
                final String p = matcher.group(3).trim();

                builder.put(state1, state2, p);
            } else {
                throw new IllegalArgumentException("Rule has errors at " + line);
            }
        }

        return builder.build();
    }

    @Override
    public Set<S> getStates() {
        return Sets.union(markovMatrix.rowKeySet(), markovMatrix.columnKeySet());
    }

    @Override
    public S apply(final S state) {
        return apply(state, VariableResolvers.emptyResolver());
    }

    public static class ChainBuilder<S> implements Builder<EvaluatingMarkovChain<S>> {

        private final Table<S, S, Expression> table = HashBasedTable.create();
        private final ExpressionFactory expressionFactory;

        public ChainBuilder(final ExpressionFactory expressionFactory) {

            this.expressionFactory = checkNotNull(expressionFactory);
        }

        public ChainBuilder<S> put(final S state, final S nextState, final String expression) {
            checkNotNull(state);
            checkNotNull(nextState);
            checkNotNull(expression);

            table.put(state, nextState, expressionFactory.compile(expression));
            return this;
        }

        @Override
        public EvaluatingMarkovChain<S> build() {
            return new EvaluatingMarkovChain<S>(this);
        }
    }

    public String toRule() {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<S, Map<S, Expression>> entry : markovMatrix.rowMap().entrySet()) {
            for (final Map.Entry<S, Expression> doubleEntry : entry.getValue().entrySet()) {
                builder.append(entry.getKey());
                builder.append(" -> ");
                builder.append(doubleEntry.getKey());
                builder.append(" : ");
                builder.append(doubleEntry.getValue().getExpression());
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return toRule();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EvaluatingMarkovChain)) {
            return false;
        }

        final EvaluatingMarkovChain that = (EvaluatingMarkovChain) o;

        return markovMatrix.equals(that.markovMatrix);

    }

    @Override
    public int hashCode() {
        return markovMatrix.hashCode();
    }
}
