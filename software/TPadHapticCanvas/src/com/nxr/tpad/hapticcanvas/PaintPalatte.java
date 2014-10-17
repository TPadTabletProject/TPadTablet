package com.nxr.tpad.hapticcanvas;

import android.graphics.Color;

public class PaintPalatte {

	private static final int numColors = 25;

	public static final int[] colors = new int[numColors];

	public PaintPalatte() {

		// Initialize all colors
		colors[0] = Color.CYAN; 
		colors[5] = Color.RED;
		colors[10] = Color.YELLOW;
		colors[15] = Color.GREEN;
		colors[20] = Color.BLUE;
		
		
	}

	public int length() {
		return numColors;
	}

	public void setColor(int index, int col) {

		colors[index] = col;

	}
	

	public int[] getColors() {

		return colors;

	}

	public int getColor(int c) {
		return colors[c];
	}

}
