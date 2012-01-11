package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
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
    }
}
