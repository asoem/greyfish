package org.asoem.greyfish.utils.base;

public interface HasName {

    /**
     * @return the name of this object
     */
	public String getName();

    /**
     *
     * @param s the name to compare to
     * @return {@code true} if this components name equals {@code name}.
     */
    public boolean hasName(String s);
}
