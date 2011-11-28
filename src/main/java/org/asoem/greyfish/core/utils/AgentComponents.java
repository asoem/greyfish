package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

public class AgentComponents {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentComponents.class);

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
}
