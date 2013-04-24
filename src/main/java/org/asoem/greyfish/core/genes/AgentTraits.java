package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Products;

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

    public static void updateValues(List<? extends AgentTrait<?,?>> traits, List<?> values) {
        checkNotNull(values);
        checkArgument(values.size() == traits.size(), "");

        for (Product2<? extends AgentTrait<?,?>, ?> tuple2 : Products.zip(traits, values)) {
            tuple2._1().trySet(tuple2._2());
        }
    }

    public static <T> T mutate(AgentTrait<?, T> component) {
        return component.mutate(component.get());
    }

    public static <T> T mutate(AgentTrait<?, T> component, Object t) {
        checkArgument(component.getValueClass().isInstance(t));

        return component.mutate((T) t);
    }

    public static <T> T segregate(AgentTrait<?, T> agentTrait, Object allele1, Object allele2) {
        checkNotNull(agentTrait);
        final Class<? super T> alleleClass = agentTrait.getValueClass();

        checkArgument(alleleClass.isInstance(allele1), "allele1 is not an instance of %s: %s", alleleClass, allele1);
        checkArgument(alleleClass.isInstance(allele2), "allele2 is not an instance of %s: %s", alleleClass, allele2);

        return agentTrait.segregate((T) allele1, (T) allele2);
    }
}
