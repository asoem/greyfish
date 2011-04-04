package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 15:12
 */
public class CompatibilityAwareResourceProvisionAction extends ResourceProvisionAction {


    public CompatibilityAwareResourceProvisionAction(CompatibilityAwareResourceProvisionAction provisionAction, CloneMap map) {
        super(provisionAction, map);
    }

    public CompatibilityAwareResourceProvisionAction(AbstractBuilder<? extends AbstractBuilder> builder) {

    }

    @Override
    public CompatibilityAwareResourceProvisionAction deepCloneHelper(CloneMap cloneMap) {
        return new CompatibilityAwareResourceProvisionAction(this, cloneMap);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
    }

        public static class Builder extends AbstractBuilder<Builder> implements BuilderInterface<CompatibilityAwareResourceProvisionAction> {
        @Override protected Builder self() { return this; }
        @Override public CompatibilityAwareResourceProvisionAction build() { return new CompatibilityAwareResourceProvisionAction(this); }
    }
    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ResourceProvisionAction.AbstractBuilder<T> {
        private GFProperty similarityTrait;

        public T similarityTrait(GFProperty trait) { this.similarityTrait = checkNotNull(trait); return self(); }
    }
}