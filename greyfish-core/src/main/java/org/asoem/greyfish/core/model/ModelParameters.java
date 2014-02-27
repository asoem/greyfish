package org.asoem.greyfish.core.model;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Map;

public final class ModelParameters {

    private static final ClassToTransformerMap defaultTransformerMap = new ClassToTransformerMap.Builder()
            .put(String.class, Functions.<String>identity())
            .put(Integer.class, new Function<String, Integer>() {
                @Nullable
                @Override
                public Integer apply(@Nullable final String input) {
                    return Integer.valueOf(input);
                }
            })
            .put(Double.class, new Function<String, Double>() {
                @Nullable
                @Override
                public Double apply(@Nullable final String input) {
                    return Double.valueOf(input);
                }
            })
            .put(Float.class, new Function<String, Float>() {
                @Nullable
                @Override
                public Float apply(@Nullable final String input) {
                    return Float.valueOf(input);
                }
            })
            .put(Boolean.class, new Function<String, Boolean>() {
                @Nullable
                @Override
                public Boolean apply(@Nullable final String input) {
                    return Boolean.valueOf(input);
                }
            })
            .build();

    private ModelParameters() {
        throw new UnsupportedOperationException();
    }

    public static void bind(final Binder binder, final Map<String, String> properties) {
        bind(binder, properties, ClassToTransformerMap.builder().build());
    }

    public static void bind(final Binder binder, final Map<String, String> properties,
                            final ClassToTransformerMap transformerMap) {
        final ClassToTransformerMap mergedMap =
                ClassToTransformerMap.merge(defaultTransformerMap, transformerMap);

        final Binder binderWithSkip = binder.skipSources(ModelParameters.class);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            final ModelParameter annotation = new ModelParameterImpl(key, false);
            bind(binderWithSkip, annotation, value, mergedMap);

            final ModelParameter annotationOptional = new ModelParameterImpl(key, true);
            bind(binderWithSkip, annotationOptional, value, mergedMap);

            final Named namedAnnotation = Names.named(key);
            bind(binderWithSkip, namedAnnotation, value, mergedMap);
        }
    }

    private static void bind(final Binder binderWithSkip, final Annotation annotation,
                             final String value, final ClassToTransformerMap transformerMap) {
        for (TypeLiteral<?> typeLiteral : transformerMap.keySet()) {
            bind(binderWithSkip, annotation, transformerMap, typeLiteral, value);
        }
    }

    private static <T> void bind(final Binder binder, final Annotation annotation,
                                 final ClassToTransformerMap transformerMap,
                                 final TypeLiteral<T> typeLiteral, final String value) {
        binder.bind(Key.get(typeLiteral, annotation)).toProvider(new Provider<T>() {
            @Override
            public T get() {
                return transformerMap.getTransformer(typeLiteral).apply(value);
            }
        });
    }

    public static class ClassToTransformerMap extends ForwardingMap<TypeLiteral<?>, Function<String, ?>> {

        private final Map<TypeLiteral<?>, Function<String, ?>> map;

        private ClassToTransformerMap(final Map<TypeLiteral<?>, Function<String, ?>> map) {
            this.map = map;
        }

        @Override
        protected Map<TypeLiteral<?>, Function<String, ?>> delegate() {
            return map;
        }

        @SuppressWarnings("unchecked")
        protected <T> Function<String, T> getTransformer(final TypeLiteral<T> aClass) {
            return (Function<String, T>) map.get(aClass);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private final Map<TypeLiteral<?>, Function<String, ?>> map = Maps.newHashMap();

            public <T> Builder put(final Class<T> classToBind, final Function<String, T> transformer) {
                map.put(TypeLiteral.get(classToBind), transformer);
                return this;
            }

            public <T> Builder put(final TypeLiteral<T> typeToBind, final Function<String, T> transformer) {
                map.put(typeToBind, transformer);
                return this;
            }

            public ClassToTransformerMap build() {
                return new ClassToTransformerMap(ImmutableMap.copyOf(map));
            }

            private <T> Builder put(final TypeLiteral<T> typeLiteral, final ClassToTransformerMap map1) {
                map.put(typeLiteral, map1.getTransformer(typeLiteral));
                return this;
            }
        }

        public static ClassToTransformerMap merge(final ClassToTransformerMap map1, final ClassToTransformerMap map2) {
            final Builder builder = new Builder();
            for (TypeLiteral<?> typeLiteral : map1.keySet()) {
                builder.put(typeLiteral, map1);
            }
            for (TypeLiteral<?> typeLiteral : map2.keySet()) {
                builder.put(typeLiteral, map1);
            }
            return builder.build();
        }
    }
}
