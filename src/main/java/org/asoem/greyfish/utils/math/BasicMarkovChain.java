package org.asoem.greyfish.utils.math;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:30
 */
public class BasicMarkovChain<S> implements MarkovChain<S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMarkovChain.class);

    private final Table<S, S, Double> markovMatrix;

    private BasicMarkovChain(Table<S, S, Double> markovMatrix) {
        this.markovMatrix = markovMatrix;
    }

    /**
     * Make a transition to the next state as defined by the markovMatrix
     *
     * @param state the current state
     * @return the next state
     */
    public S apply(S state) {
        checkNotNull(state, "State must not be null");

        if (!markovMatrix.containsRow(state)) {
            if (markovMatrix.containsColumn(state)) {
                LOGGER.debug("State is (implicitly) just self referent: {}", state);
                return state;
            }
            else
                throw new IllegalArgumentException("State '" + state + "' does not match any of the defined states in set {" + Joiner.on(", ").join(getStates()) + "}");
        }


        final Map<S, Double> row = markovMatrix.row(state);

        if (row.isEmpty()) {
            LOGGER.debug("No outgoing transition has been defined for state '{}'", state);
            return state;
        }

        double sum = 0;
        double rand = RandomUtils.nextDouble();
        for (Map.Entry<S, Double> cell : row.entrySet()) {
            sum += cell.getValue();
            if (sum > rand) {
                return cell.getKey();
            }
        }

        if (sum < 1) {
            LOGGER.debug("The sum of transition probabilities for state {} are < 1: {}." +
                    "Reminding fraction will be used by the identity transition", state, sum);
        }
        else if (sum > 1) {
            LOGGER.warn("The sum of transition probabilities for state {} are > 1: {}." +
                    "Some states might never be reached.", state, sum);
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

    public static BasicMarkovChain<String> parse(String rule) {
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

    public static class ChainBuilder<S> implements Builder<BasicMarkovChain<S>> {

        private final Table<S, S, Double> table = HashBasedTable.create();

        public ChainBuilder<S> put(S state, S nextState, double p) {
            table.put(state, nextState, p);
            return this;
        }

        @Override
        public BasicMarkovChain<S> build() throws IllegalStateException {
            return new BasicMarkovChain<S>(ImmutableTable.copyOf(table));
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
