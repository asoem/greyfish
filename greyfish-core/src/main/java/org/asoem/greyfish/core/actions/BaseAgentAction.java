/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.actions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.conditions.ActionCondition;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.*;

/**
 * An abstract base implementation of a agent action which delegates the precondition check to an {@link
 * org.asoem.greyfish.core.conditions.ActionCondition ActionCondition}.
 *
 * @param <A> the type of the agent
 */
public abstract class BaseAgentAction<A extends Agent<?>, C extends AgentContext<A>>
        implements AgentAction<C> {

    @Nullable
    private String name;
    @Nullable
    private ActionCondition<A> condition;
    private ActionState actionState;
    private Optional<A> agent;

    @VisibleForTesting
    BaseAgentAction(final ActionCondition<A> condition) {
        this.condition = condition;
        this.actionState = INITIAL;
    }

    protected BaseAgentAction(final AbstractBuilder<A, ? extends BaseAgentAction<A, C>,
            ? extends AbstractBuilder<A, ?, ?, C>, C> builder) {
        this.setActionState(builder.actionState);
        this.name = builder.name;
        setCondition(builder.condition);
    }

    public final boolean evaluateCondition() {
        return condition == null || condition.evaluate();
    }

    /**
     * Called by the {@code Agent} which contains this {@code AgentAction}
     *
     * @param agentContext
     */
    @Override
    public final ActionExecutionResult apply(final C agentContext) {
        checkNotNull(agentContext);
        this.agent = Optional.of(agentContext.agent());

        ActionExecutionResult result = null;

        while (result == null) {
            final ActionState currentState = getActionState();
            switch (currentState) {
                case INITIAL:
                    final boolean preconditionsMet = evaluateCondition();
                    if (preconditionsMet) {
                        setState(PRECONDITIONS_MET);
                    } else {
                        setState(PRECONDITIONS_FAILED);
                    }
                    break;
                case PRECONDITIONS_MET:
                case INTERMEDIATE:
                    final ActionState state = proceed(agentContext);
                    setState(state);
                    if (state == INTERMEDIATE) {
                        result = ActionExecutionResult.CONTINUE;
                    }
                    break;
                case PRECONDITIONS_FAILED:
                case ABORTED:
                    result = ActionExecutionResult.NEXT;
                    reset();
                    break;
                case COMPLETED:
                    result = ActionExecutionResult.BREAK;
                    reset();
                    break;
                default:
                    throw new AssertionError("Unexpected state: " + currentState);
            }
        }

        this.agent = Optional.absent();

        return result;
    }

    protected abstract ActionState proceed(final C context);

    private void setState(final ActionState state) {
        assert state != null;
        setActionState(state);
    }

    private void reset() {
        setState(INITIAL);
    }

    @Override
    public void initialize() {
        reset();
        if (condition != null) {
            condition.initialize();
        }
    }

    @Nullable
    public final ActionCondition<A> getCondition() {
        return condition;
    }

    private void setCondition(@Nullable final ActionCondition<A> condition) {
        this.condition = condition;
        if (condition != null) {
            condition.setAction(this);
        }
    }

    @Override
    public final Iterable<AgentNode> children() {
        return condition != null ? Collections.<AgentNode>singletonList(getCondition()) : Collections.<AgentNode>emptyList();
    }

    private ActionState getActionState() {
        return actionState;
    }

    private void setActionState(final ActionState actionState) {
        this.actionState = actionState;
    }

    /**
     * @return this components optional {@code Agent}
     */
    public final Optional<A> agent() {
        return agent;
    }

    public <T> T ask(final C context, final Object message, final Class<T> replyType) {
        throw new IllegalArgumentException();
    }

    @Override
    public final String getName() {
        return name;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected abstract static class AbstractBuilder<
            A extends Agent<?>,
            T extends BaseAgentAction<A, C>,
            B extends AbstractBuilder<A, T, B, C>,
            C extends AgentContext<A>>
            extends org.asoem.greyfish.utils.base.InheritableBuilder<T, B>
            implements Serializable {
        protected String name;
        private ActionCondition<A> condition;
        private int successCount;
        private ActionState actionState = INITIAL;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final BaseAgentAction<A, C> action) {
            this.condition = action.condition;
            this.actionState = action.getActionState();
            this.name = action.name;
        }

        public final B executedIf(final ActionCondition<A> condition) {
            this.condition = condition;
            return self();
        }

        public final B name(final String name) {
            this.name = name;
            return self();
        }
    }
}
