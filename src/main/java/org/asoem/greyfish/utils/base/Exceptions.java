package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 12.04.12
 * Time: 14:44
 *
 * Taken from http://www.eishay.com/2011/11/throw-undeclared-checked-exception-in.html
 */
public final class Exceptions {
    public static RuntimeException asRuntimeException(Exception e) {
        Exceptions.<RuntimeException>throwAs(e);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAs(Throwable e) throws E {
        throw (E)e;
    }
}
