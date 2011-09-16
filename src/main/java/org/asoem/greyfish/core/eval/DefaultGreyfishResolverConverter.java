package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;

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
public enum DefaultGreyfishResolverConverter implements ResolverConverter {
    INSTANCE;

    private static final Splitter GOM_SPLITTER = Splitter.on('.').trimResults(); // TODO: Exclude dots in parentheses

    @Override
    public <T extends GFComponent> Function<T, ?> get(String varName, final Class<T> context) {
        checkNotNull(varName);
        checkNotNull(context);

        Iterator<String> gomParts = GOM_SPLITTER.split(varName).iterator();

        if (gomParts.hasNext()) {

            String root = gomParts.next();

            if ("this".equals(root)) {
                if (GFAction.class.equals(context)) {
                    return action(gomParts, new Function<T, GFAction>() {
                        @Override
                        public GFAction apply(@Nullable T gfComponent) {
                            return GFAction.class.cast(gfComponent);
                        }
                    });
                }
                else if (GFProperty.class.equals(context)) {
                    return property(gomParts, new Function<T, GFProperty>() {
                        @Override
                        public GFProperty apply(@Nullable T gfComponent) {
                            return GFProperty.class.cast(gfComponent);
                        }
                    });
                }
            }
            else if ("sim".equals(root)) {
                if (Simulation.class.equals(context)) {
                    return simulation(gomParts, new Function<T, Simulation>() {
                        @Override
                        public Simulation apply(@Nullable T gfComponent) {
                            return gfComponent.getAgent().getSimulation();
                            // TODO: We should have direct access to simulation object through a component
                        }
                    });
                }
            }
        }

        throw new RuntimeException("");
    }

    private <T extends GFComponent> Function<T, ?> action(Iterator<String> parts, Function<T, GFAction> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<GFAction, Agent>() {
                    @Override
                    public Agent apply(@Nullable GFAction action) {
                        return action.getAgent();
                    }
                }, ret));
            }

            if (nextPart.matches("conditions\\[.+\\]")) {
            }
            throw new RuntimeException("GFAction has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T extends GFComponent> Function<T, ?> property(Iterator<String> parts, Function<T, GFProperty> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            if ("agent".equals(nextPart)) {
                return agent(parts, Functions.compose(new Function<GFProperty, Agent>() {
                    @Override
                    public Agent apply(@Nullable GFProperty property) {
                        return property.getAgent();
                    }
                }, ret));
            }

            if ("".equals(nextPart)) {

            }
            throw new RuntimeException("GFProperty has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T extends GFComponent> Function<T, ?> simulation(Iterator<String> parts, Function<T, Simulation> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();

            Pattern.compile("agents\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);

            if (nextPart.matches("agents\\[.+\\]")) {
                return agent(parts, Functions.compose(new Function<Simulation, Agent>() {
                    @Override
                    public Agent apply(@Nullable Simulation simulation) {
                        return Iterables.find(simulation.getAgents(), new Predicate<Agent>() {
                            @Override
                            public boolean apply(Agent agent) {
                                return agent.getId() == 0; // TODO: get id from regex
                            }
                        });
                    }
                }, ret));
            }

            throw new RuntimeException("Simulation has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T extends GFComponent> Function<T, ?> gene(Iterator<String> parts, Function<T, Gene> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            throw new RuntimeException("Gene has no member named " + nextPart);
        }
        else {
            return ret;
        }
    }

    private <T extends GFComponent> Function<T, ?> agent(Iterator<String> parts, Function<T, Agent> ret) {
        if (parts.hasNext()) {
            String nextPart = parts.next();
            Matcher matcher;

            if ("simulation".equals(nextPart)) {
                return simulation(parts, Functions.compose( new Function<Agent, Simulation>() {
                    @Override
                    public Simulation apply(@Nullable Agent agent) {
                        return agent.getSimulation();
                    }
                },ret));
            }


            matcher = Pattern.compile("properties\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String propertyName = matcher.group(1);
                return property(parts, Functions.compose(new Function<Agent, GFProperty>() {
                    @Override
                    public GFProperty apply(@Nullable Agent agent) {
                        return agent.getProperty(propertyName, GFProperty.class);
                    }
                }, ret));
            }

            matcher = Pattern.compile("actions\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String actionName = matcher.group(1);
                return action(parts, Functions.compose(new Function<Agent, GFAction>() {
                    @Override
                    public GFAction apply(@Nullable Agent agent) {
                        return agent.getAction(actionName, GFAction.class);
                    }
                }, ret));
            }

            matcher = Pattern.compile("genes\\[[\"'](\\w+)[\"']\\]").matcher(nextPart);
            if (matcher.matches()) {
                final String geneName = matcher.group(1);
                return gene(parts, Functions.compose(new Function<Agent, Gene>() {
                    @Override
                    public Gene apply(@Nullable Agent agent) {
                        return agent.getGene(geneName, Gene.class);
                    }
                }, ret));
            }

            if ("age".equals(nextPart)) {
                return Functions.compose( new Function<Agent, Object>() {
                    @Override
                    public Object apply(@Nullable Agent agent) {
                        return agent.getAge();
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
