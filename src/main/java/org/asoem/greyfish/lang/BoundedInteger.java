package org.asoem.greyfish.lang;

public class BoundedInteger extends Number implements Comparable<Integer>, WellOrderedSetElement<Integer> {

	public final int min;
	public final int max;
	
	private int val;

	public BoundedInteger(final int min, final int max, final int val) {
		this.min = min;
		this.max = max;
		this.val = val;
	}
	
	@Override
	public int intValue() {
		return val;
	}

	@Override
	public long longValue() {
		return (long) val;
	}

	@Override
	public float floatValue() {
		return (float) val;
	}

	@Override
	public double doubleValue() {
		return (double) val;
	}

	private boolean checkBounds(final int val) {
		return val >= min && val <= max;
	}

	@Override
	public int compareTo(Integer o) {
		return new Integer(val).compareTo(o);
	}
	
	public void setVal(int val) {
		if (checkBounds(val))
			this.val = val;
	}

    @Override
    public Integer getUpperBound() {
        return max;
    }

    @Override
    public Integer getLowerBound() {
        return min;
    }

    @Override
    public Integer get() {
        return val;
    }
}
