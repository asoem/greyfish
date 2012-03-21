package org.asoem.greyfish.core.io;

import com.google.common.primitives.Doubles;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.serializers.UUIDSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.netflix.astyanax.util.TimeUUIDUtils;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.IOError;
import java.util.UUID;

/**
 * User: christoph
 * Date: 20.03.12
 * Time: 10:56
 */
public class CassandraLogger implements SimulationLogger {

    private final Keyspace keyspace;
    private final ColumnFamily<UUID, String> cf_agent_events;
    private final AstyanaxContext<Keyspace> context;

    @Inject
    protected CassandraLogger(@Assisted Simulation simulation) {

        context = new AstyanaxContext.Builder()
                .forCluster("ClusterName")
                .forKeyspace("greyfish")
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                        .setDiscoveryType(NodeDiscoveryType.NONE)
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("MyConnectionPool")
                        .setPort(9160)
                        .setMaxConnsPerHost(1)
                        .setSeeds("127.0.0.1:9160")
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());

        context.start();
        keyspace = context.getEntity();

        cf_agent_events = new ColumnFamily<UUID, String>(
                "agent_events",              // Column Family Name
                UUIDSerializer.get(),   // Key Serializer
                StringSerializer.get());  // Column Serializer

    }

    /*
    create column family agent_events
  with comparator = UTF8Type
  and key_validation_class = TimeUUIDType
  and column_metadata = [{column_name: agent_id, validation_class:LongType},
                      {column_name: location, validation_class:UTF8Type},
                      {column_name: message, validation_class:UTF8Type},
                      {column_name: population, validation_class:UTF8Type},
                      {column_name: simulation_id, validation_class:UTF8Type},
                      {column_name: source, validation_class:UTF8Type},
                      {column_name: title, validation_class:UTF8Type}];
     */

    @Override
    public void addEvent(AgentEvent event) {
        MutationBatch mutationBatch = keyspace.prepareMutationBatch();

        mutationBatch.withRow(cf_agent_events, TimeUUIDUtils.getTimeUUID(event.getCreatedAt().getTime()))
                .putColumn("simulation_id", event.getSimulationId(), null)
                .putColumn("agent_id", (long) event.getAgentId(), null)
                .putColumn("source", event.getSourceOfEvent().toString(), null)
                .putColumn("location", Doubles.join(",", event.getLocatable2D()), null)
                .putColumn("population", event.getAgentPopulationName(), null)
                .putColumn("title", event.getEventTitle(), null)
                .putColumn("message", event.getEventMessage(), null)
        ;

        try {
            mutationBatch.execute();
        } catch (ConnectionException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void close() {
        context.shutdown();
    }
}
