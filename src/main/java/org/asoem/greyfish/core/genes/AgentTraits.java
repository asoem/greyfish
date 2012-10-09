package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Tuple2;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 17:56
 */
public final class AgentTraits {

    private AgentTraits() {}

    public static void updateValues(List<? extends AgentTrait> traits, List<?> values) {
        checkNotNull(values);
        checkArgument(values.size() == traits.size(), "");

        for (Product2<? extends AgentTrait, ?> tuple2 : Tuple2.Zipped.of(traits, values)) {
            tuple2._1().setAllele(tuple2._2());
        }
    }
}
