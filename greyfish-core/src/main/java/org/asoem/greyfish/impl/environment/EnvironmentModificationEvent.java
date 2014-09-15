package org.asoem.greyfish.impl.environment;

import org.asoem.greyfish.core.environment.Environment;

/**
 * The common type for all events which happen in and are published by {@link org.asoem.greyfish.core.environment.Environment
 * environments}
 */
public interface EnvironmentModificationEvent {
    Environment<?> getEnvironment();
}
