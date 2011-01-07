package org.asoem.greyfish.utils;

import org.asoem.greyfish.lang.Command;

public class Commands {

	public static final Command NULL_COMMAND = new Command() {
		
		@Override
		public void execute() {
			// NOP
		}
	};
}
