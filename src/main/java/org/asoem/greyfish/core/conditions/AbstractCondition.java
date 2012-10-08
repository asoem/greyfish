package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;

/**
 * A class that implements the <code>Condition</code> interface.
 * Can be used to make a <code>AgentAction</code> conditional.
 * @author christoph
 */
public abstract class AbstractCondition extends AbstractAgentComponent implements ActionCondition {

    @Nullable
    private ActionCondition parentCondition;

    protected AbstractCondition(AbstractCondition cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.parentCondition = cloner.getClone(cloneable.parentCondition, ActionCondition.class);
    }

    protected AbstractCondition() {}

    @Override
    @Nullable
    public ActionCondition getParentCondition() {
        return parentCondition;
    }

    @Override
    public void setParent(@Nullable ActionCondition parent) {
        this.parentCondition = parent;
    }

    @Override
    public final boolean isRootCondition() {
        return parentCondition == null;
    }

    @Override
    public final ActionCondition getRoot() {
        return (parentCondition == null) ? this : parentCondition.getRoot();
    }

    @Commit
    private void commit() {
        if (!isLeafCondition()) {
            for (ActionCondition condition : getChildConditions()) {
                condition.setParent(this);
            }
        }
    }

    @Override
    public void configure(ConfigurationHandler e) {
    }

    protected AbstractCondition(AbstractBuilder<? extends AbstractCondition, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<C extends AbstractCondition, B extends AbstractBuilder<C, B>> extends AbstractAgentComponent.AbstractBuilder<C, B> {
    }

    @Override
    public String toString() {
        return parentCondition + "<-" + this.getClass().getSimpleName();
    }

}
