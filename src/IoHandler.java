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

/** This class handles all the memory mapped IO in the range
 *  FF00 - FF4F.  It also handles high memory accessed by the
 *  LDH instruction which is locked at FF50 - FFFF.
 */

class IoHandler {

	/** Reference to the current CPU object */
	Dmgcpu dmgcpu;

	/** Create an IoHandler for the specified CPU */
	public IoHandler(Dmgcpu d) {
		dmgcpu = d;
		reset();
	}

	/** Initialize IO to initial power on state */
	public void reset() {
	}
}
