package org.asoem.sico.utils;

import java.util.EventListener;

public interface TableCellListener extends EventListener {
	public void cellChange(TableCellEvent evt);
}
