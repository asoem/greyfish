package org.asoem.greyfish.core.eval;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public enum GreyfishMathExpression {
    SINGLETON_INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(GreyfishMathExpression.class);
    private final Map<String, Object> parserCache = Maps.newHashMap();

    private final static Supplier<ExpressionParser> PARSER_FACTORY = new Supplier<ExpressionParser>() {
        @Override
        public ExpressionParser get() {
            return new SeeExpressionParser();
        }
    };

    public double getResult(String expression, Agent agent, Simulation simulation, Object... args) throws EvaluationException {
        checkNotNull(expression);
        checkNotNull(agent);
        checkNotNull(args);

        if (!isCached(expression))
            cache(expression);
        return evaluateInternal(fromCache(expression), agent, simulation, args);
    }

    private void cache(String expression) {
        if (Pattern.matches("^\\d+(\\.\\d+)?$", expression)) {
            final double d = Double.valueOf(expression);
            parserCache.put(expression, d);
        }
        else {
            try {
                ExpressionParser evaluator = PARSER_FACTORY.get();
                evaluator.parse(expression);
                parserCache.put(expression, evaluator);
            } catch (Exception e) {
                LOGGER.error("Failed to parse expression", e);
                parserCache.put(expression, 0.0); // future calls will also fail
            }
        }
    }

    private double evaluateInternal(Object object, Agent agent, Simulation simulation, Object... args) throws EvaluationException {
        assert object != null;
        assert agent != null;
        assert args != null;

        if (object instanceof Double)
            return Double.class.cast(object);

        else if (object instanceof ExpressionParser) {
            ExpressionParser evaluator = ExpressionParser.class.cast(object);

            VariableResolver variableResolver =
                    VariableResolvers.concat(
                            new SimulationVariableResolver(simulation),
                            new AgentVariableResolver(agent),
                            new ArgumentsVariableResolver(args));

            evaluator.setResolver(variableResolver);

            // TODO: Add bool evaluation
            return evaluator.evaluateAsDouble();
        }

        else
            throw new IllegalArgumentException("Argument has unhandled type: " + object.getClass());
    }

    private Object fromCache(String expression) {
        return parserCache.get(expression);
    }

    private boolean isCached(String expression) {
        return parserCache.containsKey(expression);
    }

    public static double evaluateAsDouble(String expression, Agent agent, Simulation simulation) throws EvaluationException {
        return SINGLETON_INSTANCE.getResult(checkNotNull(expression), checkNotNull(agent), simulation);
    }

    public static double evaluateAsDouble(String expression, Agent componentOwner, Simulation simulation, Object... args) throws EvaluationException {
        return SINGLETON_INSTANCE.getResult(checkNotNull(expression), checkNotNull(componentOwner), simulation, args);
    }

    public static boolean isValidExpression(String expression) {
        try {
            ExpressionParser evaluator = PARSER_FACTORY.get();
            evaluator.parse(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}