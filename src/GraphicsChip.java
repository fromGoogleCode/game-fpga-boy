/*

JavaBoy
                                  
COPYRIGHT (C) 2001 Neil Millstone and The Victoria University of Manchester
                                                                         ;;;
This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the Free
Software Foundation; either version 2 of the License, or (at your option)
any later version.        

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
more details.


You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 59 Temple
Place - Suite 330, Boston, MA 02111-1307, USA.

*/

import java.awt.*;

/** This class is the master class for implementations
  *  of the graphics class.  A graphics implementation will subclass from this class.
  *  It contains methods for calculating the frame rate. */

abstract class GraphicsChip {
	/** Tile uses the background palette */
	static final int TILE_BKG = 0;

	/** Tile uses the first sprite palette */
	static final int TILE_OBJ1 = 4;

	/** Tile uses the second sprite palette */
	static final int TILE_OBJ2 = 8;

	/** Tile is flipped horizontally */
	static final int TILE_FLIPX = 1;

	/** Tile is flipped vertically */
	static final int TILE_FLIPY = 2;

	/** The current contents of the video memory, mapped in at 0x8000 - 0x9FFF */
	byte[] videoRam = new byte[0x8000];

	/** The background palette */
	GameboyPalette backgroundPalette;

	/** The first sprite palette */
	GameboyPalette obj1Palette;

	/** The second sprite palette */
	GameboyPalette obj2Palette;
	GameboyPalette[] gbcBackground = new GameboyPalette[8];
	GameboyPalette[] gbcSprite = new GameboyPalette[8];

	boolean spritesEnabled = true;

	boolean bgEnabled = true;
	boolean winEnabled = true;

	/** The image containing the Gameboy screen */
	Image backBuffer;

	/** The current frame skip value */
	int frameSkip = 1;

	/** The number of frames that have been drawn so far in the current frame sampling period */
	int framesDrawn = 0;

	/** Amount of time to wait between frames (ms) */
	int frameWaitTime = 0;

	/** The current frame has finished drawing */
	boolean frameDone = false;
	long startTime = 0;

	/** Selection of one of two addresses for the BG and Window tile data areas */
	boolean bgWindowDataSelect = true;

	/** If true, 8x16 sprites are being used.  Otherwise, 8x8. */
	boolean doubledSprites = false;
	// are there only 2 types??

	/** Selection of one of two address for the BG tile map. */
	boolean hiBgTileMapAddress= false;
	Dmgcpu dmgcpu;
	Component applet;
	int tileStart = 0;
	int vidRamStart = 0;

	/** Create a new GraphicsChip connected to the specified CPU */
	public GraphicsChip(Component a, Dmgcpu d) {
		dmgcpu = d;

		backgroundPalette = new GameboyPalette(0, 1, 2, 3);
		obj1Palette = new GameboyPalette(0, 1, 2, 3);
		obj2Palette = new GameboyPalette(0, 1, 2, 3);

		for (int r = 0; r < 8; r++) {
			gbcBackground[r] = new GameboyPalette(0, 1, 2, 3);
			gbcSprite[r] = new GameboyPalette(0, 1, 2, 3);
		}

		backBuffer = a.createImage(160, 144);
		applet = a;
	} /** Set the magnification for the screen */

	public void setMagnify(int m) {
		if (backBuffer != null) backBuffer.flush();
		backBuffer = applet.createImage(160, 144);
	}

	/** Clear up any allocated memory */
	public void dispose() {
		backBuffer.flush();
	}

	abstract public short addressRead(int addr);
	abstract public void addressWrite(int addr, byte data);
	abstract public void invalidateAll(int attribs);
	abstract public boolean draw(Graphics g, int startX, int startY, Component a);
	abstract public void notifyScanline(int line);
	abstract public void invalidateAll();
	abstract public boolean isFrameReady();
}
