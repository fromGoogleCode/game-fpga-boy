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
import java.io.*;

/** This class represents the game cartridge and contains methods to load the ROM and battery RAM
 *  (if necessary) from disk or over the web, and handles emulation of ROM mappers and RAM banking.
 *  It is missing emulation of MBC3 (this is very rare).
 */

class Cartridge {
	/** Translation between ROM size byte contained in the ROM header, and the number
	 *  of 16Kb ROM banks the cartridge will contain
	 */
	final int[][] romSizeTable = {{0, 2}, {1, 4}, {2, 8}, {3, 16}, {4, 32},
	                              {5, 64}, {6, 128}, {7, 256}, {0x52, 72}, {0x53, 80}, {0x54, 96}};

	/** Contains the complete ROM image of the cartridge */
	public byte[] rom;

	/** Number of 16Kb ROM banks */
	int numBanks = 2;

	/** Cartridge type - index into cartTypeTable[][] */
	//int cartType = 0;

	/** Starting address of the ROM bank at 0x4000 in CPU address space */
	int pageStart = 0x4000;

	/** The bank number which is currently mapped at 0x4000 in CPU address space */
	int currentBank = 1;

	/** The bank which has been saved when the debugger changes the ROM mapping.  The mapping is
	 *  restored from this register when execution resumes */
	int savedBank = -1;

	/** The RAM bank number which is currently mapped at 0xA000 in CPU address space */
	int ramBank;
	int ramPageStart;

	boolean mbc1LargeRamMode = false;
	boolean ramEnabled = false;
	Component applet;

	boolean needsReset = false;

	/** Real time clock registers.  Only used on MBC3 */
	//int[] RTCReg = new int[5];
	//long realTimeStart;
	//long lastSecondIncrement;
	String romIntFileName;

	/** Create a cartridge object, loading ROM and any associated battery RAM from the cartridge
	 *  filename given.  Loads via the web if JavaBoy is running as an applet */
	public Cartridge() {
		rom = new byte[0x04000 * numBanks];   // Recreate the ROM array with the correct size
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
		if (addr < 0x4000) {
			return (byte) (rom[addr]);
		} else {
			return (byte) (rom[pageStart + addr - 0x4000]);
		}
	}

	/** Performs saving of the battery RAM before the object is discarded */
	public void dispose() {
	}

	public boolean verifyChecksum() {
		int checkSum = (JavaBoy.unsign(rom[0x14E]) << 8) + JavaBoy.unsign(rom[0x14F]);

		int total = 0;                   // Calculate ROM checksum
		for (int r=0; r < rom.length; r++) {
			if ((r != 0x14E) && (r != 0x14F)) {
				total = (total + JavaBoy.unsign(rom[r])) & 0x0000FFFF;
			}
		}

		return checkSum == total;
	}

	/** Returns the number of 16Kb banks in a cartridge from the header size byte. */
	public int lookUpCartSize(int sizeByte) {
		int i = 0;
		while ((i < romSizeTable.length) && (romSizeTable[i][0] != sizeByte)) {
			i++;
		}

		if (romSizeTable[i][0] == sizeByte) {
			return romSizeTable[i][1];
		} else {
			return -1;
		}
	}

}
