package org.asoem.greyfish.core.conditions;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;

/**
 * A class that implements the <code>Condition</code> interface.
 * Can be used to make a <code>GFAction</code> conditional.
 * @author christoph
 */
public abstract class AbstractCondition extends AbstractAgentComponent implements GFCondition {

    private Optional<GFCondition> parentCondition;

    protected AbstractCondition(AbstractCondition cloneable, DeepCloner map) {
        super(cloneable, map);
        this.parentCondition = Optional.fromNullable(map.continueWith(cloneable.parentCondition.orNull(), GFCondition.class));
    }

    @Override
    @Nullable
    public GFCondition getParentCondition() {
        return parentCondition.orNull();
    }

    @Override
    public void setParent(@Nullable GFCondition parent) {
        this.parentCondition = Optional.fromNullable(parent);
    }

    @Override
    public final boolean isRootCondition() {
        return ! parentCondition.isPresent();
    }

    @Override
    public GFCondition getRoot() {
        return (isRootCondition()) ? this : parentCondition.get().getRoot();
    }

    @Commit
    private void commit() {
        if (!isLeafCondition()) {
            for (GFCondition condition : getChildConditions()) {
                condition.setParent(this);
            }
        }
    }

    @Override
    public void configure(ConfigurationHandler e) {
    }

    protected AbstractCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractAgentComponent.AbstractBuilder<T> {
    }

    @Override
    public String toString() {
        return parentCondition + "<-" + this.getClass().getSimpleName();
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }
}
