package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 14:18
 */
public class CompatibilityAwareResourceConsumptionAction extends ResourceConsumptionAction {

    protected GFProperty similarityTrait;

    protected CompatibilityAwareResourceConsumptionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.similarityTrait = builder.similarityTrait;
    }

    protected CompatibilityAwareResourceConsumptionAction(CompatibilityAwareResourceConsumptionAction action, CloneMap map) {
        super(action, map);
        similarityTrait = map.clone(action.similarityTrait, GFProperty.class);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);

        e.add(new FiniteSetValueAdaptor<GFProperty>("Similarity Trait", GFProperty.class) {
            @Override
            protected void set(GFProperty arg0) {
                similarityTrait = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public GFProperty get() {
                return similarityTrait;
            }

            @Override
            public Iterable<GFProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), GFProperty.class);
            }
        });
    }

    @Override
    public ResourceConsumptionAction deepCloneHelper(CloneMap cloneMap) {
        return new CompatibilityAwareResourceConsumptionAction(this, cloneMap);
    }

    @Override
    protected ACLMessage.Builder createCFP() {
        ACLMessage.Builder builder = super.createCFP();
        builder.objectContent(new CompatibilityAwareResourceConversation.CFPContent(amountPerRequest, similarityTrait));
        return builder;
    }

    @Override
    protected ACLMessage.Builder handlePropose(ACLMessage message) throws NotUnderstoodException {

        CompatibilityAwareResourceConversation.ProposeContent proposeContent =
                message.getReferenceContent(CompatibilityAwareResourceConversation.ProposeContent.class);

        return message
                .createReplyFrom(getComponentOwner().getId())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .objectContent(proposeContent.getAmount());
    }

    public static class Builder extends AbstractBuilder<Builder> implements BuilderInterface<CompatibilityAwareResourceConsumptionAction> {
        @Override protected Builder self() { return this; }
        @Override public CompatibilityAwareResourceConsumptionAction build() { return new CompatibilityAwareResourceConsumptionAction(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ResourceConsumptionAction.AbstractBuilder<T> {
        protected GFProperty similarityTrait;

        public T similarityTrait(GFProperty trait) { this.similarityTrait = checkNotNull(trait); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(this.similarityTrait != null, "similarityTrait must not be null");
            super.checkBuilder();
        }
    }
}
