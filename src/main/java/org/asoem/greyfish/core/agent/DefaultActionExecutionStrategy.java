package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.utils.ActionState;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.PRECONDITIONS_MET;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 10:46
 */
public class DefaultActionExecutionStrategy implements ActionExecutionStrategy {

    private final List<? extends AgentAction> actions;

    private ExecutionLog executionLog;

    public DefaultActionExecutionStrategy(List<? extends AgentAction> actions) {
        this.actions = checkNotNull(actions);
        this.executionLog = EMPTY_LOG;
    }

    @Override
    public void execute() {

        @Nullable
        AgentAction nextAction = null;

        // identify action to execute
        if (executionLog.hasUncompletedAction()) {
            nextAction = executionLog.getAction();
        } else {
            for (AgentAction action : actions) {
                if (action.checkPreconditions() == PRECONDITIONS_MET) {
                    nextAction = action;
                    break;
                } else
                    action.reset();
            }
        }

        // execute action
        if (nextAction != null) {
            final ActionState state = nextAction.apply();
            executionLog = new BasicExecutionLog(nextAction, state);

            if (state != ActionState.INTERMEDIATE)
                nextAction.reset();
        }
        else
            executionLog = EMPTY_LOG;
    }

    @Override
    @Nullable
    public AgentAction lastExecutedAction() {
        return executionLog.getAction();
    }

    @Override
    @Nullable
    public ActionState lastExecutedActionState() {
        return executionLog.getState();
    }

    private static class BasicExecutionLog implements ExecutionLog {
        private final AgentAction action;
        private final ActionState state;

        private BasicExecutionLog(AgentAction action, ActionState state) {
            this.action = action;
            this.state = state;
        }

        @Override
        public boolean hasUncompletedAction() {
            return ActionState.INTERMEDIATE.equals(action.getState());
        }

        @Override
        public AgentAction getAction() {
            return action;
        }

        @Override
        public ActionState getState() {
            return state;
        }
    }

    private static final ExecutionLog EMPTY_LOG = new ExecutionLog() {
        @Override
        public boolean hasUncompletedAction() {
            return false;
        }

        @Override
        public AgentAction getAction() {
            return null;
        }

        @Override
        public ActionState getState() {
            return null;
        }
    };

    /**
     * User: christoph
     * Date: 09.10.12
     * Time: 12:00
     */
    public static interface ExecutionLog {
        boolean hasUncompletedAction();
        @Nullable
        AgentAction getAction();

        ActionState getState();
    }
}
