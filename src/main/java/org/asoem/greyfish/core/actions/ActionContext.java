package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 06.09.11
 * Time: 12:09
 */
public class ActionContext {

    private final Simulation simulation;
    private final Agent agent;

    public ActionContext(Simulation simulation, Agent agent) {
        this.simulation = checkNotNull(simulation);
        this.agent = checkNotNull(agent);
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Agent getAgent() {
        return agent;
    }

    public void createAgent(Population population, Location2D anchorPoint, Genome genome) {
        simulation.createAgent(population, anchorPoint, genome);
    }

    public void deliverMessage(ACLMessage message) {
        simulation.deliverMessage(message);
    }

    public Iterable<MovingObject2D> findNeighbours(Location2D anchorPoint, double range) {
        return simulation.findObjects(anchorPoint, range);
    }

    public Iterable<MovingObject2D> findNeighbours(double range) {
        return findNeighbours(agent, range);
    }

    public void removeAgent() {
        simulation.removeAgent(agent);
    }

    public int ageOfAgent() {
        return simulation.getSteps() - agent.getTimeOfBirth();

    }

    public List<ACLMessage> receiveMessages(MessageTemplate template) {
        return agent.pollMessages(template);
    }
}
