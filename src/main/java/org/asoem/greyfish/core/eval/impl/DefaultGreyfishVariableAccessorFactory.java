package org.asoem.greyfish.core.eval.impl;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.eval.GreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.AgentComponents;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 15:56
 */
public class DefaultGreyfishVariableAccessorFactory implements GreyfishVariableAccessorFactory {

    private static final Splitter SPLITTER = Splitter.on('.').trimResults(); // TODO: Exclude dots in parentheses

    @Override
    public <T> Function<T, ?> get(String varName, final Class<T> contextClass) {
        checkNotNull(varName);
        checkNotNull(contextClass);

        final Iterator<String> gomParts = SPLITTER.split(varName).iterator();

        if (gomParts.hasNext()) {

            final String root = gomParts.next();

            if ("this".equals(root) || "self".equals(root)) {
                if (GFAction.class.isAssignableFrom(contextClass)) {
                    return action(gomParts, new Function<T, GFAction>() {
                        @Override
                        public GFAction apply(@Nullable T agentComponent) {
                            return GFAction.class.cast(agentComponent);
                        }
                    });
                }
                else if (GFProperty.class.isAssignableFrom(contextClass)) {
                    return property(gomParts, new Function<T, GFProperty>() {
                        @Override
                        public GFProperty apply(@Nullable T agentComponent) {
                            return GFProperty.class.cast(agentComponent);
                        }
                    });
                }
                else if (Gene.class.isAssignableFrom(contextClass)) {
                    return gene(gomParts, new Function<T, Gene>() {
                        @Override
                        public Gene apply(@Nullable T agentComponent) {
                            return Gene.class.cast(agentComponent);
                        }
                    });
                }
                else if (GFCondition.class.isAssignableFrom(contextClass)) {
                    return condition(gomParts, new Function<T, GFCondition>() {
                        @Override
                        public GFCondition apply(@Nullable T agentComponent) {
                            return GFCondition.class.cast(agentComponent);
                        }
                    });
                }
                else {
                    throw new IllegalArgumentException("Root keywords 'this' of 'self' are not implemented for context " + contextClass);
                }
            }
            else if ("sim".equals(root) || "simulation".equals(root)) {
                if (AgentComponent.class.isAssignableFrom(contextClass)) {
                    return simulation(gomParts, new Function<T, Simulation>() {
                        @Override
                        public Simulation apply(@Nullable T gfComponent) {
                            Agent agent = AgentComponent.class.cast(gfComponent).getAgent();
                            return checkNotNull(agent).getSimulationContext().getSimulation();
                            // TODO: We should have direct access to simulation object through a component
                        }
                    });
                }
                else if (Simulation.class.isAssignableFrom(contextClass)) {
                    return simulation(gomParts, new Function<T, Simulation>() {
                        @Override
                        public Simulation apply(@Nullable T t) {
                            return Simulation.class.cast(t);
                        }
                    });
                }
                else {
                    throw new IllegalArgumentException("Root keyword 'sim' is not implemented for context " + contextClass);
                }
            }
            else if ("testVal".equals(root)) {
                return new Function<T, Object>() {
                    @Override
                    public Object apply(@Nullable T t) {
                        return 42.0;
                    }
                };
            }
            else if (root.matches("#\\w+")) {
                if (AgentComponent.class.isAssignableFrom(contextClass)) {
                    // search for component with name equal to given identifier

                    return new Function<T, Object>() {

                        private final String componentName = root.substring(1);
                        private Function<T, ?> cachedFunction = null;

                        @Override
                        public Object apply(@Nullable T t) {

                            if (cachedFunction == null) {
                                cachedFunction = composeFunction(t);
                            }

                            assert cachedFunction != null;

                            return cachedFunction.apply(t);
                        }

                        private Function<T, ?> composeFunction(T t) {
                            final AgentComponent component = AgentComponent.class.cast(t);
                            Agent agent = component.getAgent();
                            if (agent == null)
                                throw new AssertionError("Agent must not be null at this point");
                            final Iterable<AgentComponent> components = agent.getComponents();
                            AgentComponent target = AgentComponents.findByName(components, componentName);

                            if (target == null)
                                throw new IllegalArgumentException("Cannot find component with name equal to " + componentName);

                            if (GFAction.class.isInstance(target)) {
                                return action(gomParts, new Function<T, GFAction>() {
                                    @Override
                                    public GFAction apply(@Nullable T t) {
                                        final Agent agent1 = AgentComponent.class.cast(checkNotNull(t)).getAgent();
                                        if (agent1 == null)
                                            throw new AssertionError("Agent must not be null at this point");
                                        return agent1.getAction(componentName, GFAction.class);
                                    }
                                });
                            }
                            else if (GFProperty.class.isInstance(target)) {
                                return property(gomParts, new Function<T, GFProperty>() {
                                    @Override
                                    public GFProperty apply(@Nullable T t) {
                                        final Agent agent1 = AgentComponent.class.cast(checkNotNull(t)).getAgent();
                                        if (agent1 == null)
                                            throw new AssertionError("Agent must not be null at this point");
                                        return agent1.getProperty(componentName, GFProperty.class);
                                    }
                                });
                            }
                            else if (Gene.class.isInstance(target)) {
                                return gene(gomParts, new Function<T, Gene>() {
                                    @Override
                                    public Gene apply(@Nullable T t) {
                                        final Agent agent1 = AgentComponent.class.cast(checkNotNull(t)).getAgent();
                                        if (agent1 == null)
                                            throw new AssertionError("Agent must not be null at this point");
                                        return agent1.getGene(componentName, Gene.class);
                                    }
                                });
                            }
                            else
                                throw new UnsupportedOperationException("Component of class " + target.getClass() + " is not supported");
                        }
                    };
                }

            }
            else
                throw new IllegalArgumentException("Key '"+ root + "'" + " is not handled");
        }

        throw new IllegalArgumentException("Variable Name does not meet the syntax requirements: " + varName);
    }

    private <T> Function<T, ?> condition(Iterator<String> parts, Function<T, GFCondition> function) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<GFCondition, Agent>() {
                    @Override
                    public Agent apply(@Nullable GFCondition action) {
                        return checkNotNull(action).getAgent();
                    }
                }, function));
            }
            if (nextPart.matches("conditions\\[.+\\]")) {
            }
            throw new RuntimeException("GFCondition has no member named " + nextPart);
        }
        else {
            return function;
        }
    }

    @Override
    public boolean canConvert(String name, final Class<?> contextClass) {
        try {
            return get(name, contextClass) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private <T> Function<T, ?> action(Iterator<String> parts, Function<T, GFAction> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<GFAction, Agent>() {
                    @Override
                    public Agent apply(@Nullable GFAction action) {
                        return checkNotNull(action).getAgent();
                    }
                }, ret));
            }

            if (nextPart.matches("conditions\\[.+\\]")) {
            }
            else if (nextPart.equals("stepsSinceLastExecution")) {
                return Functions.compose(new Function<GFAction, Integer>() {
                    @Override
                    public Integer apply(@Nullable GFAction o) {
                        return checkNotNull(o).stepsSinceLastExecution();
                    }
                }, ret);
            }
            throw new RuntimeException("GFAction has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T> Function<T, ?> property(Iterator<String> parts, Function<T, GFProperty> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<GFProperty, Agent>() {
                    @Override
                    public Agent apply(@Nullable GFProperty property) {
                        return checkNotNull(property).getAgent();
                    }
                }, ret));
            }

            else
                throw new RuntimeException("GFProperty has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T> Function<T, ?> simulation(Iterator<String> parts, Function<T, Simulation> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();

            Pattern.compile("agents\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);

            if (nextPart.matches("agents\\[.+\\]")) {
                return agent(parts, Functions.compose(new Function<Simulation, Agent>() {
                    @Override
                    public Agent apply(@Nullable Simulation simulation) {
                        return Iterables.find(checkNotNull(simulation).getAgents(), new Predicate<Agent>() {
                            @Override
                            public boolean apply(Agent agent) {
                                return agent.getId() == 0; // TODO: get id from regex
                            }
                        });
                    }
                }, ret));
            }

            else if ("agentCount".equals(nextPart)) {
                return Functions.compose( new Function<Simulation, Object>() {
                    @Override
                    public Object apply(Simulation simulation) {
                        return simulation.countAgents();
                    }
                },ret);
            }

            else
                throw new RuntimeException("Simulation has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T> Function<T, ?> gene(Iterator<String> parts, Function<T, Gene> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();

            if ("value".equals(nextPart)) {
                return Functions.compose(new Function<Gene, Object>() {
                    @Override
                    public Object apply(@Nullable Gene gene) {
                        return checkNotNull(gene).get();
                    }
                }, ret);
            }

            else
                throw new RuntimeException("Gene has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T> Function<T, ?> agent(Iterator<String> parts, Function<T, Agent> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            Matcher matcher;

            if ("simulation".equals(nextPart)) {
                return simulation(parts, Functions.compose( new Function<Agent, Simulation>() {
                    @Override
                    public Simulation apply(@Nullable Agent agent) {
                        return checkNotNull(agent).getSimulationContext().getSimulation();
                    }
                },ret));
            }


            matcher = Pattern.compile("properties\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String propertyName = matcher.group(1);
                return property(parts, Functions.compose(new Function<Agent, GFProperty>() {
                    @Override
                    public GFProperty apply(@Nullable Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getProperty(propertyName, GFProperty.class);
                    }
                }, ret));
            }

            matcher = Pattern.compile("actions\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String actionName = matcher.group(1);
                return action(parts, Functions.compose(new Function<Agent, GFAction>() {
                    @Override
                    public GFAction apply(@Nullable Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getAction(actionName, GFAction.class);
                    }
                }, ret));
            }

            matcher = Pattern.compile("genes\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String geneName = matcher.group(1);
                return gene(parts, Functions.compose(new Function<Agent, Gene>() {
                    @Override
                    public Gene apply(@Nullable Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getGene(geneName, Gene.class);
                    }
                }, ret));
            }

            if ("age".equals(nextPart)) {
                return Functions.compose( new Function<Agent, Object>() {
                    @Override
                    public Object apply(@Nullable Agent agent) {
                        return checkNotNull(agent).getAge();
                    }
                },ret);
            }

            throw new RuntimeException("Agent has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }
}
