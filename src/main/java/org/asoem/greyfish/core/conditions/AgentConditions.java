package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

/**
 * User: christoph
 * Date: 09.10.11
 * Time: 13:53
 */
public class AgentConditions {
    public static GFCondition forPredicate(final Predicate<Simulation> objectPredicate) {
        return new LeafCondition() {

            @Override
            public boolean evaluate(Simulation simulation) {
                synchronized (this) {
                    return objectPredicate.apply(simulation);
                }
            }

            @Override
            public DeepCloneable deepClone(DeepCloner cloner) {
                cloner.addClone(this);
                return this;
            }
        };
    }
}
