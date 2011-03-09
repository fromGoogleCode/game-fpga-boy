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
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

/** This is the main controlling class which contains the main() method
 *  to run JavaBoy as an application, and also the necessary applet methods.
 *  It also implements a full command based debugger using the console.
 */
//public class JavaBoy extends java.applet.Applet implements Runnable, KeyListener, WindowListener, ActionListener, ItemListener {
public class JavaBoy extends java.applet.Applet implements Runnable, KeyListener {
	static final long serialVersionUID = 10;
	private static final String hexChars = "0123456789ABCDEF";

	private boolean appletRunning = true;
	private boolean fullFrame = true;

	/** These strings contain all the names for the colour schemes.
	 *  A scheme can be activated using the view menu when JavaBoy is
	 *  running as an application.
	 */
	/*
	static public String[] schemeNames =
	        {"Standard colours", "LCD shades", "Midnight garden", "Psychadelic"};
	*/
	/** This array contains the actual data for the colour schemes.
	 *  These are only using in DMG mode.
	 *  The first four values control the BG palette, the second four
	 *  are the OBJ0 palette, and the third set of four are OBJ1.
	 */
	/*
	static public int[][] schemeColours =
	        {{0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000,
	          0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000,
	          0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000},

	         {0xFFFFFFC0, 0xFFC2C41E, 0xFF949600, 0xFF656600,
	          0xFFFFFFC0, 0xFFC2C41E, 0xFF949600, 0xFF656600,
	          0xFFFFFFC0, 0xFFC2C41E, 0xFF949600, 0xFF656600},

	         {0xFFC0C0FF, 0xFF4040FF, 0xFF0000FF, 0xFF000080,
	          0xFFC0FFC0, 0xFF00C000, 0xFF008000, 0xFF004000,
	          0xFFC0FFC0, 0xFF00C000, 0xFF008000, 0xFF004000},

	         {0xFFFFC0FF, 0xFF8080FF, 0xFFC000C0, 0xFF800080,
	          0xFFFFFF40, 0xFFC0C000, 0xFFFF4040, 0xFF800000,
	          0xFF80FFFF, 0xFF00C0C0, 0xFF008080, 0xFF004000}};
	*/
	/** When emulation running, references the currently loaded cartridge */
	Cartridge cartridge;

	/** When emulation running, references the current CPU object */
	Dmgcpu dmgcpu;

	/** When emulation running, references the current graphics chip implementation */
	GraphicsChip graphicsChip;

	Image doubleBuffer;

	static int[] keyCodes = {38, 40, 37, 39, 90, 88, 10, 8};

	/** True if the image size changed last frame, and we need to repaint the background */
	boolean imageSizeChanged = false;

	/** Returns the unsigned value (0 - 255) of a signed byte */
	static public short unsign(byte b) {
		if (b < 0) {
			return (short) (256 + b);
		} else {
			return b;
		}
	}

	/** Returns the unsigned value (0 - 255) of a signed 8-bit value stored in a short */
	static public short unsign(short b) {
		if (b < 0) {
			return (short) (256 + b);
		} else {
			return b;
		}
	}

	/** Returns a string representation of an 8-bit number in hexadecimal */
	static public String hexByte(int b) {
		String s = new Character(hexChars.charAt(b >> 4)).toString();
		s = s + new Character(hexChars.charAt(b & 0x0F)).toString();

		return s;
	}

	/** Returns a string representation of an 16-bit number in hexadecimal */
	static public String hexWord(int w) {
		return new String(hexByte((w & 0x0000FF00) >>  8) + hexByte(w & 0x000000FF));
	}

	/** When running as an applet, updates the screen when necessary */
	public void paint(Graphics g) {
		if (dmgcpu != null) {

			// Center the GB image
			int x = 0;
			int y = 0;

			if (!fullFrame && !imageSizeChanged) {
				dmgcpu.graphicsChip.draw(g, x, y, this);
			} else {
				Graphics bufferGraphics = doubleBuffer.getGraphics();

				if (dmgcpu.graphicsChip.isFrameReady()) {
					bufferGraphics.setColor(new Color(255, 255, 255));
					bufferGraphics.fillRect(0, 0, 160, 144);

					dmgcpu.graphicsChip.draw(bufferGraphics, x, y, this);

					g.drawImage(doubleBuffer, 0, 0, this);
				} else {
					dmgcpu.graphicsChip.draw(bufferGraphics, x, y, this);
				}

			}
		} else {
			g.setColor(new Color(0,0,0));
			g.fillRect(0, 0, 160, 144);
			g.setColor(new Color(255, 255, 255));
			g.drawRect(0, 0, 160, 144);
			g.drawString("JavaBoy (tm)", 10, 10);
			g.drawString("Version 0.92 Downgrade by ChaoticGabibo", 10, 20);

			g.drawString("Charging flux capacitor...", 10, 40);
			g.drawString("Loading game ROM...", 10, 50);
		}

	}

	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		if (e.getActionCommand().equals("Reset")) {
			dmgcpu.reset();
		}
	}

	public void update(Graphics g) {
		paint(g);
		fullFrame = true;
	}

	public void drawNextFrame() {
		fullFrame = false;
		repaint();
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == keyCodes[0]) {
			dmgcpu.ioHandler.padUp = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[1]) {
			dmgcpu.ioHandler.padDown = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[2]) {
			dmgcpu.ioHandler.padLeft = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[3]) {
			dmgcpu.ioHandler.padRight = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[4]) {
			dmgcpu.ioHandler.padA = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[5]) {
			dmgcpu.ioHandler.padB = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[6]) {
			dmgcpu.ioHandler.padStart = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[7]) {
			dmgcpu.ioHandler.padSelect = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == keyCodes[0]) {
			dmgcpu.ioHandler.padUp = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[1]) {
			dmgcpu.ioHandler.padDown = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[2]) {
			dmgcpu.ioHandler.padLeft = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[3]) {
			dmgcpu.ioHandler.padRight = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[4]) {
			dmgcpu.ioHandler.padA = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[5]) {
			dmgcpu.ioHandler.padB = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[6]) {
			dmgcpu.ioHandler.padStart = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		} else if (key == keyCodes[7]) {
			dmgcpu.ioHandler.padSelect = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
	}

	public void windowClosing(WindowEvent e) {
		dispose();
		System.exit(0);
	}

	/*
	public JavaBoy() {
		addKeyListener(this);
		System.out.println("JavaBoy (tm) Version " + versionString + " (c) 2005 Neil Millstone (applet)");
		cartridge = new Cartridge("../roms/rom.gbc");
		dmgcpu = new Dmgcpu(cartridge, this);
		dmgcpu.graphicsChip.setMagnify(1);
		this.requestFocus();
	}
	*/

	public void start() {
		Thread p = new Thread(this);
		addKeyListener(this);
		System.out.println("JavaBoy (tm) Version 0.92 Downgrade by ChaoticGabibo (c) 2005 Neil Millstone (applet)");
		cartridge = new Cartridge();
		dmgcpu = new Dmgcpu(cartridge, this);
		dmgcpu.graphicsChip.setMagnify(1);
		this.requestFocus();
		p.start();
	}

	public void run() {
		do {
			dmgcpu.reset();
			dmgcpu.execute(-1);
		} while (appletRunning);
		do {
			try {
				java.lang.Thread.sleep(1);
				this.requestFocus();
			} catch (InterruptedException e) {
				System.out.println("Interrupted!");
				break;
			}
		} while (appletRunning);
		dispose();
		System.out.println("Thread terminated");
	}

	/** Free up allocated memory */
	public void dispose() {
		if (cartridge != null) cartridge.dispose();
		if (dmgcpu != null) dmgcpu.dispose();
	}

	public void init() {
		requestFocus();
		doubleBuffer = createImage(160, 144);
	}

	public void stop() {
		System.out.println("Applet stopped");
		appletRunning = false;
		if (dmgcpu != null) dmgcpu.terminate = true;
	}

}
