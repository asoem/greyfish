package org.asoem.greyfish.utils.math;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.Well19937c;

/**
 * User: christoph
 * Date: 29.05.13
 * Time: 13:17
 */
public class Well1993cSingleton extends RandomAdaptor {

    private static final Well1993cSingleton INSTANCE = new Well1993cSingleton();

    private Well1993cSingleton() {
        super(new Well19937c());
    }

    public static Well1993cSingleton instance() {
        return INSTANCE;
    }
}
