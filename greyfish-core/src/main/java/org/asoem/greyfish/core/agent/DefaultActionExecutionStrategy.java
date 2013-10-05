package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.utils.ActionState;

import javax.annotation.Nullable;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.utils.ActionState.INITIAL;
import static org.asoem.greyfish.core.actions.utils.ActionState.PRECONDITIONS_MET;

/**
 * The default implementation of an {@code ActionExecutionStrategy}.
 */
public class DefaultActionExecutionStrategy implements ActionExecutionStrategy, Serializable {

    private final List<? extends AgentAction<?>> actions;

    private ExecutionLog executionLog;

    public DefaultActionExecutionStrategy(final List<? extends AgentAction<?>> actions) {
        this.actions = checkNotNull(actions);
        this.executionLog = EmptyExecutionLog.INSTANCE;
    }

    private DefaultActionExecutionStrategy(final List<? extends AgentAction<?>> agentActions, final ExecutionLog executionLog) {
        this.actions = agentActions;
        this.executionLog = executionLog;
    }

    @Override
    public final boolean execute() {

        @Nullable
        AgentAction<?> nextAction = null;

        // identify action to execute
        if (executionLog.hasUncompletedAction()) {
            nextAction = executionLog.getAction();
        } else {
            for (final AgentAction<?> action : actions) {
                if (action.getState() != INITIAL)
                    action.reset();

                if (action.checkPreconditions() == PRECONDITIONS_MET) {
                    nextAction = action;
                    break;
                }
            }
        }

        // execute action
        if (nextAction != null) {
            final ActionState state = nextAction.apply();
            executionLog = new BasicExecutionLog(nextAction, state);
            return true;
        } else {
            executionLog = EmptyExecutionLog.INSTANCE;
            return false;
        }
    }

    @Override
    @Nullable
    public AgentAction<?> lastExecutedAction() {
        return executionLog.getAction();
    }

    @Override
    @Nullable
    public ActionState lastExecutedActionState() {
        return executionLog.getState();
    }

    @Override
    public void reset() {
        executionLog = EmptyExecutionLog.INSTANCE;
    }

    @Override
    public String toString() {
        return "DefaultActionExecutionStrategy{" +
                "actions=" + actions +
                ", executionLog=" + executionLog +
                '}';
    }

    private Object writeReplace() {
        return new SerializedForm(this);
    }

    private static class SerializedForm implements Serializable {
        private final List<? extends AgentAction<?>> actions;
        private final ExecutionLog executionLog;

        SerializedForm(final DefaultActionExecutionStrategy strategy) {
            this.actions = strategy.actions;
            this.executionLog = strategy.executionLog;
        }

        private Object readResolve() throws ObjectStreamException {
            if (executionLog instanceof BasicExecutionLog) {
                checkNotNull(executionLog.getState());
                checkState(actions.contains(executionLog.getAction()));
            }
            return new DefaultActionExecutionStrategy(actions, executionLog);
        }

        private static final long serialVersionUID = 0;
    }

    private static class BasicExecutionLog implements ExecutionLog, Serializable {
        private final AgentAction<?> action;
        private final ActionState state;

        private BasicExecutionLog(final AgentAction<?> action, final ActionState state) {
            this.action = action;
            this.state = state;
        }

        @Override
        public boolean hasUncompletedAction() {
            return ActionState.INTERMEDIATE.equals(action.getState());
        }

        @Override
        public AgentAction<?> getAction() {
            return action;
        }

        @Override
        public ActionState getState() {
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
        public ActionState getState() {
            return null;
        }
    }

    private interface ExecutionLog {
        boolean hasUncompletedAction();
        @Nullable
        AgentAction<?> getAction();

        ActionState getState();
    }
}
