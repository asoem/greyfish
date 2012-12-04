/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
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
public abstract class BranchCondition<A extends Agent<A, ?>> extends AbstractCondition<A> {

    private final List<ActionCondition<A>> conditions = Lists.newArrayList();

    protected BranchCondition(BranchCondition<A> cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        for (ActionCondition<A> condition : cloneable.getChildConditions())
            add(cloner.getClone(condition, ActionCondition.class));
    }

    protected BranchCondition(AbstractBuilder<A, ?, ?> builder) {
        addAll(builder.conditions);
    }

    @Override
    public List<ActionCondition<A>> getChildConditions() {
        return conditions;
    }

    @Override
    public boolean isLeafCondition() {
        return false;
    }

    @Override
    public void initialize() {
        super.initialize();
        for (ActionCondition<A> condition : getChildConditions())
            condition.initialize();
    }

    public void addAll(Iterable<? extends ActionCondition<A>> childConditions) {
        checkState(!isFrozen());
        for (ActionCondition<A> childCondition : childConditions) {
            add(childCondition);
        }
    }

    public int indexOf(ActionCondition<A> currentCondition) {
        return getChildConditions().indexOf(currentCondition);
    }

    @Override
    public void add(ActionCondition<A> newChild) {
        checkState(!isFrozen());
        insert(newChild, getChildConditions().size());
    }

    @Override
    public void insert(ActionCondition<A> condition, int index) {
        checkState(!isFrozen());
        checkNotNull(condition);
        getChildConditions().add(index, condition);
        condition.setParent(this);
    }

    public ActionCondition remove(int index) {
        checkState(!isFrozen());
        checkPositionIndex(index, getChildConditions().size());
        ActionCondition<A> ret = getChildConditions().remove(index);
        ret.setParent(null);
        return ret;
    }

    @Override
    public void remove(ActionCondition<A> condition) {
        checkState(!isFrozen());
        remove(getChildConditions().indexOf(condition));
    }

    @Override
    public void removeAll() {
        checkState(!isFrozen());
        for (ActionCondition<A> condition : getChildConditions()) {
            remove(condition);
        }
    }

    @Override
    public void setAction(AgentAction<A> action) {
        super.setAction(action);
        for (ActionCondition<A> condition : getChildConditions()) {
            condition.setAction(action);
        }
    }

    @Override
    public void setParent(@Nullable ActionCondition<A> parent) {
        super.setParent(parent);
        for (ActionCondition<A> condition : getChildConditions()) {
            condition.setParent(this);
        }
    }

    @Override
    public final Iterable<AgentNode> children() {
        return Collections.<AgentNode>unmodifiableList(getChildConditions());
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, E extends BranchCondition<A>, T extends AbstractBuilder<A, E, T>> extends AbstractCondition.AbstractBuilder<A,E,T> implements Serializable {
        private final List<ActionCondition> conditions = Lists.newArrayList();

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(BranchCondition<A> branchCondition) {
            addAll(branchCondition.conditions);
        }

        protected T add(ActionCondition<A> condition) { condition.add(checkNotNull(condition)); return self(); }
        protected T add(ActionCondition<A>... conditions) { this.conditions.addAll(asList(checkNotNull(conditions))); return self(); }
        protected T addAll(Iterable<? extends ActionCondition<A>> conditions) { Iterables.addAll(this.conditions, checkNotNull(conditions)); return self(); }
    }
}
