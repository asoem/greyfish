/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static java.util.Arrays.asList;


/**
 * Implementations of <code>BranchCondition</code> should logically concatenate two or more implementations of <code>Condition</code>.
 * @author christoph
 *
 */
public abstract class BranchCondition extends AbstractCondition implements Iterable<ActionCondition> {

    private final List<ActionCondition> conditions;

    protected BranchCondition(BranchCondition cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.conditions = Lists.newArrayList();
        for (ActionCondition condition : cloneable.getChildConditions())
            add(cloner.getClone(condition, ActionCondition.class));
    }

    protected BranchCondition(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.conditions = Lists.newArrayList();
        addAll(builder.conditions);
    }

    protected BranchCondition() {
        this.conditions = Lists.newArrayList();
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
        for (ActionCondition condition : conditions)
            condition.initialize();
    }

    public void addAll(Iterable<? extends ActionCondition> childConditions) {
        checkState(!isFrozen());
        for (ActionCondition childCondition : childConditions) {
            add(childCondition);
        }
    }

    public int indexOf(ActionCondition currentCondition) {
        return conditions.indexOf(currentCondition);
    }

    @Override
    public void add(ActionCondition newChild) {
        checkState(!isFrozen());
        insert(newChild, conditions.size());
    }

    @Override
    public void insert(ActionCondition condition, int index) {
        checkState(!isFrozen());
        checkNotNull(condition);
        conditions.add(index, condition);
        condition.setParent(this);
    }

    public ActionCondition remove(int index) {
        checkState(!isFrozen());
        checkPositionIndex(index, conditions.size());
        ActionCondition ret = conditions.remove(index);
        ret.setParent(null);
        return ret;
    }

    @Override
    public void remove(ActionCondition condition) {
        checkState(!isFrozen());
        remove(conditions.indexOf(condition));
    }

    @Override
    public void removeAll() {
        checkState(!isFrozen());
        for (ActionCondition condition : conditions) {
            remove(condition);
        }
    }

    @Override
    public void setAction(AgentAction action) {
        super.setAction(action);
        for (ActionCondition condition : conditions) {
            condition.setAction(action);
        }
    }

    @Override
    public void setParent(@Nullable ActionCondition parent) {
        super.setParent(parent);
        for (ActionCondition condition : conditions) {
            condition.setParent(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends BranchCondition, T extends AbstractBuilder<E, T>> extends AbstractCondition.AbstractBuilder<E,T> {
        private final List<ActionCondition> conditions = Lists.newArrayList();

        protected T add(ActionCondition condition) { condition.add(checkNotNull(condition)); return self(); }
        protected T add(ActionCondition... conditions) { this.conditions.addAll(asList(checkNotNull(conditions))); return self(); }
        protected T addAll(Iterable<? extends ActionCondition> conditions) { Iterables.addAll(this.conditions, checkNotNull(conditions)); return self(); }
    }

    @Override
    public final Iterable<AgentComponent> children() {
        return Collections.<AgentComponent>unmodifiableList(conditions);
    }

    @Override
    public final Iterator<ActionCondition> iterator() {
        return conditions.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BranchCondition that = (BranchCondition) o;

        if (!conditions.equals(that.conditions)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + conditions.hashCode();
        return result;
    }
}
