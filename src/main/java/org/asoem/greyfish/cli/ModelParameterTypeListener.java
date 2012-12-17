package org.asoem.greyfish.cli;

import com.google.common.primitives.Primitives;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 10:57
 */
public class ModelParameterTypeListener implements TypeListener {
    private final Map<String, String> properties;

    public ModelParameterTypeListener(Map<String, String> properties) {
        this.properties = checkNotNull(properties);
    }

    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
            if (field.isAnnotationPresent(ModelParameter.class)) {
                final String annotationValue = field.getAnnotation(ModelParameter.class).value();
                final String key = (annotationValue.isEmpty()) ? field.getName() : annotationValue;
                if (properties.containsKey(key)) {
                    final Object value = convert(properties.get(key), field.getType());
                    typeEncounter.register(new ModelParameterInjector<T>(field, value));
                }
            }
        }
    }

    private static Object convert(String s, Class<?> type) {
        if (type == String.class)
            return s;

        else if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            final Class<?> wrapperClass = Primitives.wrap(type);
            try {
                final Method valueOf = wrapperClass.getMethod("valueOf", String.class);
                return valueOf.invoke(wrapperClass, s);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert String " + s + " into type " + wrapperClass, e);
            }
        }

        else
            throw new RuntimeException("No rule implemented to convert type String to type " + type);
    }

    private static class ModelParameterInjector<T> implements MembersInjector<T> {
        private final Field field;
        private final Object o;

        public ModelParameterInjector(Field field, Object o) {
            this.field = field;
            this.o = o;
            field.setAccessible(true);
        }

        @Override
        public void injectMembers(T t) {
            try {
                field.set(t, o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
