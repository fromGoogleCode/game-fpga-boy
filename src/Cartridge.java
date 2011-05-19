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

//import java.awt.*;
import java.io.*;

class Cartridge {

	/** Contains the complete ROM image of the cartridge */
	public byte[] rom = new byte[0x8000];

	/** Create a cartridge object, loading ROM and any associated battery RAM from the cartridge
	 *  filename given.  Loads via the web if JavaBoy is running as an applet */
	public Cartridge() {
		try {
			InputStream is = new FileInputStream(new File("../roms/rom.gb"));
			is.read(rom); // Read the entire ROM
			is.close();
		} catch (IOException e) {
			System.out.println("Error opening ROM image 'rom.gbc'!");
		} catch (IndexOutOfBoundsException e) {
			System.out.print("Error, Loading the ROM image failed.The file is not a valid Gameboy ROM.");
		}
	}

	/** Returns the byte currently mapped to a CPU address.  Addr must be in the range 0x0000 - 0x4000 or
	 *  0xA000 - 0xB000 (for RAM access)
	 */
	public final byte addressRead(int addr) {
		return (byte) (rom[addr]);
	}

	/** Performs saving of the battery RAM before the object is discarded */
	public void dispose() {
	}

}
