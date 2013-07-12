package org.asoem.greyfish.core.model;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:51
 */
public final class ModelParameters {

    private ModelParameters() {}

    public static Map<String, Object> extract(SimulationModel<?> model) {
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
