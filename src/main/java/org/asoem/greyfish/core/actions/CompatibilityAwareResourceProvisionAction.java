package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genes;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.acl.NotUnderstoodException.unexpectedPayloadType;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 15:12
 */
public class CompatibilityAwareResourceProvisionAction extends ResourceProvisionAction {


    private static final Logger LOGGER = LoggerFactory.getLogger(CompatibilityAwareResourceProvisionAction.class);

    protected GFProperty similarityTrait;

    public CompatibilityAwareResourceProvisionAction(CompatibilityAwareResourceProvisionAction action, DeepCloner map) {
        super(action, map);
        similarityTrait = map.cloneField(action.similarityTrait, GFProperty.class);
    }

    public CompatibilityAwareResourceProvisionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.similarityTrait = builder.similarityTrait;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message) throws NotUnderstoodException {
        ImmutableACLMessage.Builder<Agent> builder = ImmutableACLMessage.createReply(message, this.getAgent());

        final CompatibilityAwareResourceConversation.CFPContent cfpContent;
        try {
            cfpContent = message.getContent(CompatibilityAwareResourceConversation.CFPContent.class);
        } catch (IllegalArgumentException e) {
            throw unexpectedPayloadType(message, CompatibilityAwareResourceConversation.CFPContent.class);
        }
        if (cfpContent == null)
            throw new NotUnderstoodException("Content must not be null");

        double compatibility = 0;
        if (similarityTrait != null) {
            final Iterable<Gene<?>> thisGenes = similarityTrait.getGenes();
            final Iterable<Gene<?>> thatGenes = cfpContent.getSimilatityTrait().getGenes();
            compatibility = 1 - Genes.normalizedDistance(thisGenes, thatGenes);
            LOGGER.debug("{}: Compatibility value of Resource and Consumer is {}", this, compatibility);
        }
        else {
            LOGGER.warn("{}: similarityTrait not defined", this);
        }
        double offer = cfpContent.getAmount() * compatibility;

        if (offer > 0) {
            builder.performative(ACLPerformative.PROPOSE);
            builder.content(new CompatibilityAwareResourceConversation.ProposeContent(offer), CompatibilityAwareResourceConversation.ProposeContent.class);
        }
        else {
            builder.performative(ACLPerformative.REFUSE);
        }

        return builder;
    }

    @Override
    public CompatibilityAwareResourceProvisionAction deepClone(DeepCloner cloner) {
        return new CompatibilityAwareResourceProvisionAction(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);

        e.add("Similarity Trait", new SetAdaptor<GFProperty>(GFProperty.class) {
            @Override
            protected void set(GFProperty arg0) {
                similarityTrait = checkNotNull(arg0);
            }

            @Override
            public GFProperty get() {
                return similarityTrait;
            }

            @Override
            public Iterable<GFProperty> values() {
                return agent().getProperties();
            }
        });
    }

    public static class Builder extends AbstractBuilder<CompatibilityAwareResourceProvisionAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public CompatibilityAwareResourceProvisionAction checkedBuild() { return new CompatibilityAwareResourceProvisionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends CompatibilityAwareResourceProvisionAction, T extends AbstractBuilder<E, T>> extends ResourceProvisionAction.AbstractBuilder<E, T> {
        private GFProperty similarityTrait;

        public T similarityTrait(GFProperty trait) { this.similarityTrait = checkNotNull(trait); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkNotNull(similarityTrait, "similarityTrait must not be null");
            super.checkBuilder();
        }
    }
}