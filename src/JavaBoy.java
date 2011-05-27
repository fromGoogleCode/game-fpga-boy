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

public class JavaBoy extends java.applet.Applet implements Runnable {
	static final long serialVersionUID = 10;

	private boolean appletRunning = true;
	
	Dmgcpu dmgcpu;

	static public short unsign(byte b) {
		if (b < 0) 
			return (short) (256 + b);
		else
			return b;
	}

	static public short unsign(short b) {
		if (b < 0)
			return (short) (256 + b);
		else
			return b;
	}

	/** When running as an applet, updates the screen when necessary */
	public void paint(Graphics g) {
		dmgcpu.graphicsChip.draw(g, 0, 0, this);
	}

	public void drawNextFrame() {
		repaint();
	}

	public void start() {
		Thread p = new Thread(this);
		//System.out.println("JavaBoy (tm) Version 0.92 Downgrade by ChaoticGabibo (c) 2005 Neil Millstone (applet)");
		dmgcpu = new Dmgcpu(this);
		dmgcpu.graphicsChip.setMagnify();
		p.start();
	}

	public void run() {
		do {
			dmgcpu.reset();
			dmgcpu.execute();
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
		if (dmgcpu != null) dmgcpu.dispose();
	}

	//public void stop() {
	//	System.out.println("Applet stopped");
	//	appletRunning = false;
	//	if (dmgcpu != null) dmgcpu.terminate = true;
	//}

}
