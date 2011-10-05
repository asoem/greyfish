package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.BuilderInterface;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class GFComponents {

    private static final Logger LOGGER = LoggerFactory.getLogger(GFComponents.class);

    public static <T extends AgentComponent> T createNewInstance(Class<T> clazz) throws RuntimeException {
        try {
            if (GFAction.class.isAssignableFrom(clazz)) {
                return clazz.cast(BuilderInterface.class.cast(clazz.getDeclaredMethod("with").invoke(null)).build());
            }
            else if (GFProperty.class.isAssignableFrom(clazz)) {
                return clazz.cast(BuilderInterface.class.cast(clazz.getDeclaredMethod("with").invoke(null)).build());
            }
            else if (GFCondition.class.isAssignableFrom(clazz)) {
                return clazz.cast(BuilderInterface.class.cast(clazz.getDeclaredMethod("trueIf").invoke(null)).build());
            }
            else {
                throw new IllegalArgumentException("Type not supported" + clazz);
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not instantiate class", e);
            throw new RuntimeException(e);
        }
    }

    public static <T extends AgentComponent> T createNewInstance(Class<T> clazz, String name) throws RuntimeException {
        T ret = createNewInstance(clazz);
        ret.setName(name);
        return ret;
    }

    public static void rebaseAll(Iterable<? extends AgentComponent> iterable, @Nullable Agent agent) {
        for (AgentComponent component : iterable)
            component.setAgent(agent);
    }

    public static <T extends AgentComponent> Builder<T> forClass(Class<T> clazz) {
        return new Builder<T>(clazz);
    }

    public static class Builder<T extends AgentComponent> implements BuilderInterface<T> {
        private final Class<T> clazz;
        private String name = "";

        private Builder(Class<T> clazz) {
            this.clazz = checkNotNull(clazz);
        }

        @Override
        public T build() {
             return createNewInstance(clazz, name);
        }

        public Builder<T> named(String name) {
            this.name = checkNotNull(name);
            return this;
        }
    }
}
