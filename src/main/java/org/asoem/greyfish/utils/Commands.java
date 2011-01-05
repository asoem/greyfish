package org.asoem.sico.utils;

import org.asoem.sico.lang.Command;

public class Commands {

	public static final Command NULL_COMMAND = new Command() {
		
		@Override
		public void execute() {
			// NOP
		}
	};
}
