package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 14:18
 */
public class CompatibilityAwareResourceConsumptionAction extends ResourceConsumptionAction {

    protected GFProperty similarityTrait;

    protected CompatibilityAwareResourceConsumptionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.similarityTrait = builder.similarityTrait;
    }

    protected CompatibilityAwareResourceConsumptionAction(CompatibilityAwareResourceConsumptionAction action, DeepCloner map) {
        super(action, map);
        similarityTrait = map.cloneField(action.similarityTrait, GFProperty.class);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);

        e.add(new SetAdaptor<GFProperty>("Similarity Trait", GFProperty.class) {
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

    @Override
    public ResourceConsumptionAction deepClone(DeepCloner cloner) {
        return new CompatibilityAwareResourceConsumptionAction(this, cloner);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> createCFP() {
        ImmutableACLMessage.Builder<Agent> builder = super.createCFP();
        builder.content(new CompatibilityAwareResourceConversation.CFPContent(amountPerRequest, similarityTrait), CompatibilityAwareResourceConversation.CFPContent.class);
        return builder;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message) throws NotUnderstoodException {

        CompatibilityAwareResourceConversation.ProposeContent proposeContent =
                message.getContent(CompatibilityAwareResourceConversation.ProposeContent.class);

        return ImmutableACLMessage.<Agent>createReply(message, agent())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .content(proposeContent.getAmount(), Double.class);
    }

    public static class Builder extends AbstractBuilder<CompatibilityAwareResourceConsumptionAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public CompatibilityAwareResourceConsumptionAction checkedBuild() { return new CompatibilityAwareResourceConsumptionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends CompatibilityAwareResourceConsumptionAction, T extends AbstractBuilder<E,T>> extends ResourceConsumptionAction.AbstractBuilder<E,T> {
        protected GFProperty similarityTrait;

        public T similarityTrait(GFProperty trait) { this.similarityTrait = checkNotNull(trait); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(this.similarityTrait != null, "similarityTrait must not be null");
            super.checkBuilder();
        }
    }
}
