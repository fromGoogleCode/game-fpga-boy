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
		ioWrite(0x40, (short) 0x91);
		ioWrite(0x0F, (short) 0x01);
	}

	/** Read data from IO Ram */
	public short ioRead(int num) {
		switch (num) {

		case 0x41 :         // LCDSTAT

			int output = 0;

			if (dmgcpu.memory[0xFF44] == dmgcpu.memory[0xFF45]) {
				output |= 4;
			}

			int cyclePos = dmgcpu.instrCount % dmgcpu.INSTRS_PER_HBLANK;
			int sectionLength = dmgcpu.INSTRS_PER_HBLANK / 6;

			if (JavaBoy.unsign(dmgcpu.memory[0xFF44]) > 144) {
				output |= 1;
			} else {
				if (cyclePos <= sectionLength * 3) {
					// Mode 0
				} else if (cyclePos <= sectionLength * 4) {
					// Mode 2
					output |= 2;
				} else {
					output |= 3;
				}
			}

			return (byte) (output | (dmgcpu.memory[0xFF41] & 0xF8));

		case 0x55 :
			return (byte) (dmgcpu.memory[0xFF55]);

		case 0x69 :       // GBC BG Sprite palette

			int palNumber = (dmgcpu.memory[0xFF68] & 0x38) >> 3;
			return dmgcpu.graphicsChip.gbcBackground[palNumber].getGbcColours(
		               (JavaBoy.unsign(dmgcpu.memory[0xFF68]) & 0x06) >> 1,
		               (JavaBoy.unsign(dmgcpu.memory[0xFF68]) & 0x01) == 1);

		case 0x6B :       // GBC OBJ Sprite palette

			int index = (dmgcpu.memory[0xFF6A] & 0x38) >> 3;
			return dmgcpu.graphicsChip.gbcSprite[index].getGbcColours(
		               (JavaBoy.unsign(dmgcpu.memory[0xFF6A]) & 0x06) >> 1,
		               (JavaBoy.unsign(dmgcpu.memory[0xFF6A]) & 0x01) == 1);

		default:
			return dmgcpu.memory[(0xFF << 8) + num];
		}
	}

	/** Write data to IO Ram */
	public void ioWrite(int num, short data) {

		switch (num) {
		case 0x00 :           // FF00 - Joypad
			break;
		case 0x02 :           // Serial
			break;
		case 0x04 :           // DIV
			dmgcpu.memory[0xFF04] = 0;
			break;
		case 0x07 :           // TAC
			break;
		case 0x10 :           // Sound channel 1, sweep
			//dmgcpu.memory[0xFF10] = (byte) data;
			break;
		case 0x11 :           // Sound channel 1, length and wave duty
			//dmgcpu.memory[0xFF11] = (byte) data;
			break;
		case 0x12 :           // Sound channel 1, volume envelope
			//dmgcpu.memory[0xFF12] = (byte) data;
			break;
		case 0x13 :           // Sound channel 1, frequency low
			//dmgcpu.memory[0xFF13] = (byte) data;
			break;
		case 0x14 :           // Sound channel 1, frequency high
			//dmgcpu.memory[0xFF14] = (byte) data;
			break;
		case 0x17 :           // Sound channel 2, volume envelope
			//dmgcpu.memory[0xFF17] = (byte) data;
			break;
		case 0x18 :           // Sound channel 2, frequency low
			//dmgcpu.memory[0xFF18] = (byte) data;
			break;
		case 0x19 :           // Sound channel 2, frequency high
			//dmgcpu.memory[0xFF19] = (byte) data;
			break;
		case 0x16 :           // Sound channel 2, length and wave duty
			//dmgcpu.memory[0xFF16] = (byte) data;
			break;
		case 0x1A :           // Sound channel 3, on/off
			//dmgcpu.memory[0xFF1A] = (byte) data;
			break;
		case 0x1B :           // Sound channel 3, length
			//dmgcpu.memory[0xFF1B] = (byte) data;
			break;
		case 0x1C :           // Sound channel 3, volume
			//dmgcpu.memory[0xFF1C] = (byte) data;
			break;
		case 0x1D :           // Sound channel 3, frequency lower 8-bit
			//dmgcpu.memory[0xFF1D] = (byte) data;
			break;
		case 0x1E :           // Sound channel 3, frequency higher 3-bit
			//dmgcpu.memory[0xFF1E] = (byte) data;
			break;

		case 0x20 :           // Sound channel 4, length
			//dmgcpu.memory[0xFF20] = (byte) data;
			break;
		case 0x21 :           // Sound channel 4, volume envelope
			//dmgcpu.memory[0xFF21] = (byte) data;
			break;
		case 0x22 :           // Sound channel 4, polynomial parameters
			//dmgcpu.memory[0xFF22] = (byte) data;
			break;
		case 0x23 :          // Sound channel 4, initial/consecutive
			//dmgcpu.memory[0xFF23] = (byte) data;
			break;
		case 0x25 :           // Stereo select
			//dmgcpu.memory[0xFF25] = (byte) data;
			break;

		case 0x30 :
		case 0x31 :
		case 0x32 :
		case 0x33 :
		case 0x34 :
		case 0x35 :
		case 0x36 :
		case 0x37 :
		case 0x38 :
		case 0x39 :
		case 0x3A :
		case 0x3B :
		case 0x3C :
		case 0x3D :
		case 0x3E :
		case 0x3F :
			dmgcpu.memory[(0xFF << 8)+ num] = (byte) data;
			break;

		case 0x40 :           // LCDC
			dmgcpu.graphicsChip.bgEnabled = true;

			if ((data & 0x20) == 0x20) {     // BIT 5
				dmgcpu.graphicsChip.winEnabled = true;
			} else {
				dmgcpu.graphicsChip.winEnabled = false;
			}

			if ((data & 0x10) == 0x10) {     // BIT 4
				dmgcpu.graphicsChip.bgWindowDataSelect = true;
			} else {
				dmgcpu.graphicsChip.bgWindowDataSelect = false;
			}

			if ((data & 0x08) == 0x08) {
				dmgcpu.graphicsChip.hiBgTileMapAddress = true;
			} else {
				dmgcpu.graphicsChip.hiBgTileMapAddress = false;
			}

			if ((data & 0x04) == 0x04) {      // BIT 2
				dmgcpu.graphicsChip.doubledSprites = true;
			} else {
				dmgcpu.graphicsChip.doubledSprites = false;
			}

			if ((data & 0x02) == 0x02) {     // BIT 1
				dmgcpu.graphicsChip.spritesEnabled = true;
			} else {
				dmgcpu.graphicsChip.spritesEnabled = false;
			}

			if ((data & 0x01) == 0x00) {     // BIT 0
				dmgcpu.graphicsChip.bgEnabled = false;
				dmgcpu.graphicsChip.winEnabled = false;
			}

			dmgcpu.memory[0xFF40] = (byte) data;
			break;

		case 0x41 :
			dmgcpu.memory[0xFF41] = (byte) data;
			break;

		case 0x42 :           // SCY
			dmgcpu.memory[0xFF42] = (byte) data;
			break;

		case 0x43 :           // SCX
			dmgcpu.memory[0xFF43] = (byte) data;
			break;

		case 0x46 :           // DMA
			int sourceAddress = (data << 8);

			// This could be speed up using System.arrayCopy, but hey.
			//for (int i = 0x00; i < 0xA0; i++) {
			//	dmgcpu.addressWrite(0xFE00 + i, dmgcpu.addressRead(sourceAddress + i));
			//}
			System.arraycopy(dmgcpu.memory, sourceAddress, dmgcpu.memory, 0xFE00, 0xA0);
			// This is meant to be run at the same time as the CPU is executing
			// instructions, but I don't think it's crucial.
			break;
		case 0x47 :           // FF47 - BKG and WIN palette
			dmgcpu.graphicsChip.backgroundPalette.decodePalette(data);
			if (dmgcpu.memory[(0xFF << 8) + num] != (byte) data) {
				dmgcpu.memory[(0xFF << 8) + num] = (byte) data;
				dmgcpu.graphicsChip.invalidateAll(GraphicsChip.TILE_BKG);
			}
			break;
		case 0x48 :           // FF48 - OBJ1 palette
			dmgcpu.graphicsChip.obj1Palette.decodePalette(data);
			if (dmgcpu.memory[(0xFF << 8) + num] != (byte) data) {
				dmgcpu.memory[(0xFF << 8) + num] = (byte) data;
				dmgcpu.graphicsChip.invalidateAll(GraphicsChip.TILE_OBJ1);
			}
			break;
		case 0x49 :           // FF49 - OBJ2 palette
			dmgcpu.graphicsChip.obj2Palette.decodePalette(data);
			if (dmgcpu.memory[(0xFF << 8) + num] != (byte) data) {
				dmgcpu.memory[(0xFF << 8) + num] = (byte) data;
				dmgcpu.graphicsChip.invalidateAll(GraphicsChip.TILE_OBJ2);
			}
			break;

		case 0x4F :
			dmgcpu.graphicsChip.tileStart = (data & 0x01) * 384;
			dmgcpu.graphicsChip.vidRamStart = (data & 0x01) * 0x2000;
			dmgcpu.memory[0xFF4F] = (byte) data;
			break;


		case 0x55 :
			break;

		case 0x69 :           // FF69 - BCPD: GBC BG Palette data write

			int palNumber = (dmgcpu.memory[0xFF68] & 0x38) >> 3;
			dmgcpu.graphicsChip.gbcBackground[palNumber].setGbcColours(
			        (JavaBoy.unsign(dmgcpu.memory[0xFF68]) & 0x06) >> 1,
			        (JavaBoy.unsign(dmgcpu.memory[0xFF68]) & 0x01) == 1, JavaBoy.unsign(data));
			dmgcpu.graphicsChip.invalidateAll(palNumber * 4);

			if ((JavaBoy.unsign(dmgcpu.memory[0xFF68]) & 0x80) != 0) {
				dmgcpu.memory[0xFF68]++;
			}

			dmgcpu.memory[0xFF69] = (byte) data;
			break;

		case 0x6B :           // FF6B - OCPD: GBC Sprite Palette data write

			int index = (dmgcpu.memory[0xFF6A] & 0x38) >> 3;
			dmgcpu.graphicsChip.gbcSprite[index].setGbcColours(
			        (JavaBoy.unsign(dmgcpu.memory[0xFF6A]) & 0x06) >> 1,
			        (JavaBoy.unsign(dmgcpu.memory[0xFF6A]) & 0x01) == 1, JavaBoy.unsign(data));
			dmgcpu.graphicsChip.invalidateAll((index * 4) + 32);

			if ((JavaBoy.unsign(dmgcpu.memory[0xFF6A]) & 0x80) != 0) {
				if ((dmgcpu.memory[0xFF6A] & 0x3F) == 0x3F) {
					dmgcpu.memory[0xFF6A] = (byte) 0x80;
				} else {
					dmgcpu.memory[0x6A]++;
				}
			}

			dmgcpu.memory[0xFF6B] = (byte) data;
			break;


		case 0x70 :           // FF70 - GBC Work RAM bank
			if (((data & 0x07) == 0) || ((data & 0x07) == 1)) {
				dmgcpu.gbcRamBank = 1;
			} else {
				dmgcpu.gbcRamBank = data & 0x07;
			}
			dmgcpu.memory[0xFF70] = (byte) data;
			break;

		default:
			dmgcpu.memory[(0xFF << 8) + num] = (byte) data;
			break;
		}
	}
}
