package org.asoem.greyfish.core.io;

import org.simpleframework.xml.transform.Transform;

import java.awt.*;

public class ColorTransform implements Transform<Color> {

	@Override
	public Color read(String arg0) throws Exception {
		return new Color(Integer.valueOf(arg0));
	}

	@Override
	public String write(Color arg0) throws Exception {
		return String.valueOf(arg0.getRGB());
	}

}
