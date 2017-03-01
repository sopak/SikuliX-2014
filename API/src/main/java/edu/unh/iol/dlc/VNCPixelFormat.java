/*
 *                       University of New Hampshire
 *                       InterOperability Laboratory
 *                           Copyright (c) 2014
 *
 * This software is provided by the IOL ``AS IS'' and any express or implied
 * warranties, including, but not limited to, the implied warranties of
 * merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the InterOperability Lab be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages.
 *
 * This software may not be resold without the express permission of
 * the InterOperability Lab.
 *
 * Feedback on this code may be sent to Mike Johnson (mjohnson@iol.unh.edu)
 * and dlnalab@iol.unh.edu.
 */
package edu.unh.iol.dlc;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.util.Hashtable;

class VNCPixelFormat {

	public final int bitsPerPixel;
	public final int depth;
	public final boolean bigEndian;
	public final boolean trueColor;
	public final int redMax;
	public final int greenMax;
	public final int blueMax;
	public final int redShift;
	public final int greenShift;
	public final int blueShift;

	public VNCPixelFormat(int bitsPerPixel, int depth, boolean bigEndian, boolean trueColor, int redMax, int greenMax, int blueMax, int redShift,
						  int greenShift, int blueShift)
	{
		this.bitsPerPixel = bitsPerPixel;
		this.depth = depth;
		this.bigEndian = bigEndian;
		this.trueColor = trueColor;
		this.redMax = redMax;
		this.greenMax = greenMax;
		this.blueMax = blueMax;
		this.redShift = redShift;
		this.greenShift = greenShift;
		this.blueShift = blueShift;
	}

	public BufferedImage createBufferedImage(int w, int h)
	{
		if (bitsPerPixel != 32 || !trueColor || redMax != 255 || greenMax != 255 || blueMax != 255) {
			return null;
		}

		DirectColorModel cm = new DirectColorModel(
				depth,
				0xFF << redShift,
				0xFF << greenShift,
				0xFF << blueShift
		);
		return new BufferedImage(cm, cm.createCompatibleWritableRaster(w, h), false, new Hashtable<>());
	}
}
