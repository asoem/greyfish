package org.asoem.greyfish.core.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AgentComponents {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentComponents.class);

    private AgentComponents() {}

    public static <T extends AgentComponent> T createNewInstance(final Class<T> clazz) throws RuntimeException {
        try {
            return clazz.getConstructor().newInstance();
        }
        catch (Exception e) {
            LOGGER.error("Could not instantiate object for class {}", clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static <T extends AgentComponent> T createNewInstance(final Class<T> clazz, final String name) throws RuntimeException {
        final T ret = createNewInstance(clazz);
        ret.setName(name);
        return ret;
    }

    @Nullable
    public static AgentComponent findByName(final Iterable<? extends AgentComponent> components, final String s) {
        checkNotNull(components);
        checkNotNull(s);

        return Iterables.find(components, new Predicate<AgentComponent>() {
            @Override
            public boolean apply(final AgentComponent agentComponent) {
                return agentComponent.getName().equals(s);
            }
        }, null);
    }
}
