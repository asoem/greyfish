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

import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.math.RandomGenerators.rng;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 12:16
 */
public class EvaluatingMarkovChain<S> implements MarkovChain<S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImmutableMarkovChain.class);

    private final Table<S, S, Expression> markovMatrix;
    private static final Pattern PATTERN = Pattern.compile("^\\s*([\\w\\s\\d_]+)\\s*->\\s*([\\w\\s\\d_]+)\\s*:\\s*(\\S+)\\s*$");

    private EvaluatingMarkovChain(ChainBuilder<S> builder) {
        this.markovMatrix = ImmutableTable.copyOf(builder.table);
    }

    /**
     * Make a transition to the next state as defined by the markovMatrix
     *
     * @param state the current state
     * @param resolver the context for the transition probability evaluation
     * @return the next state
     */
    public S apply(S state, VariableResolver resolver) {
        checkNotNull(state, "State must not be null");

        if (!markovMatrix.containsRow(state)) {
            if (markovMatrix.containsColumn(state)) {
                LOGGER.debug("State is (implicitly) just self referent: {}", state);
                return state;
            }
            else
                throw new IllegalArgumentException("State '" + state + "' does not match any of the defined states in set {" + Joiner.on(", ").join(getStates()) + "}");
        }


        final Map<S, Expression> row = markovMatrix.row(state);

        if (row.isEmpty()) {
            LOGGER.debug("No outgoing transition has been defined for state '{}'", state);
            return state;
        }

        double sum = 0;
        double rand = rng().nextDouble();
        for (Map.Entry<S, Expression> cell : row.entrySet()) {
            try {
                sum += cell.getValue().evaluate(resolver).asDouble();
            } catch (ParseException e) {
                throw new AssertionError("expression '" + cell.getValue() + "' could not get evaluated as a double value");
            }
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

    public static <S> ChainBuilder<S> builder(ExpressionFactory expressionFactory) {
        return new ChainBuilder<S>(expressionFactory);
    }

    public static EvaluatingMarkovChain<String> parse(String rule, ExpressionFactory factory) {
        checkNotNull(rule);
        checkNotNull(factory);
        
        final ChainBuilder<String> builder = builder(factory);

        final Splitter splitter = Splitter.onPattern("\r?\n|;").trimResults();
        final Iterable<String> lines = splitter.split(rule);

        for (String line : lines) {
            if(line.isEmpty())
                continue;
            final Matcher matcher = PATTERN.matcher(line);
            if (matcher.matches()) {
                String state1 = matcher.group(1).trim();
                String state2 = matcher.group(2).trim();
                String p = matcher.group(3).trim();

                builder.put(state1, state2, p);
            }
            else throw new IllegalArgumentException("Rule has errors at " + line);
        }

        return builder.build();
    }

    @Override
    public Set<S> getStates() {
        return Sets.union(markovMatrix.rowKeySet(), markovMatrix.columnKeySet());
    }

    @Override
    public S apply(S state) {
        return apply(state, VariableResolvers.emptyResolver());
    }

    public static class ChainBuilder<S> implements Builder<EvaluatingMarkovChain<S>> {

        private final Table<S, S, Expression> table = HashBasedTable.create();
        private final ExpressionFactory expressionFactory;

        public ChainBuilder(ExpressionFactory expressionFactory) {

            this.expressionFactory = checkNotNull(expressionFactory);
        }

        public ChainBuilder<S> put(S state, S nextState, String expression) {
            checkNotNull(state);
            checkNotNull(nextState);
            checkNotNull(expression);

            table.put(state, nextState, expressionFactory.compile(expression));
            return this;
        }

        @Override
        public EvaluatingMarkovChain<S> build() throws IllegalStateException {
            return new EvaluatingMarkovChain<S>(this);
        }
    }

    public String toRule() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<S, Map<S, Expression>> entry : markovMatrix.rowMap().entrySet()) {
            for (Map.Entry<S, Expression> doubleEntry : entry.getValue().entrySet()) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvaluatingMarkovChain)) return false;

        EvaluatingMarkovChain that = (EvaluatingMarkovChain) o;

        return markovMatrix.equals(that.markovMatrix);

    }

    @Override
    public int hashCode() {
        return markovMatrix.hashCode();
    }
}
