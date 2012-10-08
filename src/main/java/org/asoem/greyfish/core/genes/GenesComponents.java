package org.asoem.greyfish.core.genes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.02.11
 * Time: 11:43
 */
public final class GenesComponents {

    private GenesComponents() {}

    public static <T> T mutate(AgentTrait<T> component) {
        return component.mutate(component.getAllele());
    }

    public static <T> T mutate(AgentTrait<T> component, Object t) {
        return component.mutate(component.getAlleleClass().cast(t));
    }

    @SuppressWarnings("unchecked") // is checked
    public static <T> T segregate(AgentTrait<T> agentTrait, Object allele1, Object allele2) {
        checkNotNull(agentTrait);
        final Class<T> alleleClass = agentTrait.getAlleleClass();

        checkArgument(alleleClass.isInstance(allele1), "allele1 is not an instance of %s: %s", alleleClass, allele1);
        checkArgument(alleleClass.isInstance(allele2), "allele2 is not an instance of %s: %s", alleleClass, allele2);

        return agentTrait.segregate((T) allele1, (T) allele2);
    }
}
