package org.asoem.greyfish.scenarios;

import org.asoem.greyfish.cli.ScenarioParameter;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * User: christoph
 * Date: 18.07.12
 * Time: 10:30
 */
public abstract class AbstractInjectedModelFactory  implements ModelFactory {
    @Override
    public Properties getModelProperties() {
        final Properties properties = new Properties();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType().equals(String.class) && field.isAnnotationPresent(ScenarioParameter.class)) {
                try {
                    field.setAccessible(true);
                    properties.setProperty(field.getAnnotation(ScenarioParameter.class).value(), (String) field.get(this));
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }
        return properties;
    }
}
