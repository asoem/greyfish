package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 26.04.12
 * Time: 15:38
 */
public class UniparentalChromosomalOrigin implements ChromosomalOrigin {
    @Override
    public int parentCount() {
        return 1;
    }
}
