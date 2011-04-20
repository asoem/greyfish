package org.asoem.greyfish.core.eval;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.sourceforge.jeval.VariableResolver;
import net.sourceforge.jeval.function.FunctionException;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.ContinuosProperty;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* User: christoph
* Date: 20.04.11
* Time: 14:33
*/
class AgentVariableResolver implements VariableResolver {
    private final Agent agent;

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentVariableResolver.class);

    public AgentVariableResolver(Agent agent) {
        this.agent = checkNotNull(agent);
    }

    @Override
    public String resolveVariable(final String arg0) throws FunctionException {

        final Scanner scanner = new Scanner(arg0).useDelimiter(Pattern.compile("\\."));
        if (!scanner.hasNext()) {
            LOGGER.warn("Scanner was unable to scan input using delimiter '.':" + arg0);
            return "0";
        }
        final String token1 = scanner.next();

        // INTRINSIC VARS
        if ("agent".equals(token1)) {
            if (!scanner.hasNext()) {
                LOGGER.warn("Scanner found nothing after token 'agent':" + arg0);
                return "0";
            }
            final String token2 = scanner.next();

            if (token2.startsWith("age")) {
                return String.valueOf(agent.getAge());
            }
            else {
                LOGGER.debug("Unknown child element for token 'agent': {}", token2);
            }
        }

        // PROPERTY VARS
        else if ("property".equals(token1)) {
            if (!scanner.hasNext()) {
                LOGGER.warn("Scanner found nothing after token 'property':" + arg0);
                return "0";
            }
            final String token2 = scanner.next();

            try {
                final ContinuosProperty<?> property =
                        Iterables.find(
                                Iterables.filter(agent.getProperties(), ContinuosProperty.class),
                                new Predicate<ContinuosProperty>() {

                                    @Override
                                    public boolean apply(ContinuosProperty object) {
                                        return object.getName().equals(token2);
                                    }
                                });
                return String.valueOf(property.getAmount());
            } catch(NoSuchElementException e) {
                LOGGER.warn("Property variable not found", e);
                return "0";
            }
        }

        // SIMULATION VARS
        else if ("env".equals(token1)) { // TODO: implement a search cache for 'env' to speed up variable resolution
            assert agent.getSimulation() != null;
            final Simulation simulation = agent.getSimulation();

            if (!scanner.hasNext()) {
                LOGGER.warn("Scanner found nothing after token 'env':" + arg0);
                return "0";
            }
            final String token2 = scanner.next();
            if (token2.startsWith("agentcount")) {

                final Pattern conditionPattern = Pattern.compile(".+\\[.+=.+\\]");

                if (conditionPattern.matcher(token2).matches()) {


                    final String[] keyValue = token2.substring(token2.indexOf('[')+1, token2.length()-1).split("=");
                    return String.valueOf(Iterables.size(Iterables.filter(
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
                }
                else
                    return String.valueOf(agent.getSimulation().agentCount());
            }
        }

        return null;
    }
}
