package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

/**
 * User: christoph
 * Date: 04.05.12
 * Time: 11:47
 */
public class FunctionCondition extends LeafCondition {

    private final Function<? super FunctionCondition, ? extends Boolean> function;

    public FunctionCondition(Function<? super FunctionCondition, ? extends Boolean> function) {
        this.function = function;
    }

    protected FunctionCondition(FunctionCondition functionCondition, DeepCloner cloner) {
        super(functionCondition, cloner);
        this.function = functionCondition.function;
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        return function.apply(this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new FunctionCondition(this, cloner);
    }

    public static FunctionCondition evaluate(Function<? super FunctionCondition, ? extends Boolean> function) {
        return new FunctionCondition(function);
    }
}
