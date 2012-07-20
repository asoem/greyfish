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
public class ModelParameters {
    private ModelParameters() {}

    /**
     * Creates a {@link ModelParameter} annotation with {@code name} as the value.
     */
    public static ModelParameter named(String name) {
        return new ModelParameterImpl(name);
    }

    /**
     * Creates a constant binding to {@code @Named(key)} for each entry in
     * {@code properties}.
     */
    public static void bindProperties(Binder binder, Map<String, String> properties) {
        binder = binder.skipSources(ModelParameter.class);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            binder.bind(Key.get(String.class, new ModelParameterImpl(key))).toInstance(value);
        }
    }

    /**
     * Creates a constant binding to {@code @Named(key)} for each property. This
     * method binds all properties including those inherited from
     * {@link Properties#defaults defaults}.
     */
    public static void bindProperties(Binder binder, Properties properties) {
        binder = binder.skipSources(ModelParameter.class);

        // use enumeration to include the default properties
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String propertyName = (String) e.nextElement();
            String value = properties.getProperty(propertyName);
            binder.bind(Key.get(String.class, new ModelParameterImpl(propertyName))).toInstance(value);
        }
    }
}
