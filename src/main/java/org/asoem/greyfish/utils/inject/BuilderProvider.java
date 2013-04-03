package org.asoem.greyfish.utils.inject;

import com.google.inject.Provider;
import org.asoem.greyfish.utils.base.Builder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 15.01.13
 * Time: 14:53
 */
public class BuilderProvider<T> implements Provider<T> {

    private final Builder<T> builder;

    private BuilderProvider(Builder<T> builder) {
        this.builder = builder;
    }


    public static <T> Provider<T> forBuilder(Builder<T> builder) {
        checkNotNull(builder);
        return new BuilderProvider<T>(builder);
    }

    @Override
    public T get() {
        return builder.build();
    }
}
