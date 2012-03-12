package org.asoem.greyfish.core.io;

import com.google.common.collect.AbstractIterator;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import java.io.File;
import java.util.Iterator;

/**
 * User: christoph
 * Date: 09.03.12
 * Time: 16:23
 */
public class BerkeleyLogger implements AgentEventLogger {

    private static final String DATA_DB_DIR_NAME = System.getProperty("user.home") + "/" + "greyfish";
    private static final String DB_NAME = "greyfish";
    private static final String STORE_NAME = "agent_events";

    private PrimaryIndex<Integer, AgentEvent> eventById;
    private Environment environment;
    private EntityStore entityStore;

    public BerkeleyLogger() {
        setup();
    }

    private void setup() {
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        // perform other environment configurations
        File file = new File(DATA_DB_DIR_NAME);
        // perform other database configurations
        this.environment = new Environment(file, environmentConfig);
        final StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        entityStore = new EntityStore(environment, STORE_NAME, storeConfig);
        eventById = entityStore.getPrimaryIndex(Integer.class, AgentEvent.class);
    }

    @Override
    public void addEvent(AgentEvent event) {
        eventById.putNoReturn(event);
    }

    @Override
    public void close() {
        entityStore.close();
        environment.close();
    }

    public Iterable<AgentEvent> getLoggedEvents() {
        return new Iterable<AgentEvent>() {
            @Override
            public Iterator<AgentEvent> iterator() {
                return new AbstractIterator<AgentEvent>() {
                    final EntityCursor<AgentEvent> entities = eventById.entities();
                    @Override
                    protected AgentEvent computeNext() {
                        final AgentEvent next = entities.next();
                        if (next == null) {
                            entities.close();
                            return endOfData();
                        }
                        else
                            return next;
                    }
                };
            }
        };
    }
}
