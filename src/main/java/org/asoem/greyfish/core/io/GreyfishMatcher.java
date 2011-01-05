package org.asoem.sico.core.io;

import java.awt.Color;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

public class GreyfishMatcher implements Matcher {

	private volatile static GreyfishMatcher instance;
	
	@SuppressWarnings("rawtypes")
	@Override
	public Transform match(Class arg0) throws Exception {
		if(arg0.isEnum()
				|| arg0.getSuperclass() != null
					&& arg0.getSuperclass().isEnum()) { // This is a Workaround for a java bug. See: http://forums.oracle.com/forums/thread.jspa?threadID=1035332
			return new EnumTransform(arg0);
		}
		else if (Color.class.equals(arg0)) {
			return new ColorTransform();
		}
		return null;
	}
	
	private GreyfishMatcher() {
	}

	public static synchronized GreyfishMatcher getInstance() {
		if (instance == null)
			instance = new GreyfishMatcher();
		return instance;
	}

}
