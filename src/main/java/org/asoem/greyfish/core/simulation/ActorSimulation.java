package org.asoem.greyfish.core.simulation;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.Channel;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.routing.CyclicIterator;
import akka.routing.InfiniteIterator;
import akka.routing.UntypedDispatcher;
import akka.routing.UntypedLoadBalancer;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import javolution.util.FastList;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentMessage;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.*;
import org.asoem.greyfish.utils.DeepCloner;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static akka.actor.Actors.actorOf;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.10.11
 * Time: 11:46
 */
public class ActorSimulation implements Simulation {

    private final Scenario scenario;
    private String name;
    private int steps;
    private Supplier<Integer> idSupplier = new Supplier<Integer>() {
        int id;
        @Override
        public Integer get() {
            return ++id;
        }
    };
    private final List<Agent> agentList = FastList.newInstance();
    private final TiledSpace space;
    private final ActorRef simulationMessageRouter = (ActorRef) Actors.actorOf(new Creator<akka.actor.Actor>() {
        public UntypedActor create() {
            return new SimulationMessageRouter(ActorSimulation.this);
        }
    }).start();

    public ActorSimulation(Scenario scenario) {
        this.scenario = checkNotNull(scenario);
        this.space = TiledSpace.copyOf(scenario.getSpace());

        init();
    }

    private void init() {
        for (Placeholder placeholder : scenario.getPlaceholder()) {
            insertAgent(placeholder.getPopulation(), placeholder);
        }
    }

    private void insertAgent(Population population, Placeholder placeholder) {
        insertAgent(population, placeholder, null);
    }

    private void insertAgent(Population population, Object2D placeholder1, @Nullable Genome<?> genome) {
        Agent clone = createClone(population);

        // spacial
        clone.setAnchorPoint(placeholder1.getCoordinates());
        clone.setOrientation(placeholder1.getOrientation());

        clone.prepare(this);

        if (genome != null)
            clone.injectGamete(genome);

        agentList.add(clone);
    }

    private Agent createClone(final Population population) {
        return DeepCloner.clone(Iterables.find(getPrototypes(), new Predicate<Agent>() {
            @Override
            public boolean apply(@Nullable Agent agent) {
                return agent.getPopulation().equals(population);
            }
        }), Agent.class);
    }

    @Override
    public int numberOfPopulations() {
        return getPrototypes().size();
    }

    @Override
    public Iterable<MovingObject2D> findObjects(Coordinates2D coordinates, double radius) {
        return space.findObjects(coordinates, radius);
    }

    @Override
    public Iterable<Agent> getAgents(Population population) {
        return agentList;
    }

    @Override
    public Collection<Agent> getAgents() {
        return agentList;
    }

    @Override
    public void removeAgent(Agent agent) {
        ((Channel<Object>) simulationMessageRouter).tell(new RemoveAgentMessage(agent));
    }

    @Override
    public int countAgents() {
        return agentList.size();
    }

    @Override
    public int countAgents(final Population population) {
        return Iterables.frequency(Iterables.transform(agentList, new Function<Agent, Population>() {
            @Override
            public Population apply(@Nullable Agent agent) {
                return agent.getPopulation();
            }
        }), population);
    }

    @Override
    public void addSimulationListener(SimulationListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSimulationListener(SimulationListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int generateAgentID() {
        return idSupplier.get();
    }

    @Override
    public void createAgent(Population population, Coordinates2D coordinates, Genome genome) {
        ((Channel<Object>) simulationMessageRouter).tell(new CreateAgentMessage(population, coordinates, genome));
    }

    @Override
    public Set<Agent> getPrototypes() {
        return scenario.getPrototypes();
    }

    @Override
    public TiledSpace getSpace() {
        return space;
    }

    @Override
    public int getSteps() {
        return steps;
    }

    @Override
    public synchronized void step() {
        CountDownLatch latch = new CountDownLatch(1);
        for (Agent agent : agentList) {
            ((Channel<Object>) simulationMessageRouter).tell(new ExecuteAgentMessage(agent));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        steps += 1;
    }

    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void deliverMessage(ACLMessage<Agent> message) {
        ((Channel<Object>) this.simulationMessageRouter).tell(new DeliverAgentMessage(message));
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    @Override
    public boolean hasName(String s) {
        return Objects.equal(name, s);
    }

    private static class SimulationMessageRouter extends UntypedDispatcher {

        private ActorRef agentExecutionPool;

        private ActorRef defaultExecutor;

        private SimulationMessageRouter(final ActorSimulation simulation) {
            defaultExecutor  = (ActorRef) actorOf(new Creator<akka.actor.Actor>() {
                @Override
                public UntypedActor create() {
                    return new DefaultExecutor(simulation);
                }
            }).start();
            agentExecutionPool = (ActorRef) actorOf(new Creator<akka.actor.Actor>() {
                @Override
                public UntypedActor create() {
                    return new AgentExecutorPool(4);
                }
            }).start();
        }

        @Override
        public ActorRef route(Object message) {
            MessageName name = MessageName.class.cast(checkNotNull(message));
            switch (name) {
                case EXECUTE_AGENT:
                    return agentExecutionPool;
                default:
                    return defaultExecutor;
            }
        }
    }

    static class AgentExecutorPool extends UntypedLoadBalancer {
        private final InfiniteIterator<ActorRef> workers;

        public AgentExecutorPool(int nWorker) {
            ImmutableList.Builder<ActorRef> actorRefBuilder = ImmutableList.builder();
            for (int i = 0; i < nWorker; ++i) {
                actorRefBuilder.add((ActorRef) actorOf(new Creator<akka.actor.Actor>() {
                    @Override
                    public UntypedActor create() {
                        return new AgentExecutor();
                    }
                }).start());
            }
            this.workers = new CyclicIterator<ActorRef>(actorRefBuilder.build());
        }

        @Override
        public InfiniteIterator<ActorRef> seq() {
            return workers;
        }
    }

    private static class AgentExecutor extends UntypedActor {
        @Override
        public void onReceive(Object message) throws Exception {
            MessageName name = MessageName.class.cast(message);
            switch (name) {
                case EXECUTE_AGENT:
                    ExecuteAgentMessage.class.cast(message).agent.execute();
                    break;
                default:
                    throw new IllegalArgumentException("AgentExecutor doesn't handle messages of type " + name);
            }


        }
    }

    private static class DefaultExecutor extends UntypedActor {
        private final ActorSimulation simulation;

        public DefaultExecutor(ActorSimulation simulation) {
            this.simulation = simulation;
        }

        @Override
        public void onReceive(Object message) throws Exception {
            MessageName name = MessageName.class.cast(message);
            switch (name) {
                case DELIVER_MESSAGE:
                    ACLMessage<Agent> agentMessage = DeliverAgentMessage.class.cast(message).message;
                    for (Agent agent : agentMessage.getRecipients()) {
                        agent.receive(new AgentMessage(agentMessage, simulation.getSteps()));
                    }
                    break;
                case CREATE_AGENT:
                    CreateAgentMessage createAgentMessage = CreateAgentMessage.class.cast(message);
                    simulation.insertAgent(createAgentMessage.population, ImmutableObject2D.of(createAgentMessage.coordinates, 0), createAgentMessage.genome);
                    break;
                case REMOVE_AGENT:
                    RemoveAgentMessage removeAgentMessage = RemoveAgentMessage.class.cast(message);
                    simulation.removeAgent(removeAgentMessage.agent);
                    break;
                default:
                    throw new IllegalArgumentException("DefaultExecutor doesn't handle messages of type " + name);
            }
        }
    }

    private static enum MessageName {
        EXECUTE_AGENT,
        CREATE_AGENT, REMOVE_AGENT, DELIVER_MESSAGE
    }

    private static interface Message {
        MessageName name();
    }

    private static class ExecuteAgentMessage implements Message {

        private final Agent agent;

        public ExecuteAgentMessage(Agent agent) {
            this.agent = agent;
        }

        @Override
        public MessageName name() {
            return MessageName.EXECUTE_AGENT;
        }
    }

    private static class DeliverAgentMessage implements Message {

        private final ACLMessage<Agent> message;

        public DeliverAgentMessage(ACLMessage<Agent> message) {
            this.message = message;
        }

        @Override
        public MessageName name() {
            return MessageName.DELIVER_MESSAGE;
        }
    }

    private class CreateAgentMessage implements Message {
        private final Population population;
        private final Coordinates2D coordinates;
        private final Genome genome;

        public CreateAgentMessage(Population population, Coordinates2D coordinates, Genome genome) {
            this.population = population;
            this.coordinates = coordinates;
            this.genome = genome;
        }

        @Override
        public MessageName name() {
            return MessageName.CREATE_AGENT;
        }
    }

    private class RemoveAgentMessage implements Message {
        private final Agent agent;

        public RemoveAgentMessage(Agent agent) {
            this.agent = agent;
        }

        @Override
        public MessageName name() {
            return MessageName.REMOVE_AGENT;
        }
    }
}
