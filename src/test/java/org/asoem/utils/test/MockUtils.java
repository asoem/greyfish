package org.asoem.utils.test;

import org.asoem.greyfish.utils.base.CloneMap;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.mockito.internal.util.MockUtil;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 11.12.12
 * Time: 17:13
 */
public class MockUtils {
    private MockUtils() {}

    public static CloneMap mockAwareCloner() {
        return new CloneMap() {

            private final DeepCloner delegate = DeepCloner.newInstance();
            private final MockUtil mockUtil = new MockUtil();

            @Override
            public <T extends DeepCloneable> void addClone(T original, T clone) {
                delegate.addClone(original, clone);
            }

            @Nullable
            @Override
            public <T extends DeepCloneable> T getClone(@Nullable T cloneable, Class<T> clazz) {
                if (mockUtil.isMock(cloneable)) {
                    return cloneable; // TODO: Should return a clone of this mock
                }
                else return delegate.getClone(cloneable, clazz);
            }

            @Nullable
            @Override
            public DeepCloneable getClone(@Nullable DeepCloneable cloneable) {
                if (mockUtil.isMock(cloneable)) {
                    return cloneable; // TODO: Should return a clone of this mock
                }
                else return delegate.getClone(cloneable, this);
            }
        };
    }
}
