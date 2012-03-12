package org.asoem.greyfish.core.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * User: christoph
 * Date: 09.03.12
 * Time: 16:23
 */
public class BerkeleyLogger implements SimulationLogger {

    private static final String DATA_DB_DIR_NAME = System.getProperty("user.home") + "/" + "greyfish";
    private static final String STORE_NAME = "agent_events";
    private static final Logger LOGGER = LoggerFactory.getLogger(BerkeleyLogger.class);

    private PrimaryIndex<Integer, AgentEvent> eventById;
    private Environment environment;
    private EntityStore entityStore;
    
    private final static Map<BerkeleyLogger, Simulation> ACTIVE_LOGGERS = Maps.newHashMap();

    @Inject
    protected BerkeleyLogger(@Assisted Simulation simulation) {
        ACTIVE_LOGGERS.put(this, simulation);
        setup();
        LOGGER.debug("BerkeleyLogger initialized for {}", simulation);
    }

    private void setup() {
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        // perform other environment configurations
        File file = new File(DATA_DB_DIR_NAME);
        try {
            if (! file.exists() && file.mkdir())
                LOGGER.debug("Directory created {}", file);
        } catch (SecurityException e) {
            LOGGER.error("Could not create directory", e);
        }
        // perform other database configurations
        this.environment = new Environment(file, environmentConfig);
        LOGGER.debug("Opened environment {} at {}", environment, file);

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
        if (!ACTIVE_LOGGERS.containsKey(this))
            LOGGER.warn("This logger is not in map. Already closed?");
        else {
            ACTIVE_LOGGERS.remove(this);
            if (ACTIVE_LOGGERS.size() == 0) {
                entityStore.close();
                environment.close();
                LOGGER.debug("Closed environment {}", environment);
            }
        }
    }

    public Iterable<AgentEvent> getLoggedEvents() {
        final EntityCursor<AgentEvent> entities = eventById.entities();
        try {
            return ImmutableList.copyOf(entities);
        } finally {
            entities.close();
        }
    }
}
