package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 26.04.12
 * Time: 15:39
 */
public class BiparentalChromosomalOrigin implements ChromosomalOrigin {
    @Override
    public int parentCount() {
        return 2;
    }
}
