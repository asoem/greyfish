package org.asoem.greyfish.core.model;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.primitives.Primitives;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.asoem.greyfish.core.eval.Expression;
import org.asoem.greyfish.core.eval.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code TypeListener} which maps injects fields annotated with {@code ModelParameter} to values.
 */
public final class ModelParameterTypeListener implements TypeListener {
    private final Function<? super String, ExpressionFactory> expressionFactoryResolver;
    private final Map<String, String> overwriteMap;

    /**
     * Create a new instance which maps {@link ModelParameter} annotated fields
     * named like a key of the map to the value mapped to the key converted to the type of the field.
     * @param overwriteMap the mapping of field keys to values
     */
    public ModelParameterTypeListener(final Map<String, String> overwriteMap) {
        this.overwriteMap = checkNotNull(overwriteMap);
        this.expressionFactoryResolver = Functions.constant(null);
    }

    @Override
    public <T> void hear(final TypeLiteral<T> typeLiteral, final TypeEncounter<T> typeEncounter) {
        for (final Field field : typeLiteral.getRawType().getDeclaredFields()) {
            if (field.isAnnotationPresent(ModelParameter.class)) {
                final ModelParameter fieldAnnotation = field.getAnnotation(ModelParameter.class);
                final String annotationValue = fieldAnnotation.value();
                final String parameterName = (annotationValue.isEmpty()) ? field.getName() : annotationValue;

                if (overwriteMap.containsKey(parameterName)) {
                    final Object value = convertInputString(overwriteMap.get(parameterName), field.getType());
                    typeEncounter.register(new ModelParameterFieldInjector<T>(field, value));
                } else {
                    if (!fieldAnnotation.optional()) {
                        throw new IllegalArgumentException("Found no entry for required model parameter field " + field + " annotated with " + fieldAnnotation);
                    }
                }
            }
        }
    }

    private Object convertInputString(final String s, final Class<?> type) {
        if (type == String.class) {
            return s;
        } else if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            final Class<?> wrapperClass = Primitives.wrap(type);
            try {
                final Method valueOf = wrapperClass.getMethod("valueOf", String.class);
                return valueOf.invoke(wrapperClass, s);
            } catch (Exception e) {
                final String message = "Failed to convert String "
                        + s + " into type " + wrapperClass
                        + ".\nCaused by: " + e;
                throw new AssertionError(message);
            }
        } else {
            final ExpressionFactory expressionFactory = expressionFactoryResolver.apply(s);
            if (expressionFactory != null) {
                final Expression expression = expressionFactory.compile(s);
                return expression.evaluate().get();
            } else {
                throw new UnsupportedOperationException("No rule implemented to convert type String to type " + type);
            }
        }
    }

    private static class ModelParameterFieldInjector<T> implements MembersInjector<T> {
        private static final Logger LOGGER = LoggerFactory.getLogger(ModelParameterTypeListener.class);
        private final Field field;
        private final Object newFiledValue;

        private ModelParameterFieldInjector(final Field field, final Object newFiledValue) {
            this.field = field;
            this.newFiledValue = newFiledValue;
            field.setAccessible(true);
        }

        @Override
        public void injectMembers(final T t) {
            try {
                field.set(t, newFiledValue);
                LOGGER.debug("Injected field {} with value {}", field, newFiledValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
