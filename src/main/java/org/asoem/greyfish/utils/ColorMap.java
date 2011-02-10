package org.asoem.greyfish.utils;

import java.awt.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

public class ColorMap {


	public final static ColorMap topo = new ColorMap(Color.GREEN, Color.YELLOW, Color.GRAY);
	public final static ColorMap temperature = new ColorMap(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED);


	private final Vector<Color> colors = new Vector<Color>();
	private final double colorsInterval;

	public ColorMap(Color color1, Color color2, Color ... colors) {
		this.colors.add(color1);
		this.colors.add(color2);
        this.colors.addAll(Arrays.asList(colors));
		colorsInterval = (double) 1 / (this.colors.size() - 1);
	}

	public Color getColor(double ratio) {
		if (ratio < 0 || ratio > 1) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("ratio must be in the range [0,1]");
			ratio = Math.max(0, Math.min(ratio, 1));
		}

		/*
		 *  Select the two colors for calculating the gradient
		 *  If the given ratio is exactly one of <colors> further calculation is unneccessary and the color returned immediately
		 */
		if(ratio == 0)
			return colors.get(0);
		Color c1 = Color.black;
		Color c2 = Color.black;

		for (int i = 1; i < colors.size(); i++) {
			double ratioAtColor_i = colorsInterval*i;
			if(ratio == ratioAtColor_i)
				return colors.get(i);
			else if (ratio < ratioAtColor_i || i == colors.size() -1) {
				c1 = colors.get(i-1);
				c2 = colors.get(i);
				break;
			}
		}

		// calculate the rgb values for the ratio between the two selected colors
		ratio = (ratio % colorsInterval) / colorsInterval;
		int red = (int)(c2.getRed() * ratio + c1.getRed() * (1 - ratio));
		int green = (int)(c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
		int blue = (int)(c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));

		return new Color(red, green, blue);

	}
}
