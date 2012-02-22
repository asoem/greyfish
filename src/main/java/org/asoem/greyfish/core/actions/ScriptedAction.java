package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 18:24
 */
@ClassGroup(tags = "actions")
public class ScriptedAction extends AbstractGFAction {

    private GreyfishExpression script;

    @SimpleXMLConstructor
    public ScriptedAction() {
        super(new Builder());
    }

    protected ScriptedAction(ScriptedAction scriptedAction, DeepCloner cloner) {
        super(scriptedAction, cloner);
        this.script = scriptedAction.script;
    }

    protected ScriptedAction(AbstractBuilder<? extends ScriptedAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.script = builder.script;
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        assert script != null;
        script.evaluateForContext(this);
        return ActionState.END_SUCCESS;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ScriptedAction(this, cloner);
    }

    public static class Builder extends AbstractBuilder<ScriptedAction, Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected ScriptedAction checkedBuild() {
            return new ScriptedAction(this);
        }
    }
    
    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends ScriptedAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {

        public GreyfishExpression script = GreyfishExpressionFactoryHolder.compile("");
        
        public T executes(GreyfishExpression greyfishExpression) { this.script = checkNotNull(greyfishExpression); return self(); }
    }
}
