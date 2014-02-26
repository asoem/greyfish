package org.asoem.greyfish.core.model;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;

import java.util.Map;

public final class ModelParameters {

    private ModelParameters() {}

    public static void bind(final Binder binder, final Map<String, String> properties) {
        Binder binderWithSkip = binder.skipSources(ModelParameters.class);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            final ModelParameterImpl annotation = new ModelParameterImpl(key, false);
            final ModelParameterImpl annotationOptional = new ModelParameterImpl(key, true);

            bind(binderWithSkip, annotation, value);
            bind(binderWithSkip, annotationOptional, value);
        }
    }

    private static void bind(final Binder binder, final ModelParameter annotation, final String value) {
        binder.bind(Key.get(String.class, annotation)).toInstance(value);
        binder.bind(Key.get(Integer.class, annotation)).toProvider(new Provider<Integer>() {
            @Override
            public Integer get() {
                return Integer.valueOf(value);
            }
        });

        binder.bind(Key.get(Double.class, annotation)).toProvider(new Provider<Double>() {
            @Override
            public Double get() {
                return Double.valueOf(value);
            }
        });

        binder.bind(Key.get(Float.class, annotation)).toProvider(new Provider<Float>() {
            @Override
            public Float get() {
                return Float.valueOf(value);
            }
        });

        binder.bind(Key.get(Byte.class, annotation)).toProvider(new Provider<Byte>() {
            @Override
            public Byte get() {
                return Byte.valueOf(value);
            }
        });
    }
}
