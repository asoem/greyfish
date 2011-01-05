package org.asoem.sico.utils;

public class TableCellEvent {

	private int row;
	private int column;
	private Object oldValue;
	private Object newValue;

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
