package org.asoem.greyfish.utils.base;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 10:52
 */
public final class UpdateRequests {

    public static OutdateableUpdateRequest<Object> atomicRequest(final boolean initial) {
        return new OutdateableUpdateRequest<Object>() {

            private final AtomicBoolean b = new AtomicBoolean(initial);

            @Override
            public void outdate() {
                b.set(true);
            }

            @Override
            public void done() {
                b.set(false);
            }

            @Override
            public boolean apply(@Nullable Object input) {
                return b.get();
            }
        };
    }
}
