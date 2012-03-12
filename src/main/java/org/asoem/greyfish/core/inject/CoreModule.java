package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import org.asoem.greyfish.core.io.AgentEventLogger;
import org.asoem.greyfish.core.io.AgentEventLoggerFactory;
import org.asoem.greyfish.core.io.BerkeleyLogger;
import org.asoem.greyfish.core.io.ConsoleLogger;
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
        
        bind(AgentEventLogger.class).to(BerkeleyLogger.class);
        requestStaticInjection(AgentEventLoggerFactory.class);
    }
}
