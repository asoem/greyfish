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

package org.asoem.greyfish.utils.math;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.base.Builder;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default implementation of {@code MarkovChain}. <p>For convenience, this chain adds implicit identity transitions
 * (A -> A) for states, which are defined, but their transition probabilities do not sum up to 1.0.</p>
 *
 * @param <S>
 */
public final class ImmutableMarkovChain<S> implements MarkovChain<S> {

    private final Table<S, S, Double> transitionTable;
    private final RandomGenerator rng;

    private ImmutableMarkovChain(final Table<S, S, Double> transitionTable, final RandomGenerator rng) {
        this.transitionTable = transitionTable;
        this.rng = rng;
    }

    /**
     * Get the next state for given {@code state}. <p>If the transition probabilities for the state do not sum up to 1.0
     * (or rule with state as origin is given at all), an implicit identity transition (A -> A) for the remaining
     * fraction is assumed. If the given state was neither used as a origin or destination in the transition table, an
     * {@code IllegalArgumentException} will be thrown.</p>
     *
     * @param state the current state
     * @return the next state
     * @throws IllegalArgumentException if the given state is not defined in the chains transition table
     */
    @Override
    public S apply(final S state) {
        checkNotNull(state, "State must not be null");

        if (!transitionTable.containsRow(state)) {
            if (transitionTable.containsColumn(state)) {
                return state;
            } else {
                final String message = "State '" + state
                        + "' does not match any of the defined states in set {"
                        + Joiner.on(", ").join(getStates()) + "}";
                throw new IllegalArgumentException(message);
            }
        }


        final Map<S, Double> row = transitionTable.row(state);

        if (row.isEmpty()) {
            return state;
        }

        double sum = 0;
        final double rand = rng().nextDouble();
        for (final Map.Entry<S, Double> cell : row.entrySet()) {
            sum += cell.getValue();
            if (sum > rand) {
                return cell.getKey();
            }
        }

        return state;
    }

    @Override
    public Set<S> getStates() {
        return Sets.union(transitionTable.rowKeySet(), transitionTable.columnKeySet());
    }

    public static <S> ChainBuilder<S> builder() {
        return new ChainBuilder<S>();
    }

    /**
     * Parse a {@code rule} into a new {@code ImmutableMarkovChain<String>}. <p/> <p>The grammar is a follows:
     * <pre>
     * Chain: Transition (Separator Transition)*
     * Transition: State '->' State ':' Probability
     * Separator: [\r\n;]+
     * State: \w+
     * Probability: 0 | 0?\.\d+ | 1(\.0)?
     * </pre>
     * Example:
     * <pre>
     * A -> B : 1.0; B -> C : 1.0
     * </pre>
     * </p>
     *
     * @param rule A chain rule
     * @return A new ImmutableMarkovChain with {@code String} states
     */
    public static ImmutableMarkovChain<String> parse(final String rule) {
        final ChainBuilder<String> builder = builder();

        final Splitter splitter = Splitter.onPattern("[\r\n;]+").trimResults();
        final Iterable<String> lines = splitter.split(rule);

        final Pattern pattern = Pattern.compile(
                "^"
                        + "(\\w+)"
                        + "\\s*->\\s*"
                        + "(\\w+)"
                        + "\\s*:\\s*"
                        + "(0|0?\\.\\d+|1(?:\\.0)?)"
                        + "$");
        for (final String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            final Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                final String state1 = matcher.group(1).trim();
                final String state2 = matcher.group(2).trim();
                final String p = matcher.group(3).trim();

                builder.put(state1, state2, Double.parseDouble(p));
            } else {
                throw new IllegalArgumentException("Rule has errors at " + line);
            }
        }

        return builder.build();
    }

    private RandomGenerator rng() {
        return rng;
    }

    public static final class ChainBuilder<S> implements Builder<ImmutableMarkovChain<S>> {

        private final Table<S, S, Double> table = HashBasedTable.create();
        private RandomGenerator rng = RandomGenerators.rng();

        public ChainBuilder<S> put(final S state, final S nextState, final double p) {
            table.put(state, nextState, p);
            return this;
        }

        public ChainBuilder<S> rng(final RandomGenerator rng) {
            this.rng = checkNotNull(rng);
            return this;
        }

        @Override
        public ImmutableMarkovChain<S> build() {
            // todo: check if sum of transition probabilities in rows are <= 1
            for (final S state : table.rowKeySet()) {
                double sum = 0.0;
                for (final Double value : table.row(state).values()) {
                    sum += value;
                }
                if (sum < 0 || sum > 1) {
                    final String message = "Sum of transition probabilities from state "
                            + state + " must be in >= 0 and <= 1";
                    throw new IllegalArgumentException(message);
                }
            }
            rng = RandomGenerators.rng();
            return new ImmutableMarkovChain<S>(ImmutableTable.copyOf(table), rng);
        }
    }

    public String toRule() {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<S, Map<S, Double>> entry : transitionTable.rowMap().entrySet()) {
            for (final Map.Entry<S, Double> doubleEntry : entry.getValue().entrySet()) {
                builder.append(entry.getKey());
                builder.append(" -> ");
                builder.append(doubleEntry.getKey());
                builder.append(" : ");
                builder.append(doubleEntry.getValue());
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return toRule();
    }
}
