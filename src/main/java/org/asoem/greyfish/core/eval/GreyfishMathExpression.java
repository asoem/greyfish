package org.asoem.greyfish.core.eval;

import com.google.common.collect.Maps;
import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.VariableResolver;
import org.asoem.greyfish.core.individual.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public enum GreyfishMathExpression {
    SINGLETON_INSTANCE;

    private final Logger LOGGER = LoggerFactory.getLogger(GreyfishMathExpression.class);

    private final Map<String, Object> parserCache = Maps.newHashMap();


    public double getResult(String expression, Agent agent, Object ... args) throws EvaluationException {
        checkNotNull(expression);
        checkNotNull(agent);
        checkNotNull(args);

        if (!isCached(expression))
            cache(expression);
        return evaluateInternal(fromCache(expression), agent, args);
    }

    private void cache(String expression) {
        if (Pattern.matches("^\\d+(\\.\\d+)?$", expression)) {
            final double d = Double.valueOf(expression);
            parserCache.put(expression, d);
        }
        else {
            Evaluator evaluator = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);
            try {
                evaluator.parse(expression);
                parserCache.put(expression, evaluator);
            } catch (EvaluationException e) {
                LOGGER.debug("Failed to parse expression", e);
                parserCache.put(expression, 0); // future calls will also fail
            }
        }
    }

    private double evaluateInternal(Object object, Agent agent, Object... args) throws EvaluationException {
        assert object != null;
        assert agent != null;
        assert args != null;

        if (object instanceof Double)
            return Double.class.cast(object);

        else if (object instanceof Evaluator) {
            Evaluator evaluator = Evaluator.class.cast(object);

            VariableResolver variableResolver =
                    VariableResolvers.concat(
                            new AgentVariableResolver(agent),
                            new ArgumentsVariableResolver(args));

            evaluator.setVariableResolver(variableResolver);
            return Double.valueOf(evaluator.evaluate());
        }

        else
            throw new IllegalArgumentException("Argument has unhandled type: " + object);
    }

    private Object fromCache(String expression) {
        return parserCache.get(expression);
    }

    private boolean isCached(String expression) {
        return parserCache.containsKey(expression);
    }

    public static double evaluate(String expression, Agent individualInterface) throws EvaluationException {
        return SINGLETON_INSTANCE.getResult(checkNotNull(expression), checkNotNull(individualInterface));
    }

    public static double evaluate(String expression, Agent componentOwner, Object ... args) throws EvaluationException {
        return SINGLETON_INSTANCE.getResult(checkNotNull(expression), checkNotNull(componentOwner), args);
    }

}