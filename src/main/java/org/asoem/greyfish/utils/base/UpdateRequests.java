package org.asoem.greyfish.utils.base;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 10:52
 */
public final class UpdateRequests {

    private UpdateRequests() {}

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

    public static <T> UpdateRequest<T> once() {
        return new UpdateOnce<T>();
    }

    private static <T> UpdateRequest<T> synchronizedRequest(UpdateRequest<T> updateRequest) {
        return new SynchronizedRequest<T>(updateRequest);
    }

    private static class UpdateOnce<T> implements UpdateRequest<T> {
        private boolean outdated = true;

        @Override
        public void done() {
            outdated = false;
        }

        @Override
        public boolean apply(@Nullable T t) {
            return outdated;
        }
    }

    private static class SynchronizedRequest<T> implements UpdateRequest<T> {
        private final UpdateRequest<T> updateRequest;

        public SynchronizedRequest(UpdateRequest<T> updateRequest) {
            this.updateRequest = updateRequest;
        }

        @Override
        public void done() {
            synchronized (this) {
                updateRequest.done();
            }
        }

        @Override
        public boolean apply(@Nullable T t) {
            synchronized (this) {
                return updateRequest.apply(t);
            }
        }
    }
}
