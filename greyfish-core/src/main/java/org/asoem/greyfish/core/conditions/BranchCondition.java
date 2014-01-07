/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static java.util.Arrays.asList;


/**
 * Implementations of <code>BranchCondition</code> should logically concatenate two or more implementations of
 * <code>Condition</code>.
 *
 * @author christoph
 */
public abstract class BranchCondition<A extends Agent<?>> extends AbstractCondition<A> {

    private final List<ActionCondition<A>> conditions = Lists.newArrayList();

    protected BranchCondition(final AbstractBuilder<A, ?, ?> builder) {
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
        for (final ActionCondition<A> condition : getChildConditions())
            condition.initialize();
    }

    public void addAll(final Iterable<? extends ActionCondition<A>> childConditions) {
        for (final ActionCondition<A> childCondition : childConditions) {
            add(childCondition);
        }
    }

    public int indexOf(final ActionCondition<A> currentCondition) {
        return getChildConditions().indexOf(currentCondition);
    }

    @Override
    public void add(final ActionCondition<A> newChild) {
        insert(newChild, getChildConditions().size());
    }

    @Override
    public void insert(final ActionCondition<A> condition, final int index) {
        checkNotNull(condition);
        getChildConditions().add(index, condition);
        condition.setParent(this);
    }

    public ActionCondition<A> remove(final int index) {
        checkPositionIndex(index, getChildConditions().size());
        final ActionCondition<A> ret = getChildConditions().remove(index);
        ret.setParent(null);
        return ret;
    }

    @Override
    public void remove(final ActionCondition<A> condition) {
        remove(getChildConditions().indexOf(condition));
    }

    @Override
    public void removeAll() {
        for (final ActionCondition<A> condition : getChildConditions()) {
            remove(condition);
        }
    }

    @Override
    public void setAction(final AgentAction<?> action) {
        super.setAction(action);
        for (final ActionCondition<A> condition : getChildConditions()) {
            condition.setAction(action);
        }
    }

    @Override
    public void setParent(@Nullable final ActionCondition<A> parent) {
        super.setParent(parent);
        for (final ActionCondition<A> condition : getChildConditions()) {
            condition.setParent(this);
        }
    }

    @Override
    public final Iterable<AgentNode> children() {
        return Collections.<AgentNode>unmodifiableList(getChildConditions());
    }

    @Override
    public String toString() {
        return (isRootCondition() ? "*" : "") + Objects.toStringHelper(this).addValue(getChildConditions()).toString();
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, E extends BranchCondition<A>, T extends AbstractBuilder<A, E, T>> extends AbstractCondition.AbstractBuilder<A, E, T> implements Serializable {
        private final List<ActionCondition<A>> conditions = Lists.newArrayList();

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final BranchCondition<A> branchCondition) {
            addAll(branchCondition.conditions);
        }

        protected T add(final ActionCondition<A> condition) {
            this.conditions.add(checkNotNull(condition));
            return self();
        }

        protected T add(final ActionCondition<A>... conditions) {
            this.conditions.addAll(asList(checkNotNull(conditions)));
            return self();
        }

        protected T addAll(final Iterable<? extends ActionCondition<A>> conditions) {
            Iterables.addAll(this.conditions, checkNotNull(conditions));
            return self();
        }
    }
}
