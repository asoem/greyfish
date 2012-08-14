package org.asoem.greyfish.core.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class AgentComponents {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(AgentComponents.class);

    public static <T extends AgentComponent> T createNewInstance(Class<T> clazz) throws RuntimeException {
        try {
            return clazz.getConstructor().newInstance();
        }
        catch (Exception e) {
            LOGGER.error("Could not instantiate object for class {}", clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static <T extends AgentComponent> T createNewInstance(Class<T> clazz, String name) throws RuntimeException {
        T ret = createNewInstance(clazz);
        ret.setName(name);
        return ret;
    }

    @Nullable
    public static AgentComponent findByName(Iterable<? extends AgentComponent> components, final String s) {
        checkNotNull(components);
        checkNotNull(s);

        return Iterables.find(components, new Predicate<AgentComponent>() {
            @Override
            public boolean apply(AgentComponent agentComponent) {
                return agentComponent.getName().equals(s);
            }
        }, null);
    }
}
