package org.asoem.greyfish.core.eval.impl;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.eval.GreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.AgentComponents;
import org.asoem.greyfish.utils.space.SpatialObject;

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
                if (AgentAction.class.isAssignableFrom(contextClass)) {
                    return action(gomParts, new Function<T, AgentAction>() {
                        @Override
                        public AgentAction apply(T agentComponent) {
                            return AgentAction.class.cast(agentComponent);
                        }
                    });
                } else if (AgentProperty.class.isAssignableFrom(contextClass)) {
                    return property(gomParts, new Function<T, AgentProperty>() {
                        @Override
                        public AgentProperty apply(T agentComponent) {
                            return AgentProperty.class.cast(agentComponent);
                        }
                    });
                } else if (AgentTrait.class.isAssignableFrom(contextClass)) {
                    return gene(gomParts, new Function<T, AgentTrait>() {
                        @Override
                        public AgentTrait apply(T agentComponent) {
                            return AgentTrait.class.cast(agentComponent);
                        }
                    });
                } else if (ActionCondition.class.isAssignableFrom(contextClass)) {
                    return condition(gomParts, new Function<T, ActionCondition>() {
                        @Override
                        public ActionCondition apply(T agentComponent) {
                            return ActionCondition.class.cast(agentComponent);
                        }
                    });
                } else {
                    throw new IllegalArgumentException("Root keywords 'this' of 'self' are not implemented for context " + contextClass);
                }
            } else if ("sim".equals(root) || "simulation".equals(root)) {
                if (AgentComponent.class.isAssignableFrom(contextClass)) {
                    return simulation(gomParts, new Function<T, Simulation<SpatialObject>>() {
                        @Override
                        public Simulation<SpatialObject> apply(T gfComponent) {
                            Agent agent = AgentComponent.class.cast(gfComponent).getAgent();
                            return checkNotNull(agent).simulation();
                            // TODO: We should have direct access to simulation object through a component
                        }
                    });
                } else if (Simulation<SpatialObject>.class.isAssignableFrom(contextClass)) {
                    return simulation(gomParts, new Function<T, Simulation<SpatialObject>>() {
                        @Override
                        public Simulation<SpatialObject> apply(T t) {
                            return Simulation<SpatialObject>.class.cast(t);
                        }
                    });
                } else {
                    throw new IllegalArgumentException("Root keyword 'sim' is not implemented for context " + contextClass);
                }
            } else if ("testVal".equals(root)) {
                return new Function<T, Object>() {
                    @Override
                    public Object apply(T t) {
                        return 42.0;
                    }
                };
            } else if (root.matches("#\\w+")) {
                if (AgentComponent.class.isAssignableFrom(contextClass)) {
                    // search for component with name equal to given identifier

                    return new Function<T, Object>() {

                        private final String componentName = root.substring(1);
                        private Function<T, ?> cachedFunction = null;

                        @Override
                        public Object apply(T t) {

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
                            final Iterable<AgentComponent> components = Iterables.concat(agent.getActions(), agent.getProperties(), agent.getTraits());
                            AgentComponent target = AgentComponents.findByName(components, componentName);

                            if (target == null)
                                throw new IllegalArgumentException("Cannot find component with name equal to " + componentName);

                            if (AgentAction.class.isInstance(target)) {
                                return action(gomParts, new Function<T, AgentAction>() {
                                    @Override
                                    public AgentAction apply(T t) {
                                        final Agent agent1 = AgentComponent.class.cast(checkNotNull(t)).getAgent();
                                        if (agent1 == null)
                                            throw new AssertionError("Agent must not be null at this point");
                                        return agent1.getAction(componentName);
                                    }
                                });
                            } else if (AgentProperty.class.isInstance(target)) {
                                return property(gomParts, new Function<T, AgentProperty>() {
                                    @Override
                                    public AgentProperty apply(T t) {
                                        final Agent agent1 = AgentComponent.class.cast(checkNotNull(t)).getAgent();
                                        if (agent1 == null)
                                            throw new AssertionError("Agent must not be null at this point");
                                        return agent1.getProperty(componentName, AgentProperty.class);
                                    }
                                });
                            } else if (AgentTrait.class.isInstance(target)) {
                                return gene(gomParts, new Function<T, AgentTrait>() {
                                    @Override
                                    public AgentTrait apply(T t) {
                                        final Agent agent1 = AgentComponent.class.cast(checkNotNull(t)).getAgent();
                                        if (agent1 == null)
                                            throw new AssertionError("Agent must not be null at this point");
                                        return agent1.getTrait(componentName);
                                    }
                                });
                            } else
                                throw new UnsupportedOperationException("Component of class " + target.getClass() + " is not supported");
                        }
                    };
                }

            } else
                throw new IllegalArgumentException("Key '" + root + "'" + " is not handled");
        }

        throw new IllegalArgumentException("Variable Name does not meet the syntax requirements: " + varName);
    }

    private <T> Function<T, ?> condition(Iterator<String> parts, Function<T, ActionCondition> function) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<ActionCondition, Agent>() {
                    @Override
                    public Agent apply(ActionCondition action) {
                        return checkNotNull(action).getAgent();
                    }
                }, function));
            }
            //if (nextPart.matches("conditions\\[.+\\]")) {
            //}
            throw new RuntimeException("ActionCondition has no member named " + nextPart);
        } else {
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

    private <T> Function<T, ?> action(Iterator<String> parts, Function<T, AgentAction> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<AgentAction, Agent>() {
                    @Override
                    public Agent apply(AgentAction action) {
                        return checkNotNull(action).getAgent();
                    }
                }, ret));
            }

            if (nextPart.matches("conditions\\[.+\\]")) {
            } else if (nextPart.equals("stepsSinceLastExecution")) {
                return Functions.compose(new Function<AgentAction, Integer>() {
                    @Override
                    public Integer apply(AgentAction o) {
                        return checkNotNull(o).lastCompletionStep();
                    }
                }, ret);
            }
            throw new RuntimeException("AgentAction has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> property(Iterator<String> parts, Function<T, AgentProperty> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<AgentProperty, Agent>() {
                    @Override
                    public Agent apply(AgentProperty property) {
                        return checkNotNull(property).getAgent();
                    }
                }, ret));
            } else
                throw new RuntimeException("AgentProperty has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> simulation(Iterator<String> parts, Function<T, Simulation<SpatialObject>> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();

            Pattern.compile("agents\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);

            if (nextPart.matches("agents\\[.+\\]")) {
                return agent(parts, Functions.compose(new Function<Simulation<SpatialObject>, Agent>() {
                    @Override
                    public Agent apply(Simulation<SpatialObject> simulation) {
                        return Iterables.find(checkNotNull(simulation).getAgents(), new Predicate<Agent>() {
                            @Override
                            public boolean apply(Agent agent) {
                                return agent.getId() == 0; // TODO: get id from regex
                            }
                        });
                    }
                }, ret));
            } else if ("agentCount".equals(nextPart)) {
                return Functions.compose(new Function<Simulation<SpatialObject>, Object>() {
                    @Override
                    public Object apply(Simulation<SpatialObject> simulation) {
                        return simulation.countAgents();
                    }
                }, ret);
            } else
                throw new RuntimeException("Simulation has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> gene(Iterator<String> parts, Function<T, AgentTrait> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();

            if ("value".equals(nextPart)) {
                return Functions.compose(new Function<AgentTrait, Object>() {
                    @Override
                    public Object apply(AgentTrait gene) {
                        return checkNotNull(gene).getAllele();
                    }
                }, ret);
            } else
                throw new RuntimeException("AgentTrait has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> agent(Iterator<String> parts, Function<T, Agent> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            Matcher matcher;

            if ("simulation".equals(nextPart)) {
                return simulation(parts, Functions.compose(new Function<Agent, Simulation<SpatialObject>>() {
                    @Override
                    public Simulation<SpatialObject> apply(Agent agent) {
                        return checkNotNull(agent).simulation();
                    }
                }, ret));
            }


            matcher = Pattern.compile("properties\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String propertyName = matcher.group(1);
                return property(parts, Functions.compose(new Function<Agent, AgentProperty>() {
                    @Override
                    public AgentProperty apply(Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getProperty(propertyName, AgentProperty.class);
                    }
                }, ret));
            }

            matcher = Pattern.compile("actions\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String actionName = matcher.group(1);
                return action(parts, Functions.compose(new Function<Agent, AgentAction>() {
                    @Override
                    public AgentAction apply(Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getAction(actionName);
                    }
                }, ret));
            }

            matcher = Pattern.compile("genes\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String geneName = matcher.group(1);
                return gene(parts, Functions.compose(new Function<Agent, AgentTrait>() {
                    @Override
                    public AgentTrait apply(Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getTrait(geneName);
                    }
                }, ret));
            }

            if ("age".equals(nextPart)) {
                return Functions.compose(new Function<Agent, Object>() {
                    @Override
                    public Object apply(Agent agent) {
                        return checkNotNull(agent).getAge();
                    }
                }, ret);
            }

            throw new RuntimeException("Agent has no member named " + nextPart);
        } else {
            return ret;
        }
    }
}
