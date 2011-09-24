package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genes;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
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
        similarityTrait = map.continueWith(action.similarityTrait, GFProperty.class);
    }

    public CompatibilityAwareResourceProvisionAction(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.similarityTrait = builder.similarityTrait;
    }

    @Override
    protected ACLMessage.Builder handleCFP(ACLMessage message) throws NotUnderstoodException {
        ACLMessage.Builder builder = message.createReplyFrom(this.getAgent().getId());

        final CompatibilityAwareResourceConversation.CFPContent cfpContent;
        try {
            cfpContent = message.getReferenceContent(CompatibilityAwareResourceConversation.CFPContent.class);
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
            builder.objectContent(new CompatibilityAwareResourceConversation.ProposeContent(offer));
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

        e.add(new FiniteSetValueAdaptor<GFProperty>("Similarity Trait", GFProperty.class) {
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
                return Iterables.filter(getAllComponents(), GFProperty.class);
            }
        });
    }

    public static class Builder extends AbstractBuilder<Builder> implements BuilderInterface<CompatibilityAwareResourceProvisionAction> {
        @Override protected Builder self() { return this; }
        @Override public CompatibilityAwareResourceProvisionAction build() { return new CompatibilityAwareResourceProvisionAction(checkedSelf()); }
    }
    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ResourceProvisionAction.AbstractBuilder<T> {
        private GFProperty similarityTrait;

        public T similarityTrait(GFProperty trait) { this.similarityTrait = checkNotNull(trait); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkNotNull(similarityTrait, "similarityTrait must not be null");
            super.checkBuilder();
        }
    }
}