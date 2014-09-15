package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.ActionExecutionResult;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.AgentContext;

import javax.annotation.Nullable;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The default implementation of an {@code ActionExecutionStrategy}.
 */
public final class DefaultActionScheduler<A extends Agent<?>, C extends AgentContext<A>>
        implements ActionScheduler<C>, Serializable {

    private final List<? extends AgentAction<? super C>> actions;

    private ExecutionLog<? super C> executionLog;

    public DefaultActionScheduler(final List<? extends AgentAction<? super C>> actions) {
        this.actions = checkNotNull(actions);
        this.executionLog = emptyLog();
    }

    @SuppressWarnings("unchecked")
    private ExecutionLog<Object> emptyLog() {
        return (ExecutionLog<Object>) EmptyExecutionLog.INSTANCE;
    }

    private DefaultActionScheduler(final List<? extends AgentAction<? super C>> agentActions, final ExecutionLog<? super C> executionLog) {
        this.actions = agentActions;
        this.executionLog = executionLog;
    }

    @Override
    public boolean executeNext(final C context) {
        checkNotNull(context);

        // identify action to execute
        if (executionLog.hasUncompletedAction()) {
            final AgentAction<? super C> nextAction = executionLog.getAction();
            assert nextAction != null;
            final ActionExecutionResult state = nextAction.apply(context);
            executionLog = new BasicExecutionLog<>(nextAction, state);
            return true;
        } else {
            for (final AgentAction<? super C> action : actions) {
                final ActionExecutionResult state = action.apply(context);
                if (state != ActionExecutionResult.NEXT) {
                    executionLog = new BasicExecutionLog<>(action, state);
                    return true;
                }
            }

            executionLog = emptyLog();
            return false;
        }
    }

    @Override
    public void reset() {
        executionLog = emptyLog();
    }

    @Override
    public String toString() {
        return "DefaultActionExecutionStrategy{" +
                "actions=" + actions +
                ", executionLog=" + executionLog +
                '}';
    }

    private Object writeReplace() {
        return new SerializedForm<>(this);
    }

    private static class SerializedForm<A extends Agent<?>, C extends AgentContext<A>> implements Serializable {
        private final List<? extends AgentAction<? super C>> actions;
        private final ExecutionLog<? super C> executionLog;

        SerializedForm(final DefaultActionScheduler<A, C> strategy) {
            this.actions = strategy.actions;
            this.executionLog = strategy.executionLog;
        }

        private Object readResolve() throws ObjectStreamException {
            if (executionLog instanceof BasicExecutionLog) {
                checkNotNull(executionLog.getState());
                checkState(actions.contains(executionLog.getAction()));
            }
            return new DefaultActionScheduler<A, C>(actions, executionLog);
        }

        private static final long serialVersionUID = 0;
    }

    private static class BasicExecutionLog<C> implements ExecutionLog<C>, Serializable {
        private final AgentAction<C> action;
        private final ActionExecutionResult state;

        private BasicExecutionLog(final AgentAction<C> action, final ActionExecutionResult state) {
            this.action = action;
            this.state = state;
        }

        @Override
        public boolean hasUncompletedAction() {
            return ActionExecutionResult.CONTINUE.equals(state);
        }

        @Override
        public AgentAction<C> getAction() {
            return action;
        }

        @Override
        public ActionExecutionResult getState() {
            return state;
        }

        @Override
        public String toString() {
            return "BasicExecutionLog{" +
                    "action=" + action +
                    ", state=" + state +
                    '}';
        }

        private static final long serialVersionUID = 0;
    }

    private enum EmptyExecutionLog implements ExecutionLog {
        INSTANCE;

        @Override
        public boolean hasUncompletedAction() {
            return false;
        }

        @Override
        public AgentAction getAction() {
            return null;
        }

        @Override
        public ActionExecutionResult getState() {
            return null;
        }
    }

    private interface ExecutionLog<C> {
        boolean hasUncompletedAction();

        @Nullable
        AgentAction<C> getAction();

        ActionExecutionResult getState();
    }
}
