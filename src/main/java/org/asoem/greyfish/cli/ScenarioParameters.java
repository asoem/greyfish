package org.asoem.greyfish.cli;

import com.google.inject.Binder;
import com.google.inject.Key;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 15:16
 */
public class ScenarioParameters {
    private ScenarioParameters() {}

    /**
     * Creates a {@link ScenarioParameter} annotation with {@code name} as the value.
     */
    public static ScenarioParameter named(String name) {
        return new ScenarioParameterImpl(name);
    }

    /**
     * Creates a constant binding to {@code @Named(key)} for each entry in
     * {@code properties}.
     */
    public static void bindProperties(Binder binder, Map<String, String> properties) {
        binder = binder.skipSources(ScenarioParameter.class);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            binder.bind(Key.get(String.class, new ScenarioParameterImpl(key))).toInstance(value);
        }
    }

    /**
     * Creates a constant binding to {@code @Named(key)} for each property. This
     * method binds all properties including those inherited from
     * {@link Properties#defaults defaults}.
     */
    public static void bindProperties(Binder binder, Properties properties) {
        binder = binder.skipSources(ScenarioParameter.class);

        // use enumeration to include the default properties
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String propertyName = (String) e.nextElement();
            String value = properties.getProperty(propertyName);
            binder.bind(Key.get(String.class, new ScenarioParameterImpl(propertyName))).toInstance(value);
        }
    }
}
