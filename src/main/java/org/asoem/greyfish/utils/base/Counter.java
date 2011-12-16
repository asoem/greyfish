package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 19.04.11
 * Time: 14:21
 */
public class Counter {

    private int n;

    public Counter(int n) {
        this.n = n;
    }

    public Counter() {
    }

    public void increase() {
        n++;
    }

    public void decrease() {
        n--;
    }

    public int get() {
        return n;
    }
}
