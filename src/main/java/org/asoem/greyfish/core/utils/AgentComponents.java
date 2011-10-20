package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.base.AbstractBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public class AgentComponents {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentComponents.class);

    public static <T extends AgentComponent> T createNewInstance(Class<T> clazz) throws RuntimeException {
        try {
            if (GFAction.class.isAssignableFrom(clazz)) {
                return clazz.cast(org.asoem.greyfish.utils.base.Builder.class.cast(clazz.getDeclaredMethod("with").invoke(null)).build());
            }
            else if (GFProperty.class.isAssignableFrom(clazz)) {
                return clazz.cast(org.asoem.greyfish.utils.base.Builder.class.cast(clazz.getDeclaredMethod("with").invoke(null)).build());
            }
            else if (GFCondition.class.isAssignableFrom(clazz)) {
                return clazz.cast(org.asoem.greyfish.utils.base.Builder.class.cast(clazz.getDeclaredMethod("all").invoke(null)).build());
            }
            else if (Gene.class.isAssignableFrom(clazz)) {
                return clazz.getConstructor().newInstance();
            }
            else {
                throw new IllegalArgumentException("Type not supported: " + clazz);
            }
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

    public static <T extends AgentComponent> Builder<T> forClass(Class<T> clazz) {
        return new Builder<T>(clazz);
    }

    public static class Builder<T extends AgentComponent> extends AbstractBuilder<T, Builder<T>> {
        private final Class<T> clazz;
        private String name = "";

        private Builder(Class<T> clazz) {
            this.clazz = checkNotNull(clazz);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        @Override
        public T checkedBuild() {
             return createNewInstance(clazz, name);
        }

        public Builder<T> named(String name) {
            this.name = checkNotNull(name);
            return this;
        }
    }
}
