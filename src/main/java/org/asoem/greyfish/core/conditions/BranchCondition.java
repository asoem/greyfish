/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static java.util.Arrays.asList;


/**
 * Implementations of <code>BranchCondition</code> should logically concatenate two or more implementations of <code>Condition</code>.
 * @author christoph
 *
 */
public abstract class BranchCondition extends AbstractCondition {

    private final List<ActionCondition> conditions = Lists.newArrayList();

    protected BranchCondition(BranchCondition cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        for (ActionCondition condition : cloneable.getChildConditions())
            add(cloner.getClone(condition, ActionCondition.class));
    }

    protected BranchCondition(AbstractBuilder<?, ?> builder) {
        addAll(builder.conditions);
    }

    @Override
    public List<ActionCondition> getChildConditions() {
        return conditions;
    }

    @Override
    public boolean isLeafCondition() {
        return false;
    }

    @Override
    public void initialize() {
        super.initialize();
        for (ActionCondition condition : getChildConditions())
            condition.initialize();
    }

    public void addAll(Iterable<? extends ActionCondition> childConditions) {
        checkState(!isFrozen());
        for (ActionCondition childCondition : childConditions) {
            add(childCondition);
        }
    }

    public int indexOf(ActionCondition currentCondition) {
        return getChildConditions().indexOf(currentCondition);
    }

    @Override
    public void add(ActionCondition newChild) {
        checkState(!isFrozen());
        insert(newChild, getChildConditions().size());
    }

    @Override
    public void insert(ActionCondition condition, int index) {
        checkState(!isFrozen());
        checkNotNull(condition);
        getChildConditions().add(index, condition);
        condition.setParent(this);
    }

    public ActionCondition remove(int index) {
        checkState(!isFrozen());
        checkPositionIndex(index, getChildConditions().size());
        ActionCondition ret = getChildConditions().remove(index);
        ret.setParent(null);
        return ret;
    }

    @Override
    public void remove(ActionCondition condition) {
        checkState(!isFrozen());
        remove(getChildConditions().indexOf(condition));
    }

    @Override
    public void removeAll() {
        checkState(!isFrozen());
        for (ActionCondition condition : getChildConditions()) {
            remove(condition);
        }
    }

    @Override
    public void setAction(AgentAction action) {
        super.setAction(action);
        for (ActionCondition condition : getChildConditions()) {
            condition.setAction(action);
        }
    }

    @Override
    public void setParent(@Nullable ActionCondition parent) {
        super.setParent(parent);
        for (ActionCondition condition : getChildConditions()) {
            condition.setParent(this);
        }
    }

    @Override
    public final Iterable<AgentNode> children() {
        return Collections.<AgentNode>unmodifiableList(getChildConditions());
    }

    protected static abstract class AbstractBuilder<E extends BranchCondition, T extends AbstractBuilder<E, T>> extends AbstractCondition.AbstractBuilder<E,T> implements Serializable {
        private final List<ActionCondition> conditions = Lists.newArrayList();

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(BranchCondition branchCondition) {
            addAll(branchCondition.conditions);
        }

        protected T add(ActionCondition condition) { condition.add(checkNotNull(condition)); return self(); }
        protected T add(ActionCondition... conditions) { this.conditions.addAll(asList(checkNotNull(conditions))); return self(); }
        protected T addAll(Iterable<? extends ActionCondition> conditions) { Iterables.addAll(this.conditions, checkNotNull(conditions)); return self(); }
    }
}
