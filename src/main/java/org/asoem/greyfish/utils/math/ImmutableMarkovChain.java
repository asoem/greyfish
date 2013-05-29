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

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:30
 */
public class ImmutableMarkovChain<S> implements MarkovChain<S> {

    private final Table<S, S, Double> markovMatrix;

    private ImmutableMarkovChain(Table<S, S, Double> markovMatrix) {
        this.markovMatrix = markovMatrix;
    }

    @Override
    public S apply(S state) {
        checkNotNull(state, "State must not be null");

        if (!markovMatrix.containsRow(state)) {
            if (markovMatrix.containsColumn(state)) {
                return state;
            }
            else
                throw new IllegalArgumentException("State '" + state + "' does not match any of the defined states in set {" + Joiner.on(", ").join(getStates()) + "}");
        }


        final Map<S, Double> row = markovMatrix.row(state);

        if (row.isEmpty()) {
            return state;
        }

        double sum = 0;
        double rand = rng().nextDouble();
        for (Map.Entry<S, Double> cell : row.entrySet()) {
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

    public static ImmutableMarkovChain<String> parse(String rule) {
        ChainBuilder<String> builder = builder();

        final Splitter splitter = Splitter.onPattern("\r?\n|;").trimResults();
        final Iterable<String> lines = splitter.split(rule);

        final Pattern pattern = Pattern.compile("^(.+)->(.+):(.+)$");
        for (String line : lines) {
            if(line.isEmpty())
                continue;
            final Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String state1 = matcher.group(1).trim();
                String state2 = matcher.group(2).trim();
                String p = matcher.group(3).trim();
                
                builder.put(state1, state2, Double.parseDouble(p));
            }
            else throw new IllegalArgumentException("Rule has errors at " + line);
        }

        return builder.build();
    }

    public static class ChainBuilder<S> implements Builder<ImmutableMarkovChain<S>> {

        private final Table<S, S, Double> table = HashBasedTable.create();

        public ChainBuilder<S> put(S state, S nextState, double p) {
            table.put(state, nextState, p);
            return this;
        }

        @Override
        public ImmutableMarkovChain<S> build() throws IllegalStateException {
            // todo: check if sum of transition probabilities in rows are <= 1
            for (S state : table.rowKeySet()) {
                double sum = 0.0;
                for (Double value : table.row(state).values()) {
                    sum += value;
                }
                if (sum < 0 || sum > 1)
                    throw new IllegalArgumentException("Sum of transition probabilities from state " + state + " must be in >= 0 and <= 1");
            }
            return new ImmutableMarkovChain<S>(ImmutableTable.copyOf(table));
        }
    }
    
    public String toRule() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<S, Map<S,Double>> entry : markovMatrix.rowMap().entrySet()) {
            for (Map.Entry<S, Double> doubleEntry : entry.getValue().entrySet()) {
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
