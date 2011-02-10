package org.asoem.greyfish.core.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.VariableResolver;
import net.sourceforge.jeval.function.FunctionException;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.ContinuosProperty;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.io.GreyfishLogger.debug;
import static org.asoem.greyfish.core.io.GreyfishLogger.isDebugEnabled;

public enum GreyfishMathExpression {
    INSTANCE;

    private Map<String, Object> parserCache = Maps.newHashMap();

    private double getResult(String expression, IndividualInterface individualInterface) {
        double ret = 0;

        Evaluator evaluator;
        if (parserCache.containsKey(expression)) {
            Object object = parserCache.get(expression);
            if (object instanceof Double)
                return Double.class.cast(object);
            else
                evaluator = Evaluator.class.cast(object);
        }
        else {
            Scanner scanner = new Scanner(expression);
            if (scanner.hasNextDouble()) {
                double d = scanner.nextDouble();
                parserCache.put(expression, d);
                return d;
            }
            else {
                evaluator = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);
                try {
                    evaluator.parse(expression);
                    parserCache.put(expression, evaluator);
                } catch (EvaluationException e) {
                    if (isDebugEnabled()) debug("Failed to parse expression", e);
                    parserCache.put(expression, 0); // future calls will also fail
                    return 0;
                }
            }
        }

        try {
            // TODO: Unnecessary if expression is constant. How to check this?
            synchronized (parserCache.get(expression)) {
                evaluator.setVariableResolver(getVariableResolver(individualInterface));
                ret = Double.valueOf(evaluator.evaluate());
            }
        } catch (EvaluationException e) {
            if (isDebugEnabled()) debug("Failed to evaluate expression", e);
        } catch (NumberFormatException e) {
            if (isDebugEnabled()) debug("Failed to convert to Double", e);
        }

        return ret;
    }

    private VariableResolver getVariableResolver(final IndividualInterface individualInterface) {

        return new VariableResolver() {

            @Override
            public String resolveVariable(final String arg0) throws FunctionException {
                try {
                    ContinuosProperty<?> property = Iterables.find(Iterables.filter(individualInterface.getProperties(), ContinuosProperty.class), new Predicate<ContinuosProperty>() {

                        @Override
                        public boolean apply(ContinuosProperty object) {
                            return object.getName().equals(arg0);
                        }
                    });
                    return String.valueOf(property.getAmount());
                } catch(NoSuchElementException e) {
                    GreyfishLogger.warn(e);
                    return "0";
                }
            }
        };
    }

    public static double evaluate(String expression, IndividualInterface individualInterface) {
        return INSTANCE.getResult(checkNotNull(expression), checkNotNull(individualInterface));
    }
}