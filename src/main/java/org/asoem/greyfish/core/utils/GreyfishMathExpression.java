package org.asoem.greyfish.core.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.VariableResolver;
import net.sourceforge.jeval.function.FunctionException;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.ContinuosProperty;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;

public enum GreyfishMathExpression {
    INSTANCE;

    private final Map<String, Object> parserCache = Maps.newHashMap();

    private double getResult(String expression, Agent individualInterface) {
        double ret = 0;

        Evaluator evaluator;
        if (parserCache.containsKey(expression)) {
            final Object object = parserCache.get(expression);
            if (object instanceof Double)
                return Double.class.cast(object);
            else
                evaluator = Evaluator.class.cast(object);
        }
        else {

            if (Pattern.matches("^\\d+(\\.\\d+)?$", expression)) {
                final double d = Double.valueOf(expression);
                parserCache.put(expression, d);
                return d;
            }
            else {
                evaluator = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);
                try {
                    evaluator.parse(expression);
                    parserCache.put(expression, evaluator);
                } catch (EvaluationException e) {
                    if (CORE_LOGGER.hasDebugEnabled()) CORE_LOGGER.debug("Failed to parse expression", e);
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
            if (CORE_LOGGER.hasDebugEnabled()) CORE_LOGGER.debug("Failed to evaluateConditions expression", e);
        } catch (NumberFormatException e) {
            if (CORE_LOGGER.hasDebugEnabled()) CORE_LOGGER.debug("Failed to convert to Double", e);
        }

        return ret;
    }

    private VariableResolver getVariableResolver(final Agent individualInterface) {

        return new VariableResolver() {
            @Override
            public String resolveVariable(final String arg0) throws FunctionException {

                final Scanner scanner = new Scanner(arg0).useDelimiter(Pattern.compile("\\."));
                if (!scanner.hasNext()) {
                    CORE_LOGGER.warn("Scanner was unable to scan input using delimiter '.':" + arg0);
                    return "0";
                }
                final String token1 = scanner.next();
                if ("property".equals(token1)) {
                    if (!scanner.hasNext()) {
                        CORE_LOGGER.warn("Scanner found nothing after token 'property':" + arg0);
                        return "0";
                    }
                    final String token2 = scanner.next();

                    try {
                        final ContinuosProperty<?> property =
                                Iterables.find(
                                        Iterables.filter(individualInterface.getProperties(), ContinuosProperty.class),
                                        new Predicate<ContinuosProperty>() {

                                            @Override
                                            public boolean apply(ContinuosProperty object) {
                                                return object.getName().equals(token2);
                                            }
                                        });
                        return String.valueOf(property.getAmount());
                    } catch(NoSuchElementException e) {
                        CORE_LOGGER.warn(e);
                        return "0";
                    }
                }
                else if ("env".equals(token1)) { // TODO: implement a search cache for 'env' to speed up variable resolution
                    assert individualInterface.getSimulation() != null;
                    final Simulation simulation = individualInterface.getSimulation();

                    if (!scanner.hasNext()) {
                        CORE_LOGGER.warn("Scanner found nothing after token 'env':" + arg0);
                        return "0";
                    }
                    final String token2 = scanner.next();
                    if (token2.startsWith("agentcount")) {

                        final Pattern conditionPattern = Pattern.compile(".+\\[.+=.+\\]");

                        if (conditionPattern.matcher(token2).matches()) {


                            final String[] keyValue = token2.substring(token2.indexOf('[')+1, token2.length()-1).split("=");
                            final String ret =  String.valueOf(Iterables.size(Iterables.filter(
                                    simulation.getAgents(),
                                    new Predicate<Agent>() {

                                        @Override
                                        public boolean apply(Agent object) {
                                            return Iterables.find(object.getProperties(), new Predicate<GFProperty>() {
                                                @Override
                                                public boolean apply(GFProperty gfProperty) {
                                                    return FiniteSetProperty.class.isInstance(gfProperty)
                                                            && gfProperty.hasName(keyValue[0])
                                                            && FiniteSetProperty.class.cast(gfProperty).get().toString().equals(keyValue[1]);
                                                }
                                            }, null) != null;
                                        }
                                    })));
                            return ret;
                        }
                        else
                            return String.valueOf(individualInterface.getSimulation().agentCount());
                    }
                }

                CORE_LOGGER.warn("No match for variable: " + arg0);
                return "0";
            }
        };
    }

    public static double evaluate(String expression, Agent individualInterface) {
        return INSTANCE.getResult(checkNotNull(expression), checkNotNull(individualInterface));
    }
}