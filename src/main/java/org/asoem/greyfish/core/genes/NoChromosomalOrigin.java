package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 26.04.12
 * Time: 15:38
 */
public enum NoChromosomalOrigin implements ChromosomalOrigin {
    INSTANCE;

    @Override
    public int parentCount() {
        return 0;
    }
}
