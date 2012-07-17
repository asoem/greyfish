package org.asoem.greyfish.core.conditions;

import com.google.inject.Inject;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

/**
 * User: christoph
 * Date: 04.05.12
 * Time: 11:47
 */
public class GenericCondition extends LeafCondition {

    private final Callback<? super GenericCondition, Boolean> callback;

    @Inject
    private GenericCondition() {
        callback = Callbacks.constant(true);
    }

    public GenericCondition(Callback<? super GenericCondition, Boolean> callback) {
        this.callback = callback;
    }

    protected GenericCondition(GenericCondition genericCondition, DeepCloner cloner) {
        super(genericCondition, cloner);
        this.callback = genericCondition.callback;
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        return Callbacks.call(callback, this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new GenericCondition(this, cloner);
    }

    public static GenericCondition evaluate(Callback<? super GenericCondition, Boolean> callback) {
        return new GenericCondition(callback);
    }
}
