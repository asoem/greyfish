package org.asoem.greyfish.core.genes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.02.11
 * Time: 11:43
 */
public class GenesComponents {

    public static <T> T mutate(GeneComponent<T> component) {
        return component.mutate(component.getAllele());
    }

    public static <T> T mutate(GeneComponent<T> component, Object t) {
        return component.mutate(component.getAlleleClass().cast(t));
    }

    @SuppressWarnings("unchecked") // is checked
    public static <T> T segregate(GeneComponent<T> geneComponent, Object allele1, Object allele2) {
        checkNotNull(geneComponent);
        final Class<T> alleleClass = geneComponent.getAlleleClass();

        checkArgument(alleleClass.isInstance(allele1), "allele1 is not an instance of %s: %s", alleleClass, allele1);
        checkArgument(alleleClass.isInstance(allele2), "allele2 is not an instance of %s: %s", alleleClass, allele2);

        return geneComponent.segregate((T) allele1, (T) allele2);
    }
}
