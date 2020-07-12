package fern.cytoscape;

import java.awt.Color;
import fern.tools.ColorSpectrum;

public class ColorCalculator implements Cloneable {

	private Color reactionColor = Color.red;
	private Color amountBottomColor = Color.white;
	private Color amountTopColor = Color.black;
	private Scale scale = Scale.Linear;
	private double scaleMax = -1;
	
	public Object clone() throws CloneNotSupportedException {
		ColorCalculator re = new ColorCalculator();
		re.reactionColor = reactionColor;
		re.amountBottomColor = amountBottomColor;
		re.amountTopColor = amountTopColor;
		re.scale = scale;
		re.scaleMax = scaleMax;
		return re;
	}
	
	public Color getReactionColor() {
		return reactionColor;
	}

	public void setReactionColor(Color reactionColor) {
		this.reactionColor = reactionColor;
	}

	public Color getAmountBottomColor() {
		return amountBottomColor;
	}

	public void setAmountBottomColor(Color amountBottomColor) {
		this.amountBottomColor = amountBottomColor;
	}

	public Color getAmountTopColor() {
		return amountTopColor;
	}

	public void setAmountTopColor(Color amountTopColor) {
		this.amountTopColor = amountTopColor;
	}

	public Scale getScale() {
		return scale;
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	public double getScaleMax() {
		return scaleMax;
	}

	public void setScaleMax(double scaleMax) {
		this.scaleMax = scaleMax;
	}
	
	public Color getColor(double d, double max) {
		float val;
		if (scale==Scale.Linear) 
			val =(float)(d/(scaleMax<0 ? max : scaleMax));
		else
			val = (float)(Math.log(d+1)/Math.log((scaleMax<0 ? max : scaleMax)+1));
		return ColorSpectrum.getSpectrum(val, amountBottomColor, amountTopColor);
	}


	
	public static enum Scale {
		Linear, Logarithmic
	}


	
	

}
