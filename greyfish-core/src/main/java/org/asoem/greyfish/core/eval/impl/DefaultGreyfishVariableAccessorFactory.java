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
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.utils.AgentComponents;

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
    public <T> Function<T, ?> get(final String varName, final Class<T> contextClass) {
        checkNotNull(varName);
        checkNotNull(contextClass);

        final Iterator<String> gomParts = SPLITTER.split(varName).iterator();

        if (gomParts.hasNext()) {

            final String root = gomParts.next();

            if ("this".equals(root) || "self".equals(root)) {
                if (AgentAction.class.isAssignableFrom(contextClass)) {
                    return action(gomParts, new Function<T, AgentAction<?>>() {
                        @Override
                        public AgentAction apply(final T agentComponent) {
                            return AgentAction.class.cast(agentComponent);
                        }
                    });
                } else if (AgentProperty.class.isAssignableFrom(contextClass)) {
                    return property(gomParts, new Function<T, AgentProperty<?, ?>>() {
                        @Override
                        public AgentProperty apply(final T agentComponent) {
                            return AgentProperty.class.cast(agentComponent);
                        }
                    });
                } else if (AgentTrait.class.isAssignableFrom(contextClass)) {
                    return gene(gomParts, new Function<T, AgentTrait>() {
                        @Override
                        public AgentTrait apply(final T agentComponent) {
                            return AgentTrait.class.cast(agentComponent);
                        }
                    });
                } else if (ActionCondition.class.isAssignableFrom(contextClass)) {
                    return condition(gomParts, new Function<T, ActionCondition<?>>() {
                        @Override
                        public ActionCondition apply(final T agentComponent) {
                            return ActionCondition.class.cast(agentComponent);
                        }
                    });
                } else {
                    throw new IllegalArgumentException("Root keywords 'this' of 'self' are not implemented for context " + contextClass);
                }
            } else if ("sim".equals(root) || "getSimulation".equals(root)) {
                if (AgentComponent.class.isAssignableFrom(contextClass)) {
                    return simulation(gomParts, new Function<T, Simulation<?>>() {
                        @Override
                        public Simulation<?> apply(final T gfComponent) {
                            final Agent<?, ?> agent = ((AgentComponent<?>) AgentComponent.class.cast(gfComponent)).agent().orNull();
                            return checkNotNull(agent).simulation();
                            // TODO: We should have direct access to getSimulation object through a component
                        }
                    });
                } else if (DiscreteTimeSimulation.class.isAssignableFrom(contextClass)) {
                    return simulation(gomParts, new Function<T, Simulation<?>>() {
                        @Override
                        public Simulation<?> apply(final T t) {
                            return DiscreteTimeSimulation.class.cast(t);
                        }
                    });
                } else {
                    throw new IllegalArgumentException("Root keyword 'sim' is not implemented for context " + contextClass);
                }
            } else if ("testVal".equals(root)) {
                return new Function<T, Object>() {
                    @Override
                    public Object apply(final T t) {
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
                        public Object apply(final T t) {

                            if (cachedFunction == null) {
                                cachedFunction = composeFunction(t);
                            }

                            assert cachedFunction != null;

                            return cachedFunction.apply(t);
                        }

                        private Function<T, ?> composeFunction(final T t) {
                            final AgentComponent<?> component = (AgentComponent<?>) t;
                            final Agent<?, ?> agent = component.agent().orNull();
                            if (agent == null) {
                                throw new AssertionError("Agent must not be null at this point");
                            }
                            final Iterable<AgentComponent<?>> components = Iterables.concat(agent.getActions(), agent.getProperties(), agent.getTraits());
                            final AgentComponent target = AgentComponents.findByName(components, componentName);

                            if (target == null) {
                                throw new IllegalArgumentException("Cannot find component with name equal to " + componentName);
                            }

                            if (AgentAction.class.isInstance(target)) {
                                return action(gomParts, new Function<T, AgentAction<?>>() {
                                    @Override
                                    public AgentAction apply(final T t) {
                                        final Agent<?, ?> agent1 = ((AgentComponent<?>) AgentComponent.class.cast(checkNotNull(t))).agent().orNull();
                                        if (agent1 == null) {
                                            throw new AssertionError("Agent must not be null at this point");
                                        }
                                        return agent1.getAction(componentName);
                                    }
                                });
                            } else if (AgentProperty.class.isInstance(target)) {
                                return property(gomParts, new Function<T, AgentProperty<?, ?>>() {
                                    @Override
                                    public AgentProperty<?, ?> apply(final T t) {
                                        final Agent<?, ?> agent1 = ((AgentComponent<?>) AgentComponent.class.cast(checkNotNull(t))).agent().orNull();
                                        if (agent1 == null) {
                                            throw new AssertionError("Agent must not be null at this point");
                                        }
                                        return agent1.getProperty(componentName);
                                    }
                                });
                            } else if (AgentTrait.class.isInstance(target)) {
                                return gene(gomParts, new Function<T, AgentTrait>() {
                                    @Override
                                    public AgentTrait<?, ?> apply(final T t) {
                                        final Agent<?, ?> agent1 = ((AgentComponent<?>) AgentComponent.class.cast(checkNotNull(t))).agent().orNull();
                                        if (agent1 == null) {
                                            throw new AssertionError("Agent must not be null at this point");
                                        }
                                        return agent1.getTrait(componentName);
                                    }
                                });
                            } else {
                                throw new UnsupportedOperationException("Component of class " + target.getClass() + " is not supported");
                            }
                        }
                    };
                }

            } else
                throw new IllegalArgumentException("Key '" + root + "'" + " is not handled");
        }

        throw new IllegalArgumentException("Variable Name does not meet the syntax requirements: " + varName);
    }

    private <T> Function<T, ?> condition(final Iterator<String> parts, final Function<T, ActionCondition<?>> function) {
        if (parts.hasNext()) {
            final String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<ActionCondition<?>, Agent>() {
                    @Override
                    public Agent apply(final ActionCondition<?> action) {
                        return checkNotNull(action).agent().orNull();
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
    public boolean canConvert(final String name, final Class<?> contextClass) {
        try {
            return get(name, contextClass) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private <T> Function<T, ?> action(final Iterator<String> parts, final Function<T, AgentAction<?>> ret) {
        if (parts.hasNext()) {
            final String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<AgentAction<?>, Agent>() {
                    @Override
                    public Agent apply(final AgentAction<?> action) {
                        return checkNotNull(action).agent().orNull();
                    }
                }, ret));
            }

            if (nextPart.matches("conditions\\[.+\\]")) {
            } else if (nextPart.equals("stepsSinceLastExecution")) {
                return Functions.compose(new Function<AgentAction, Long>() {
                    @Override
                    public Long apply(final AgentAction o) {
                        return checkNotNull(o).lastCompletionStep();
                    }
                }, ret);
            }
            throw new RuntimeException("AgentAction has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> property(final Iterator<String> parts, final Function<T, AgentProperty<?, ?>> ret) {
        if (parts.hasNext()) {
            final String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<AgentProperty<?, ?>, Agent>() {
                    @Override
                    public Agent apply(final AgentProperty<?, ?> property) {
                        return checkNotNull(property).agent().orNull();
                    }
                }, ret));
            } else
                throw new RuntimeException("AgentProperty has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> simulation(final Iterator<String> parts, final Function<T, Simulation<?>> ret) {
        if (parts.hasNext()) {
            final String nextPart = parts.next();

            Pattern.compile("agents\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);

            if (nextPart.matches("agents\\[.+\\]")) {
                return agent(parts, Functions.compose(new Function<Simulation<?>, Agent>() {
                    @Override
                    public Agent apply(final Simulation<?> simulation) {
                        return Iterables.find(checkNotNull(simulation).getActiveAgents(), new Predicate<Agent>() {
                            @Override
                            public boolean apply(final Agent agent) {
                                return agent.getId() == 0; // TODO: get id from regex
                            }
                        });
                    }
                }, ret));
            } else if ("agentCount".equals(nextPart)) {
                return Functions.compose(new Function<Simulation<?>, Object>() {
                    @Override
                    public Object apply(final Simulation<?> simulation) {
                        return simulation.countAgents();
                    }
                }, ret);
            } else
                throw new RuntimeException("Simulation has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> gene(final Iterator<String> parts, final Function<T, AgentTrait> ret) {
        if (parts.hasNext()) {
            final String nextPart = parts.next();

            if ("value".equals(nextPart)) {
                return Functions.compose(new Function<AgentTrait, Object>() {
                    @Override
                    public Object apply(final AgentTrait gene) {
                        return checkNotNull(gene).get();
                    }
                }, ret);
            } else
                throw new RuntimeException("AgentTrait has no member named " + nextPart);
        } else {
            return ret;
        }
    }

    private <T> Function<T, ?> agent(final Iterator<String> parts, final Function<T, Agent> ret) {
        if (parts.hasNext()) {
            final String nextPart = parts.next();
            Matcher matcher;

            if ("getSimulation".equals(nextPart)) {
                return simulation(parts, Functions.compose(new Function<Agent, Simulation<?>>() {
                    @Override
                    public Simulation<?> apply(final Agent agent) {
                        return checkNotNull(agent).simulation();
                    }
                }, ret));
            }


            matcher = Pattern.compile("properties\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String propertyName = matcher.group(1);
                return property(parts, Functions.compose(new Function<Agent, AgentProperty<?, ?>>() {
                    @Override
                    public AgentProperty apply(final Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getProperty(propertyName);
                    }
                }, ret));
            }

            matcher = Pattern.compile("actions\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String actionName = matcher.group(1);
                return action(parts, Functions.compose(new Function<Agent, AgentAction<?>>() {
                    @Override
                    public AgentAction apply(final Agent agent) {
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
                    public AgentTrait apply(final Agent agent) {
                        // todo: access by name could be replaced by access by index if agent is frozen
                        return checkNotNull(agent).getTrait(geneName);
                    }
                }, ret));
            }

            if ("age".equals(nextPart)) {
                return Functions.compose(new Function<Agent, Object>() {
                    @Override
                    public Object apply(final Agent agent) {
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
