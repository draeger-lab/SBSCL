package fern.tools;

import java.awt.Color;

/**
 * 
 * Provides methods to assign colors to numbers between 0 and 1.
 * 
 * @author Florian Erhard
 *
 */
public class ColorSpectrum {

	/**
	 * Gets a gray scale color representing the given number, 0 means white, 1 means black.
	 * 
	 * @param value	between 0 and 1
	 * @return		gray scale color
	 */
	public static Color getGrayScaleSpectrum(float value) {
		return getSpectrum(value, Color.white, Color.black);
	}
	
	/**
	 * Gets a color representing the given number in an given color scale.
	 * You can give as much colors as you want. The resulting color will be interpolated.
	 * 
	 * @param val		between 0 and 1
	 * @param colors	color spectrum
	 * @return			color for the value
	 */
	public static Color getSpectrum(float val,Color... colors) {
		if (val>1) return colors[colors.length-1];
		
		int leftIndex = (int) Math.floor((colors.length-1)*val);
		int rightIndex = (int) Math.ceil((colors.length-1)*val);
		
		if (leftIndex==rightIndex)
			return colors[leftIndex];
		
		val = (val-(float)leftIndex/(float)(colors.length-1))*(colors.length-1);
		
		return new Color(
				colors[leftIndex].getRed()/255f*(1-val)+colors[rightIndex].getRed()/255f*val,
				colors[leftIndex].getGreen()/255f*(1-val)+colors[rightIndex].getGreen()/255f*val,
				colors[leftIndex].getBlue()/255f*(1-val)+colors[rightIndex].getBlue()/255f*val
				);
	}

	
	
}
