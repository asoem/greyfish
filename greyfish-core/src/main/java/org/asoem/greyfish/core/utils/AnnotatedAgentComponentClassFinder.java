package org.asoem.greyfish.core.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.base.ClassFinder;
import org.asoem.greyfish.utils.base.Tagged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * User: christoph
 * Date: 11.01.12
 * Time: 13:58
 */
public class AnnotatedAgentComponentClassFinder implements AgentComponentClassFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedAgentComponentClassFinder.class);

    @Override
    public Iterable<Class<? extends AgentAction>> getAvailableActions() {
        return findClasses(AgentAction.class, "actions");
    }

    @Override
    public Iterable<Class<? extends AgentProperty>> getAvailableProperties() {
        return findClasses(AgentProperty.class, "properties");
    }

    @Override
    public Iterable<Class<? extends AgentTrait>> getAvailableGenes() {
        return findClasses(AgentTrait.class, "traits");
    }

    @Override
    public Iterable<Class<? extends ActionCondition>> getAvailableConditionClasses() {
        return findClasses(ActionCondition.class, "conditions");
    }

    private <T extends AgentComponent> Iterable<Class<? extends T>> findClasses(final Class<T> clazz, final String tag) {
        try {
            final Iterable<Class<?>> classes = ClassFinder.getInstance().getAll(clazz.getPackage().getName());

            return ImmutableList.copyOf(
                    transform(
                            filter(classes, new Predicate<Class<?>>() {
                                @Override
                                public boolean apply(final Class<?> aClass) {
                                    if (clazz.isAssignableFrom(aClass) && aClass.isAnnotationPresent(Tagged.class)) {
                                        final String[] tags = aClass.getAnnotation(Tagged.class).value();
                                        return Arrays.binarySearch(tags, tag) >= 0;
                                    }
                                    return false;
                                }
                            }), new Function<Class<?>, Class<? extends T>>() {
                        @Override
                        @SuppressWarnings("unchecked") // the previous filter guarantees that the cast will not fail
                        public Class<? extends T> apply(final Class<?> aClass) {
                            return (Class<? extends T>) aClass;
                        }
                    })
            );

        } catch (ClassNotFoundException e) {
            LOGGER.error("Problem in ClassFinder", e);
            return ImmutableList.of();
        }
    }
}
