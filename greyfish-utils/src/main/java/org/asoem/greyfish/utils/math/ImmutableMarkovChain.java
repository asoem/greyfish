package org.asoem.greyfish.utils.math;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.asoem.greyfish.utils.base.Builder;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.math.RandomGenerators.rng;

public class ImmutableMarkovChain<S> implements MarkovChain<S> {

    private final Table<S, S, Double> markovMatrix;

    private ImmutableMarkovChain(final Table<S, S, Double> markovMatrix) {
        this.markovMatrix = markovMatrix;
    }

    @Override
    public S apply(final S state) {
        checkNotNull(state, "State must not be null");

        if (!markovMatrix.containsRow(state)) {
            if (markovMatrix.containsColumn(state)) {
                return state;
            } else {
                final String message = "State '" + state
                        + "' does not match any of the defined states in set {"
                        + Joiner.on(", ").join(getStates()) + "}";
                throw new IllegalArgumentException(message);
            }
        }


        final Map<S, Double> row = markovMatrix.row(state);

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
        return Sets.union(markovMatrix.rowKeySet(), markovMatrix.columnKeySet());
    }

    public static <S> ChainBuilder<S> builder() {
        return new ChainBuilder<S>();
    }

    public static ImmutableMarkovChain<String> parse(final String rule) {
        final ChainBuilder<String> builder = builder();

        final Splitter splitter = Splitter.onPattern("\r?\n|;").trimResults();
        final Iterable<String> lines = splitter.split(rule);

        final Pattern pattern = Pattern.compile("^(.+)->(.+):(.+)$");
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

    public static final class ChainBuilder<S> implements Builder<ImmutableMarkovChain<S>> {

        private final Table<S, S, Double> table = HashBasedTable.create();

        public ChainBuilder<S> put(final S state, final S nextState, final double p) {
            table.put(state, nextState, p);
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
            return new ImmutableMarkovChain<S>(ImmutableTable.copyOf(table));
        }
    }
    
    public String toRule() {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<S, Map<S, Double>> entry : markovMatrix.rowMap().entrySet()) {
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