package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.base.Product2;

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

    public static <T> T segregate(GeneComponent<T> geneComponent, Object allele1, Object allele2) {
        final Class<T> alleleClass = geneComponent.getAlleleClass();
        return geneComponent.segregate(alleleClass.cast(allele1), alleleClass.cast(allele2));
    }
}
