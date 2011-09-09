package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.ContinuousProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.04.11
 * Time: 14:33
 */
class AgentVariableResolver implements VariableResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentVariableResolver.class);

    private final static Map<String, Function<Object, Object>> variableFunctionMap = Maps.newHashMap();

    private final Agent agent;

    public AgentVariableResolver(Agent agent) {
        this.agent = checkNotNull(agent);
    }

    @Override
    public Object resolve(@Nonnull final String arg0) {
        checkNotNull(arg0);

        LOGGER.trace("Resolving variable '{}' for agent {}", arg0, agent);

        Object ret = getFromCache(arg0);
        if (ret != null)
            return ret;

        else {
            final Scanner scanner = new Scanner(arg0).useDelimiter(Pattern.compile("\\."));
            if (!scanner.hasNext()) {
                return noMatch(arg0, "Scanner was unable to scan '{}' using delimiter '.'", arg0);
            }
            final String token1 = scanner.next();

            // INTRINSIC VARS
            if ("agent".equals(token1)) {
                if (!scanner.hasNext()) {
                    return noMatch(arg0, "Syntax error in declaration of variable '{}'. Expected 'agent.<PopulationName>'.", arg0);
                }
                else {
                    final String token2 = scanner.next();

                    if (token2.startsWith("age")) {
                        return cache(arg0, new Function<Object, Object>() {
                            @Override
                            public Object apply(@Nullable Object o) {
                                if (o == null || !Agent.class.isInstance(o))
                                    return null;
                                return agent.getAge();
                            }
                        });
                    }
                    else {
                        return noMatch(arg0, "Error in declaration of variable '{}'. Unknown child element for token 'agent'", arg0);
                    }
                }
            }

            // PROPERTY VARS
            else if ("property".equals(token1)) {
                if (!scanner.hasNext()) {
                    return noMatch(arg0, "Syntax error in declaration of variable '{}'. Expected 'property.<PropertyName>'.", arg0);
                }
                else {
                    final String token2 = scanner.next();

                    return cache(arg0, new Function<Object, Object>() {
                        @Override
                        public Object apply(@Nullable Object o) {
                            if (o == null || !Agent.class.isInstance(o))
                                return null;
                            ContinuousProperty<?> property = agent.getProperty(token2, ContinuousProperty.class);
                            return property == null ? null : property.get();
                        }
                    });
                }
            }

            /*
            // SIMULATION VARS
            else if ("env".equals(token1)) {

                if (!scanner.hasNext()) {
                    return noMatch(arg0, "Syntax error in declaration of variable '{}'. Expected 'env.<EnvironmentalProperty>'.", arg0);
                }
                final String token2 = scanner.next();
                if (token2.startsWith("agentcount")) {

                    final Pattern conditionPattern = Pattern.compile(".+\\[.+=.+\\]");

                    if (conditionPattern.matcher(token2).matches()) {

                        final String[] keyValue = token2.substring(token2.indexOf('[')+1, token2.length()-1).split("=");
                        final String propertyName = keyValue[0];
                        final String propertyValue = keyValue[1];

                        return cache(arg0, new Function<Object, Object>() {
                            @Override
                            public Object apply(@Nullable Object o) {
                                if (o == null || !Agent.class.isInstance(o))
                                    return null;
                                return Iterables.size(Iterables.filter(
                                        agent.getSimulation().getAgents(),
                                        new Predicate<FinalizedAgent>() {

                                            @Override
                                            public boolean apply(FinalizedAgent object) {
                                                return Iterables.find(object.getProperties(), new Predicate<GFProperty>() {
                                                            @Override
                                                            public boolean apply(GFProperty gfProperty) {
                                                                return FiniteSetProperty.class.isInstance(gfProperty)
                                                                        && gfProperty.hasName(propertyName)
                                                                        && FiniteSetProperty.class.cast(gfProperty).get().toString().equals(propertyValue);
                                                            }
                                                        }, null) != null;
                                            }
                                        }));
                            }
                        });
                    }
                    else
                        return cache(arg0, new Function<Object, Object>() {
                            @Override
                            public Object apply(@Nullable Object o) {
                                if (o == null || !FinalizedAgent.class.isInstance(o))
                                    return null;
                                FinalizedAgent agent = FinalizedAgent.class.cast(o);
                                return agent.getSimulation().agentCount();
                            }
                        });
                }
            }
            */
        }

        return noMatch(arg0, "Variable '{}' cannot be resolved with this resolver.", arg0);
    }

    private @Nullable Object getFromCache(@Nullable String arg0) {
        Object ret = variableFunctionMap.containsKey(arg0) ? variableFunctionMap.get(arg0).apply(agent) : null;
        if (ret != null) {
            LOGGER.debug("Found varName '{}' in cache. Evaluates to '{}' for agent {}", arg0, ret, agent);
            return ret;
        }
        else
            return null;
    }

    private @Nonnull Object cache(@Nonnull String varName, @Nonnull Function<Object, Object> function) {
        variableFunctionMap.put(varName, function);
        //noinspection ConstantConditions
        return getFromCache(varName);
    }

    private @Nonnull Object noMatch(@Nonnull String arg0, @Nonnull String logMessage, Object... logArgs) {
        LOGGER.debug(logMessage, logArgs);
        return cache(arg0, Functions.<Object>constant(null));
    }
}
