package org.asoem.greyfish.cli;

import com.google.common.collect.Maps;
import org.asoem.greyfish.core.simulation.Model;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:51
 */
public final class ModelParameters {

    private ModelParameters() {}

    public static Map<String, Object> extract(Model<?> model) {
        final Map<String, Object> map = Maps.newHashMap();
        for (Field field : model.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ModelParameter.class)) {
                try {
                    field.setAccessible(true);
                    final String annotationValue = field.getAnnotation(ModelParameter.class).value();
                    final String key = (annotationValue.isEmpty()) ? field.getName() : annotationValue;
                    map.put(key, field.get(model));
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }
        return map;
    }
}
