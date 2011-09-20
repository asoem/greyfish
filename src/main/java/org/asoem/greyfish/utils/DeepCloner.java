package org.asoem.greyfish.utils;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DeepCloner {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newHashMap();
    private DeepCloneable keyForExpectedPut = null;

    private boolean insertIsRequired() {
        return keyForExpectedPut != null;
    }

    public <T extends DeepCloneable> void setAsCloned(T key, T value) {
        checkNotNull(key);
        checkNotNull(value);
        checkArgument(key == keyForExpectedPut,
                "key was expected to be == " + keyForExpectedPut);
        keyForExpectedPut = null;
        map.put(key, value);
    }

    public <T extends DeepCloneable> T continueWith(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        if (insertIsRequired())
            throw new IllegalStateException(
                    "A clone should add an entry for itself first with cloner.setAsCloned(cloneable, this)");

        if (cloneable == null)
            return null;
        else if (map.containsKey(cloneable)) {
            return clazz.cast(map.get(cloneable));
        }
        else {
            keyForExpectedPut = cloneable;
            return clazz.cast(cloneable.deepClone(this));
        }
    }

    public static <T extends DeepCloneable> T startWith(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        return new DeepCloner().continueWith(cloneable, clazz);
    }
}
