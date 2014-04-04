package org.asoem.greyfish.utils.base;

public class ClassNotInstantiableError extends Error {
    public ClassNotInstantiableError() {
        super("Not instantiable");
    }
}
