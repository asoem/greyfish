package org.asoem.greyfish.core.actions;

/**
 * User: christoph
 * Date: 13.02.12
 * Time: 14:23
 */
public class MatingEvent {
    private final int agentId;
    private final double quality;

    public MatingEvent(int agentId, double quality) {
        this.quality = quality;
        this.agentId = agentId;
    }

    public int getAgentId() {
        return agentId;
    }

    public double getQuality() {
        return quality;
    }
}
