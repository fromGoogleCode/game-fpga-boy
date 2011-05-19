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

public class JavaBoy extends java.applet.Applet implements Runnable, KeyListener {
	static final long serialVersionUID = 10;

	private boolean appletRunning = true;
	private boolean fullFrame = true;
	
	Cartridge cartridge;
	Dmgcpu dmgcpu;
	GraphicsChip graphicsChip;
	Image doubleBuffer;

	static int[] keyCodes = {38, 40, 37, 39, 90, 88, 10, 8};

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

	/** When running as an applet, updates the screen when necessary */
	public void paint(Graphics g) {
		if (dmgcpu != null) {

			// Center the GB image
			//int x = 0;
			//int y = 0;

			if (!fullFrame) {
				dmgcpu.graphicsChip.draw(g, 0, 0, this);
			} else {
				Graphics bufferGraphics = doubleBuffer.getGraphics();

				if (dmgcpu.graphicsChip.isFrameReady()) {
					bufferGraphics.setColor(new Color(255, 255, 255));
					bufferGraphics.fillRect(0, 0, 160, 144);

					dmgcpu.graphicsChip.draw(bufferGraphics, 0, 0, this);

					g.drawImage(doubleBuffer, 0, 0, this);
				} else {
					dmgcpu.graphicsChip.draw(bufferGraphics, 0, 0, this);
				}

			}
		} else {
			g.setColor(new Color(0,0,0));
			g.fillRect(0, 0, 160, 144);
			g.setColor(new Color(255, 255, 255));
			g.drawRect(0, 0, 160, 144);
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
