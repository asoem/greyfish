package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.ActionExecutionResult;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.ComponentContext;

import javax.annotation.Nullable;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The default implementation of an {@code ActionExecutionStrategy}.
 */
public final class DefaultActionExecutionStrategy<T extends Agent<T, ?>>
        implements ActionExecutionStrategy<T>, Serializable {

    private final List<? extends AgentAction<T>> actions;

    private ExecutionLog<T> executionLog;

    public DefaultActionExecutionStrategy(final List<? extends AgentAction<T>> actions) {
        this.actions = checkNotNull(actions);
        this.executionLog = emptyLog();
    }

    @SuppressWarnings("unchecked")
    private ExecutionLog<T> emptyLog() {
        return (ExecutionLog<T>) EmptyExecutionLog.INSTANCE;
    }

    private DefaultActionExecutionStrategy(final List<? extends AgentAction<T>> agentActions, final ExecutionLog<T> executionLog) {
        this.actions = agentActions;
        this.executionLog = executionLog;
    }

    @Override
    public boolean execute(final ComponentContext<T, ?> componentContext) {
        checkNotNull(componentContext);

        // identify action to execute
        if (executionLog.hasUncompletedAction()) {
            final AgentAction<T> nextAction = executionLog.getAction();
            assert nextAction != null;
            final ActionExecutionResult state = nextAction.apply(componentContext);
            executionLog = new BasicExecutionLog<>(nextAction, state);
            return true;
        } else {
            for (final AgentAction<T> action : actions) {
                final ActionExecutionResult state = action.apply(componentContext);
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

    private static class SerializedForm<T extends Agent<T, ?>> implements Serializable {
        private final List<? extends AgentAction<T>> actions;
        private final ExecutionLog<T> executionLog;

        SerializedForm(final DefaultActionExecutionStrategy<T> strategy) {
            this.actions = strategy.actions;
            this.executionLog = strategy.executionLog;
        }

        private Object readResolve() throws ObjectStreamException {
            if (executionLog instanceof BasicExecutionLog) {
                checkNotNull(executionLog.getState());
                checkState(actions.contains(executionLog.getAction()));
            }
            return new DefaultActionExecutionStrategy<>(actions, executionLog);
        }

        private static final long serialVersionUID = 0;
    }

    private static class BasicExecutionLog<T extends Agent<T, ?>> implements ExecutionLog<T>, Serializable {
        private final AgentAction<T> action;
        private final ActionExecutionResult state;

        private BasicExecutionLog(final AgentAction<T> action, final ActionExecutionResult state) {
            this.action = action;
            this.state = state;
        }

        @Override
        public boolean hasUncompletedAction() {
            return ActionExecutionResult.CONTINUE.equals(state);
        }

        @Override
        public AgentAction<T> getAction() {
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
        public AgentAction<?> getAction() {
            return null;
        }

        @Override
        public ActionExecutionResult getState() {
            return null;
        }
    }

    private interface ExecutionLog<T extends Agent<T, ?>> {
        boolean hasUncompletedAction();

        @Nullable
        AgentAction<T> getAction();

        ActionExecutionResult getState();
    }
}
