package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.BuilderInterface;

import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;

public class GFComponents {

    public static <T extends GFComponent> T createNewInstance(Class<T> clazz) {
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
            CORE_LOGGER.error("Could not instantiate class", e);
            System.exit(1);
        }
        return null;
    }
}
