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
		dmgcpu = new Dmgcpu(this);
		dmgcpu.graphicsChip.setMagnify();
		p.start();
	}

	public void run() {
		do {
			dmgcpu.reset();
			dmgcpu.execute();
		} while (true);
	}

}
