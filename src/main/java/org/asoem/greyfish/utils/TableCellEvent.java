package org.asoem.greyfish.utils;

public class TableCellEvent {

	private final int row;
	private final int column;
	private final Object oldValue;
	private final Object newValue;

	public TableCellEvent(int row, int column, Object oldValue, Object newValue) {
		super();
		this.row = row;
		this.column = column;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}
}
