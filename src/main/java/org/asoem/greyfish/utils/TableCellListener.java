package org.asoem.greyfish.utils;

import java.util.EventListener;

public interface TableCellListener extends EventListener {
	public void cellChange(TableCellEvent evt);
}
