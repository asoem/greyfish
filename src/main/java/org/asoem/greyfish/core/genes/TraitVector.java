package org.asoem.greyfish.core.genes;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.utils.base.TypedSupplier;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:30
 */
public class TraitVector<T> implements TypedSupplier<T> {
    @Nullable
    private final T value;
    private final double recombinationProbability;
    private final TypeToken<T> typeToken;

    private TraitVector(@Nullable T value, double recombinationProbability, TypeToken<T> typeToken) {
        this.value = value;
        this.recombinationProbability = recombinationProbability;
        this.typeToken = typeToken;
    }

    @Nullable
    @Override
    public T get() {
        return value;
    }

    public double getRecombinationProbability() {
        return recombinationProbability;
    }

    @Override
    public TypeToken<T> getValueType() {
        return typeToken;
    }

    public static <T> TraitVector<T> create(@Nullable T value, double recombinationProbability, TypeToken<T> typeToken) {
        checkNotNull(typeToken);
        return new TraitVector<T>(value, recombinationProbability, typeToken);
    }
}
