package org.asoem.greyfish.core.inject;

import com.google.inject.AbstractModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.core.io.persistence.SimpleXMLPersister;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:29
 */
public class PersistenceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Persister.class).to(SimpleXMLPersister.class);
    }
}
