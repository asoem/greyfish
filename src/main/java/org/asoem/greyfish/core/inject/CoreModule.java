package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.asoem.greyfish.core.io.*;
import org.asoem.greyfish.core.utils.AgentComponentClassFinder;
import org.asoem.greyfish.core.utils.AnnotatedAgentComponentClassFinder;

/**
 * User: christoph
 * Date: 11.01.12
 * Time: 14:37
 */
public class CoreModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AgentComponentClassFinder.class).to(AnnotatedAgentComponentClassFinder.class).asEagerSingleton();

        install(new FactoryModuleBuilder()
                .implement(SimulationLogger.class, LoadLogger.class)
                .build(SimulationLoggerFactory.class));

        requestStaticInjection(SimulationLoggerProvider.class);
    }
}
