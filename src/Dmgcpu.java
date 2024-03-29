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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/** This is the main controlling class for the emulation
 *  It contains the code to emulate the Z80-like processor
 *  found in the Gameboy, and code to provide the locations
 *  in CPU address space that points to the correct area of
 *  ROM/RAM/IO.
 */
class Dmgcpu {
	// Registers: 8-bit
	int a, b, c, d, e, f;
	// Registers: 16-bit
	public int sp, pc, hl;

	// The number of instructions that have been executed since the last reset
	int instrCount = 0;

	boolean interruptsEnabled = false;

	// Used to implement the IE delay slot
	int ieDelay = -1;

	final short F_ZERO =      0x80; // Zero flag
	final short F_SUBTRACT =  0x40; // Subtract/negative flag
	final short F_HALFCARRY = 0x20; // Half carry flag
	final short F_CARRY =     0x10; // Carry flag

	final short INSTRS_PER_VBLANK = 9000;

	/** Used to set the speed of the emulator.  This controls how
	 *  many instructions are executed for each horizontal line scanned
	 *  on the screen.  Multiply by 154 to find out how many instructions
	 *  per frame.
	 */
	short INSTRS_PER_HBLANK = 60;
	short INSTRS_PER_DIV = 33; // Used to set the speed of DIV increments

	// Constants for interrupts
	public final short INT_VBLANK =  0x01; // Vertical blank interrupt
	public final short INT_LCDC =    0x02; // LCD Coincidence interrupt
	public final short INT_TIMA =    0x04; // TIMA (programmable timer) interrupt
	public final short INT_SER =     0x08; // Serial interrupt
	public final short INT_P10 =     0x10; // P10 - P13 (Joypad) interrupt

	TileBasedGraphicsChip graphicsChip;
	Component applet;
	boolean terminate;

	int gbcRamBank = 1;

	byte[] memory = new byte[0x10000];
	
	/** Create a CPU emulator with the supplied cartridge and game link objects.  Both can be set up
	 *  or changed later if needed
	 */
	public Dmgcpu(Component a) {
		try {
			InputStream is = new FileInputStream(new File("../roms/rom.gb"));
			is.read(memory, 0, 0x8000);
			is.close();
		} catch (IOException e) {
			System.out.println("Error opening ROM image");
		}
		graphicsChip = new TileBasedGraphicsChip(a, this);
		applet = a;
	}

	/** Performs a CPU address space write.  Maps all of the relevant object into the right parts of
	 *  memory.
	 */
	public final void addressWrite(int addr, int data) {

		switch (addr & 0xF000) {
		case 0x0000 :
		case 0x1000 :
		case 0x2000 :
		case 0x3000 :
		case 0x4000 :
		case 0x5000 :
		case 0x6000 :
		case 0x7000 :
			break;

		case 0x8000 :
		case 0x9000 :
				memory[addr] = (byte) data;
			break;

		case 0xA000 :
		case 0xB000 :
			break;

		case 0xC000 :
		case 0xD000 :
		case 0xE000 :
			memory[addr] = (byte) data;
			break;

		case 0xF000 :
			if (addr < 0xFE00) {
				memory[addr] = (byte) data;
			} else if (addr < 0xFF00) {
				memory[addr] = (byte) data;
			} else {
				switch (addr) {
				case 0xFF00 :           // FF00 - Joypad
					break;
				case 0xFF02 :           // Serial
					break;
				case 0xFF04 :           // DIV
					memory[0xFF04] = 0;
					break;
				case 0xFF07 :           // TAC
					break;
				case 0xFF10 :           // Sound channel 1, sweep
					//memory[0xFF10] = (byte) data;
					break;
				case 0xFF11 :           // Sound channel 1, length and wave duty
					//memory[0xFF11] = (byte) data;
					break;
				case 0xFF12 :           // Sound channel 1, volume envelope
					//memory[0xFF12] = (byte) data;
					break;
				case 0xFF13 :           // Sound channel 1, frequency low
					//memory[0xFF13] = (byte) data;
					break;
				case 0xFF14 :           // Sound channel 1, frequency high
					//memory[0xFF14] = (byte) data;
					break;
				case 0xFF17 :           // Sound channel 2, volume envelope
					//memory[0xFF17] = (byte) data;
					break;
				case 0xFF18 :           // Sound channel 2, frequency low
					//memory[0xFF18] = (byte) data;
					break;
				case 0xFF19 :           // Sound channel 2, frequency high
					//memory[0xFF19] = (byte) data;
					break;
				case 0xFF16 :           // Sound channel 2, length and wave duty
					//memory[0xFF16] = (byte) data;
					break;
				case 0xFF1A :           // Sound channel 3, on/off
					//memory[0xFF1A] = (byte) data;
					break;
				case 0xFF1B :           // Sound channel 3, length
					//memory[0xFF1B] = (byte) data;
					break;
				case 0xFF1C :           // Sound channel 3, volume
					//memory[0xFF1C] = (byte) data;
					break;
				case 0xFF1D :           // Sound channel 3, frequency lower 8-bit
					//memory[0xFF1D] = (byte) data;
					break;
				case 0xFF1E :           // Sound channel 3, frequency higher 3-bit
					//memory[0xFF1E] = (byte) data;
					break;
				case 0xFF20 :           // Sound channel 4, length
					//memory[0xFF20] = (byte) data;
					break;
				case 0xFF21 :           // Sound channel 4, volume envelope
					//memory[0xFF21] = (byte) data;
					break;
				case 0xFF22 :           // Sound channel 4, polynomial parameters
					//memory[0xFF22] = (byte) data;
					break;
				case 0xFF23 :          // Sound channel 4, initial/consecutive
					//memory[0xFF23] = (byte) data;
					break;
				case 0xFF25 :           // Stereo select
					//memory[0xFF25] = (byte) data;
					break;
	
				case 0xFF30 :
				case 0xFF31 :
				case 0xFF32 :
				case 0xFF33 :
				case 0xFF34 :
				case 0xFF35 :
				case 0xFF36 :
				case 0xFF37 :
				case 0xFF38 :
				case 0xFF39 :
				case 0xFF3A :
				case 0xFF3B :
				case 0xFF3C :
				case 0xFF3D :
				case 0xFF3E :
				case 0xFF3F :
					memory[addr] = (byte) data;
					break;
	
				case 0xFF40 :           // LCDC
					graphicsChip.bgEnabled = true;
	
					if ((data & 0x20) == 0x20)     // BIT 5
						graphicsChip.winEnabled = true;
					else
						graphicsChip.winEnabled = false;
	
					if ((data & 0x10) == 0x10)     // BIT 4
						graphicsChip.bgWindowDataSelect = true;
					else
						graphicsChip.bgWindowDataSelect = false;
	
					if ((data & 0x08) == 0x08)
						graphicsChip.hiBgTileMapAddress = true;
					else
						graphicsChip.hiBgTileMapAddress = false;
	
					if ((data & 0x04) == 0x04)      // BIT 2
						graphicsChip.doubledSprites = true;
					else
						graphicsChip.doubledSprites = false;
	
					if ((data & 0x02) == 0x02)     // BIT 1
						graphicsChip.spritesEnabled = true;
					else
						graphicsChip.spritesEnabled = false;
	
					if ((data & 0x01) == 0x00) {     // BIT 0
						graphicsChip.bgEnabled = false;
						graphicsChip.winEnabled = false;
					}
	
					memory[0xFF40] = (byte) data;
					break;
	
				case 0xFF41 :
					memory[0xFF41] = (byte) data;
					break;
	
				case 0xFF42 :           // SCY
					memory[0xFF42] = (byte) data;
					break;
	
				case 0xFF43 :           // SCX
					memory[0xFF43] = (byte) data;
					break;
	
				case 0xFF46 :           // DMA
					int sourceAddress = (data << 8);
					System.arraycopy(memory, sourceAddress, memory, 0xFE00, 0xA0);
					// This is meant to be run at the same time as the CPU is executing
					// instructions, but I don't think it's crucial.
					break;
				case 0xFF47 :           // FF47 - BKG and WIN palette
					graphicsChip.backgroundPalette.decodePalette(data);
					if (memory[addr] != (byte) data) {
						memory[addr] = (byte) data;
						graphicsChip.invalidateAll(GraphicsChip.TILE_BKG);
					}
					break;
				case 0xFF48 :           // FF48 - OBJ1 palette
					graphicsChip.obj1Palette.decodePalette(data);
					if (memory[addr] != (byte) data) {
						memory[addr] = (byte) data;
						graphicsChip.invalidateAll(GraphicsChip.TILE_OBJ1);
					}
					break;
				case 0xFF49 :           // FF49 - OBJ2 palette
					graphicsChip.obj2Palette.decodePalette(data);
					if (memory[addr] != (byte) data) {
						memory[addr] = (byte) data;
						graphicsChip.invalidateAll(GraphicsChip.TILE_OBJ2);
					}
					break;
	
				case 0xFF4F :
					//System.out.println("LALALALALALA");
					//graphicsChip.tileStart = (data & 0x01) * 384;
					//graphicsChip.vidRamStart = (data & 0x01) * 0x2000;
					//memory[0xFF4F] = (byte) data;
					break;
	
	
				case 0xFF55 :
					break;
	
				case 0xFF69 :           // FF69 - BCPD: GBC BG Palette data write
	
					int palNumber = (memory[0xFF68] & 0x38) >> 3;
					graphicsChip.gbcBackground[palNumber].setGbcColours(
					        (JavaBoy.unsign(memory[0xFF68]) & 0x06) >> 1,
					        (JavaBoy.unsign(memory[0xFF68]) & 0x01) == 1, JavaBoy.unsign((byte) data));
					graphicsChip.invalidateAll(palNumber * 4);
	
					if ((JavaBoy.unsign(memory[0xFF68]) & 0x80) != 0) {
						memory[0xFF68]++;
					}
	
					memory[0xFF69] = (byte) data;
					break;
	
				case 0xFF6B :           // FF6B - OCPD: GBC Sprite Palette data write
	
					int index = (memory[0xFF6A] & 0x38) >> 3;
					graphicsChip.gbcSprite[index].setGbcColours(
					        (JavaBoy.unsign(memory[0xFF6A]) & 0x06) >> 1,
					        (JavaBoy.unsign(memory[0xFF6A]) & 0x01) == 1, JavaBoy.unsign((byte) data));
					graphicsChip.invalidateAll((index * 4) + 32);
	
					if ((JavaBoy.unsign(memory[0xFF6A]) & 0x80) != 0) {
						if ((memory[0xFF6A] & 0x3F) == 0x3F) {
							memory[0xFF6A] = (byte) 0x80;
						} else {
							memory[0x6A]++;
						}
					}
	
					memory[0xFF6B] = (byte) data;
					break;
	
				case 0xFF70 :           // FF70 - GBC Work RAM bank
					if (((data & 0x07) == 0) || ((data & 0x07) == 1)) {
						gbcRamBank = 1;
					} else {
						gbcRamBank = data & 0x07;
					}
					memory[0xFF70] = (byte) data;
					break;
	
				default:
					memory[addr] = (byte) data;
					break;
				}
			}
		}
	}

	/** Performs a read of a register by internal register number */
	public final int registerRead(int regNum) {
		switch (regNum) {
		case 0  : return b;
		case 1  : return c;
		case 2  : return d;
		case 3  : return e;
		case 4  : return (short) ((hl & 0xFF00) >> 8);
		case 5  : return (short) (hl & 0x00FF);
		case 6  : return JavaBoy.unsign(memory[hl]);
		case 7  : return a;
		default : return -1;
		}
	}

	/** Performs a write of a register by internal register number */
	public final void registerWrite(int regNum, int data) {
		switch (regNum) {
		case 0  : b = (short) data;
			return;
		case 1  : c = (short) data;
			return;
		case 2  : d = (short) data;
			return;
		case 3  : e = (short) data;
			return;
		case 4  : hl = (hl & 0x00FF) | (data << 8);
			return;
		case 5  : hl = (hl & 0xFF00) | data;
			return;
		case 6  : addressWrite(hl, data);
			return;
		case 7  : a = (short) data;
			return;
		default : return;
		}
	}

	/** Resets the CPU to it's power on state.  Memory contents are not cleared. */
	public void reset() {
		interruptsEnabled = false;
		ieDelay = -1;
		pc = 0x0100;
		sp = 0xFFFE;
		f = 0xB0;
		gbcRamBank = 1;
		instrCount = 0;

		a = 0x11;
		b = 0;
		c = 0;
		d = 0;
		e = 0;
		hl = 0;

		addressWrite(0xFF40, 0x91);
		addressWrite(0xFF0F, 0x01);
	}

	/** If an interrupt is enabled an the interrupt register shows that it has occurred, jump to
	 *  the relevant interrupt vector address
	 */
	public final void checkInterrupts() {
		int intFlags = memory[0xFF0F];
		int ieReg = memory[0xFFFF];
		if ((intFlags & ieReg) != 0) {
			sp -= 2;
			addressWrite(sp + 1, pc >> 8);  // Push current program counter onto stack
			addressWrite(sp, pc & 0x00FF);
			interruptsEnabled = false;

			if ((intFlags & ieReg & INT_VBLANK) != 0) {
				pc = 0x40;                      // Jump to Vblank interrupt address
				intFlags -= INT_VBLANK;
			} else if ((intFlags & ieReg & INT_LCDC) != 0) {
				pc = 0x48;
				intFlags -= INT_LCDC;
			} else if ((intFlags & ieReg & INT_TIMA) != 0) {
				pc = 0x50;
				intFlags -= INT_TIMA;
			} else if ((intFlags & ieReg & INT_SER) != 0) {
				pc = 0x58;
				intFlags -= INT_SER;
			} else if ((intFlags & ieReg & INT_P10) != 0) {	// Joypad interrupt
				pc = 0x60;
				intFlags -= INT_P10;
			} /* Other interrupts go here, not done yet */

			memory[0xFF0F] = (byte) intFlags;
		}
	}

	/** Initiate an interrupt of the specified type */
	public final void triggerInterrupt(int intr) {
		memory[0xFF0F] |= intr;
	}

	public final void triggerInterruptIfEnabled(int intr) {
		if ((memory[0xFFFF] & (short) (intr)) != 0) memory[0xFF0F] |= intr;
	}

	/** Check for interrupts that need to be initiated */
	public final void initiateInterrupts() {
	
		if ((instrCount % INSTRS_PER_DIV) == 0) {
			memory[0xFF04]++;
		}
		
		if ((instrCount % INSTRS_PER_HBLANK) == 0) {
			// LCY Coincidence
			// The +1 is due to the LCY register being just about to be incremented
			int cline = JavaBoy.unsign(memory[0xFF44]) + 1;
			if (cline == 152) cline = 0;

			if (((memory[0xFFFF] & INT_LCDC) != 0) &&
			                ((memory[0xFF41] & 64) != 0) &&
			                (JavaBoy.unsign(memory[0xFF45]) == cline) && ((memory[0xFF40] & 0x80) != 0) && (cline < 0x90)) {
				triggerInterrupt(INT_LCDC);
			}

			// Trigger on every line
			if (((memory[0xFFFF] & INT_LCDC) != 0) &&
			                ((memory[0xFF41] & 0x8) != 0) && ((memory[0xFF40] & 0x80) != 0) && (cline < 0x90) ) {
				triggerInterrupt(INT_LCDC);
			}

			if (JavaBoy.unsign(memory[0xFF44]) == 143) {
				for (int r = 144; r < 170; r++) {
					graphicsChip.notifyScanline(r);
				}
				if ( ((memory[0xFF40] & 0x80) != 0) && ((memory[0xFFFF] & INT_VBLANK) != 0) ) {
					triggerInterrupt(INT_VBLANK);
					if ( ((memory[0xFF41] & 16) != 0) && ((memory[0xFFFF] & INT_LCDC) != 0) ) {
						triggerInterrupt(INT_LCDC);
					}
				}

			}

			graphicsChip.notifyScanline(JavaBoy.unsign(memory[0xFF44]));
			memory[0xFF44] = (byte) (JavaBoy.unsign(memory[0xFF44]) + 1);

			if (JavaBoy.unsign(memory[0xFF44]) >= 153) {

				memory[0xFF44] = 0;
				graphicsChip.frameDone = false;
				((JavaBoy) (applet)).drawNextFrame();
				try {
					while (!graphicsChip.frameDone) {
						java.lang.Thread.sleep(1);
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/** Execute the specified number of Gameboy instructions. */
	public final void execute() {

		terminate = false;
		short newf;
		int dat;
		graphicsChip.startTime = System.currentTimeMillis();
		int b1, b2, b3, offset;

		while (!terminate) {

			instrCount++;

			b1 = JavaBoy.unsign(memory[pc]);
			offset = memory[pc + 1];
			b3 = JavaBoy.unsign(memory[pc + 2]);
			b2 = JavaBoy.unsign((short) offset);

			switch (b1) {
			case 0x00 :               // NOP
				pc++;
				break;
			case 0x01 :               // LD BC, nn
				pc+=3;
				b = b3;
				c = b2;
				break;
			case 0x02 :               // LD (BC), A
				pc++;
				addressWrite((b << 8) | c, a);
				break;
			case 0x03 :               // INC BC
				pc++;
				c++;
				if (c == 0x0100) {
					b++;
					c = 0;
					if (b == 0x0100) {
						b = 0;
					}
				}
				break;
			case 0x04 :               // INC B
				pc++;
				f &= F_CARRY;
				switch (b) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					b = 0x00;
					break;
				case 0x0F: f |= F_HALFCARRY;
					b = 0x10;
					break;
				default:   b++;
					break;
				}
				break;
			case 0x05 :               // DEC B
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				switch (b) {
				case 0x00: f |= F_HALFCARRY;
					b = 0xFF;
					break;
				case 0x10: f |= F_HALFCARRY;
					b = 0x0F;
					break;
				case 0x01: f |= F_ZERO;
					b = 0x00;
					break;
				default:   b--;
					break;
				}
				break;
			case 0x06 :               // LD B, nn
				pc += 2;
				b = b2;
				break;
			case 0x07 :               // RLC A
				pc++;
				f = 0;

				a <<= 1;

				if ((a & 0x0100) != 0) {
					f |= F_CARRY;
					a |= 1;
					a &= 0xFF;
				}
				if (a == 0) {
					f |= F_ZERO;
				}
				break;
			case 0x08 :               // LD (nnnn), SP   /* **** May be wrong! **** */
				pc+=3;
				addressWrite((b3 << 8) + b2 + 1, (sp & 0xFF00) >> 8);
				addressWrite((b3 << 8) + b2, (sp & 0x00FF));
				break;
			case 0x09 :               // ADD HL, BC
				pc++;
				hl = (hl + ((b << 8) + c));
				if ((hl & 0xFFFF0000) != 0) {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)) | (F_CARRY));
					hl &= 0xFFFF;
				} else {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)));
				}
				break;
			case 0x0A :               // LD A, (BC)
				pc++;
				a = JavaBoy.unsign(memory[(b << 8) + c]);
				break;
			case 0x0B :               // DEC BC
				pc++;
				c--;
				if ((c & 0xFF00) != 0) {
					c = 0xFF;
					b--;
					if ((b & 0xFF00) != 0) {
						b = 0xFF;
					}
				}
				break;
			case 0x0C :               // INC C
				pc++;
				f &= F_CARRY;
				switch (c) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					c = 0x00;
					break;
				case 0x0F: f |= F_HALFCARRY;
					c = 0x10;
					break;
				default:   c++;
					break;
				}
				break;
			case 0x0D :               // DEC C
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				switch (c) {
				case 0x00: f |= F_HALFCARRY;
					c = 0xFF;
					break;
				case 0x10: f |= F_HALFCARRY;
					c = 0x0F;
					break;
				case 0x01: f |= F_ZERO;
					c = 0x00;
					break;
				default:   c--;
					break;
				}
				break;
			case 0x0E :               // LD C, nn
				pc+=2;
				c = b2;
				break;
			case 0x0F :               // RRC A
				pc++;
				if ((a & 0x01) == 0x01) {
					f = F_CARRY;
				} else {
					f = 0;
				}
				a >>= 1;
				if ((f & F_CARRY) == F_CARRY) {
					a |= 0x80;
				}
				if (a == 0) {
					f |= F_ZERO;
				}
				break;
			case 0x10 :               // STOP
				pc+=2;

				//if (gbcFeatures) {
					if ((memory[0xFF4D] & 0x01) == 1) {
						int newKey1Reg = memory[0xFF4D] & 0xFE;
						if ((newKey1Reg & 0x80) == 0x80) {
							//setDoubleSpeedCpu(false);
							newKey1Reg &= 0x7F;
						} //else {
						//	setDoubleSpeedCpu(true);
						//	newKey1Reg |= 0x80;
						//}
						memory[0xFF4D] = (byte) newKey1Reg;
					}
				//}

				break;
			case 0x11 :               // LD DE, nnnn
				pc+=3;
				d = b3;
				e = b2;
				break;
			case 0x12 :               // LD (DE), A
				pc++;
				addressWrite((d << 8) + e, a);
				break;
			case 0x13 :               // INC DE
				pc++;
				e++;
				if (e == 0x0100) {
					d++;
					e = 0;
					if (d == 0x0100) {
						d = 0;
					}
				}
				break;
			case 0x14 :               // INC D
				pc++;
				f &= F_CARRY;
				switch (d) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					d = 0x00;
					break;
				case 0x0F: f |= F_HALFCARRY;
					d = 0x10;
					break;
				default:   d++;
					break;
				}
				break;
			case 0x15 :               // DEC D
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				switch (d) {
				case 0x00: f |= F_HALFCARRY;
					d = 0xFF;
					break;
				case 0x10: f |= F_HALFCARRY;
					d = 0x0F;
					break;
				case 0x01: f |= F_ZERO;
					d = 0x00;
					break;
				default:   d--;
					break;
				}
				break;
			case 0x16 :               // LD D, nn
				pc += 2;
				d = b2;
				break;
			case 0x17 :               // RL A
				pc++;
				if ((a & 0x80) == 0x80) {
					newf = F_CARRY;
				} else {
					newf = 0;
				}
				a <<= 1;

				if ((f & F_CARRY) == F_CARRY) {
					a |= 1;
				}

				a &= 0xFF;
				if (a == 0) {
					newf |= F_ZERO;
				}
				f = newf;
				break;
			case 0x18 :               // JR nn
				pc += 2 + offset;
				break;
			case 0x19 :               // ADD HL, DE
				pc++;
				hl = (hl + ((d << 8) + e));
				if ((hl & 0xFFFF0000) != 0) {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)) | (F_CARRY));
					hl &= 0xFFFF;
				} else {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)));
				}
				break;
			case 0x1A :               // LD A, (DE)
				pc++;
				a = JavaBoy.unsign(memory[(d << 8) + e]);
				break;
			case 0x1B :               // DEC DE
				pc++;
				e--;
				if ((e & 0xFF00) != 0) {
					e = 0xFF;
					d--;
					if ((d & 0xFF00) != 0) {
						d = 0xFF;
					}
				}
				break;
			case 0x1C :               // INC E
				pc++;
				f &= F_CARRY;
				switch (e) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					e = 0x00;
					break;
				case 0x0F: f |= F_HALFCARRY;
					e = 0x10;
					break;
				default:   e++;
					break;
				}
				break;
			case 0x1D :               // DEC E
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				switch (e) {
				case 0x00: f |= F_HALFCARRY;
					e = 0xFF;
					break;
				case 0x10: f |= F_HALFCARRY;
					e = 0x0F;
					break;
				case 0x01: f |= F_ZERO;
					e = 0x00;
					break;
				default:   e--;
					break;
				}
				break;
			case 0x1E :               // LD E, nn
				pc+=2;
				e = b2;
				break;
			case 0x1F :               // RR A
				pc++;
				if ((a & 0x01) == 0x01) {
					newf = F_CARRY;
				} else {
					newf = 0;
				}
				a >>= 1;

				if ((f & F_CARRY) == F_CARRY) {
					a |= 0x80;
				}

				if (a == 0) {
					newf |= F_ZERO;
				}
				f = newf;
				break;
			case 0x20 :               // JR NZ, nn
				if ((f & 0x80) == 0x00) {
					pc += 2 + offset;
				} else {
					pc += 2;
				}
				break;
			case 0x21 :               // LD HL, nnnn
				pc += 3;
				hl = (b3 << 8) + b2;
				break;
			case 0x22 :               // LD (HL+), A
				pc++;
				addressWrite(hl, a);
				hl = (hl + 1) & 0xFFFF;
				break;
			case 0x23 :               // INC HL
				pc++;
				hl = (hl + 1) & 0xFFFF;
				break;
			case 0x24 :               // INC H         ** May be wrong **
				pc++;
				f &= F_CARRY;
				switch ((hl & 0xFF00) >> 8) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					hl = (hl & 0x00FF);
					break;
				case 0x0F: f |= F_HALFCARRY;
					hl = (hl & 0x00FF) | 0x10;
					break;
				default:   hl = (hl + 0x0100);
					break;
				}
				break;
			case 0x25 :               // DEC H           ** May be wrong **
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				switch ((hl & 0xFF00) >> 8) {
				case 0x00: f |= F_HALFCARRY;
					hl = (hl & 0x00FF) | (0xFF00);
					break;
				case 0x10: f |= F_HALFCARRY;
					hl = (hl & 0x00FF) | (0x0F00);
					break;
				case 0x01: f |= F_ZERO;
					hl = (hl & 0x00FF);
					break;
				default:   hl = (hl & 0x00FF) | ((hl & 0xFF00) - 0x0100);
					break;
				}
				break;
			case 0x26 :               // LD H, nn
				pc+=2;
				hl = (hl & 0x00FF) | (b2 << 8);
				break;
			case 0x27 :               // DAA         ** This could be wrong! **
				pc++;

				int upperNibble = (a & 0xF0) >> 4;
				int lowerNibble = a & 0x0F;

				newf = (short) (f & F_SUBTRACT);

				if ((f & F_SUBTRACT) == 0) {

					if ((f & F_CARRY) == 0) {
						if ((upperNibble <= 8) && (lowerNibble >= 0xA) &&
						                ((f & F_HALFCARRY) == 0)) {
							a += 0x06;
						}

						if ((upperNibble <= 9) && (lowerNibble <= 0x3) &&
						                ((f & F_HALFCARRY) == F_HALFCARRY)) {
							a += 0x06;
						}

						if ((upperNibble >= 0xA) && (lowerNibble <= 0x9) &&
						                ((f & F_HALFCARRY) == 0)) {
							a += 0x60;
							newf |= F_CARRY;
						}

						if ((upperNibble >= 0x9) && (lowerNibble >= 0xA) &&
						                ((f & F_HALFCARRY) == 0)) {
							a += 0x66;
							newf |= F_CARRY;
						}

						if ((upperNibble >= 0xA) && (lowerNibble <= 0x3) &&
						                ((f & F_HALFCARRY) == F_HALFCARRY)) {
							a += 0x66;
							newf |= F_CARRY;
						}

					} else {  // If carry set

						if ((upperNibble <= 0x2) && (lowerNibble <= 0x9) &&
						                ((f & F_HALFCARRY) == 0)) {
							a += 0x60;
							newf |= F_CARRY;
						}

						if ((upperNibble <= 0x2) && (lowerNibble >= 0xA) &&
						                ((f & F_HALFCARRY) == 0)) {
							a += 0x66;
							newf |= F_CARRY;
						}

						if ((upperNibble <= 0x3) && (lowerNibble <= 0x3) &&
						                ((f & F_HALFCARRY) == F_HALFCARRY)) {
							a += 0x66;
							newf |= F_CARRY;
						}

					}

				} else { // Subtract is set

					if ((f & F_CARRY) == 0) {

						if ((upperNibble <= 0x8) && (lowerNibble >= 0x6) &&
						                ((f & F_HALFCARRY) == F_HALFCARRY)) {
							a += 0xFA;
						}

					} else { // Carry is set

						if ((upperNibble >= 0x7) && (lowerNibble <= 0x9) &&
						                ((f & F_HALFCARRY) == 0)) {
							a += 0xA0;
							newf |= F_CARRY;
						}

						if ((upperNibble >= 0x6) && (lowerNibble >= 0x6) &&
						                ((f & F_HALFCARRY) == F_HALFCARRY)) {
							a += 0x9A;
							newf |= F_CARRY;
						}

					}

				}

				a &= 0x00FF;
				if (a == 0) newf |= F_ZERO;

				f = newf;

				break;
			case 0x28 :               // JR Z, nn
				if ((f & F_ZERO) == F_ZERO) {
					pc += 2 + offset;
				} else {
					pc += 2;
				}
				break;
			case 0x29 :               // ADD HL, HL
				pc++;
				hl = (hl + hl);
				if ((hl & 0xFFFF0000) != 0) {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)) | (F_CARRY));
					hl &= 0xFFFF;
				} else {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)));
				}
				break;
			case 0x2A :               // LDI A, (HL)
				pc++;
				a = JavaBoy.unsign(memory[hl]);
				hl++;
				break;
			case 0x2B :               // DEC HL
				pc++;
				if (hl == 0) {
					hl = 0xFFFF;
				} else {
					hl--;
				}
				break;
			case 0x2C :               // INC L
				pc++;
				f &= F_CARRY;
				switch (hl & 0x00FF) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					hl = hl & 0xFF00;
					break;
				case 0x0F: f |= F_HALFCARRY;
					hl++;
					break;
				default:   hl++;
					break;
				}
				break;
			case 0x2D :               // DEC L
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				switch (hl & 0x00FF) {
				case 0x00: f |= F_HALFCARRY;
					hl = (hl & 0xFF00) | 0x00FF;
					break;
				case 0x10: f |= F_HALFCARRY;
					hl = (hl & 0xFF00) | 0x000F;
					break;
				case 0x01: f |= F_ZERO;
					hl = (hl & 0xFF00);
					break;
				default:   hl = (hl & 0xFF00) | ((hl & 0x00FF) - 1);
					break;
				}
				break;
			case 0x2E :               // LD L, nn
				pc+=2;
				hl = (hl & 0xFF00) | b2;
				break;
			case 0x2F :               // CPL A
				pc++;
				short mask = 0x80;
				a = (short) ((~a) & 0x00FF);
				f = (short) ((f & (F_CARRY | F_ZERO)) | F_SUBTRACT | F_HALFCARRY);
				break;
			case 0x30 :               // JR NC, nn
				if ((f & F_CARRY) == 0) {
					pc += 2 + offset;
				} else {
					pc += 2;
				}
				break;
			case 0x31 :               // LD SP, nnnn
				pc += 3;
				sp = (b3 << 8) + b2;
				break;
			case 0x32 :
				pc++;
				addressWrite(hl, a);  // LD (HL-), A
				hl--;
				break;
			case 0x33 :               // INC SP
				pc++;
				sp = (sp + 1) & 0xFFFF;
				break;
			case 0x34 :               // INC (HL)
				pc++;
				f &= F_CARRY;
				dat = JavaBoy.unsign(memory[hl]);
				switch (dat) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					addressWrite(hl, 0x00);
					break;
				case 0x0F: f |= F_HALFCARRY;
					addressWrite(hl, 0x10);
					break;
				default:   addressWrite(hl, dat + 1);
					break;
				}
				break;
			case 0x35 :               // DEC (HL)
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				dat = JavaBoy.unsign(memory[hl]);
				switch (dat) {
				case 0x00: f |= F_HALFCARRY;
					addressWrite(hl, 0xFF);
					break;
				case 0x10: f |= F_HALFCARRY;
					addressWrite(hl, 0x0F);
					break;
				case 0x01: f |= F_ZERO;
					addressWrite(hl, 0x00);
					break;
				default:   addressWrite(hl, dat - 1);
					break;
				}
				break;
			case 0x36 :               // LD (HL), nn
				pc += 2;
				addressWrite(hl, b2);
				break;
			case 0x37 :               // SCF
				pc++;
				f &= F_ZERO;
				f |= F_CARRY;
				break;
			case 0x38 :               // JR C, nn
				if ((f & F_CARRY) == F_CARRY) {
					pc += 2 + offset;
				} else {
					pc += 2;
				}
				break;
			case 0x39 :               // ADD HL, SP      ** Could be wrong **
				pc++;
				hl = (hl + sp);
				if ((hl & 0xFFFF0000) != 0) {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)) | (F_CARRY));
					hl &= 0xFFFF;
				} else {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)));
				}
				break;
			case 0x3A :               // LD A, (HL-)
				pc++;
				a = JavaBoy.unsign(memory[hl]);
				hl = (hl - 1) & 0xFFFF;
				break;
			case 0x3B :               // DEC SP
				pc++;
				sp = (sp - 1) & 0xFFFF;
				break;
			case 0x3C :               // INC A
				pc++;
				f &= F_CARRY;
				switch (a) {
				case 0xFF: f |= F_HALFCARRY + F_ZERO;
					a = 0x00;
					break;
				case 0x0F: f |= F_HALFCARRY;
					a = 0x10;
					break;
				default:   a++;
					break;
				}
				break;
			case 0x3D :               // DEC A
				pc++;
				f &= F_CARRY;
				f |= F_SUBTRACT;
				switch (a) {
				case 0x00: f |= F_HALFCARRY;
					a = 0xFF;
					break;
				case 0x10: f |= F_HALFCARRY;
					a = 0x0F;
					break;
				case 0x01: f |= F_ZERO;
					a = 0x00;
					break;
				default:   a--;
					break;
				}
				break;
			case 0x3E :               // LD A, nn
				pc += 2;
				a = b2;
				break;
			case 0x3F :               // CCF
				pc++;
				if ((f & F_CARRY) == 0) {
					f = (short) ((f & F_ZERO) | F_CARRY);
				} else {
					f = (short) (f & F_ZERO);
				}
				break;
			case 0x52 :               // Debug breakpoint (LD D, D)
				// As this instruction is used in games (why?) only break here if the breakpoint is on in the debugger
				//if (breakpointEnable) {
				//	terminate = true;
				//	System.out.println("- Breakpoint reached");
				//} else {
					pc++;
				//}
				break;

			case 0x76 :               // HALT
				interruptsEnabled = true;
				while (memory[0xFF0F] == 0) {
					initiateInterrupts();
					instrCount++;
				}

				pc++;
				break;
			case 0xAF :               // XOR A, A (== LD A, 0)
				pc ++;
				a = 0;
				f = 0x80;             // Set zero flag
				break;
			case 0xC0 :               // RET NZ
				if ((f & F_ZERO) == 0) {
					pc = (JavaBoy.unsign(memory[sp + 1]) << 8) + JavaBoy.unsign(memory[sp]);
					sp += 2;
				} else {
					pc++;
				}
				break;
			case 0xC1 :               // POP BC
				pc++;
				c = JavaBoy.unsign(memory[sp]);
				b = JavaBoy.unsign(memory[sp + 1]);
				sp+=2;
				break;
			case 0xC2 :               // JP NZ, nnnn
				if ((f & F_ZERO) == 0) {
					pc = (b3 << 8) + b2;
				} else {
					pc += 3;
				}
				break;
			case 0xC3 :
				pc = (b3 << 8) + b2;  // JP nnnn
				break;
			case 0xC4 :               // CALL NZ, nnnnn
				if ((f & F_ZERO) == 0) {
					pc += 3;
					sp -= 2;
					addressWrite(sp + 1, pc >> 8);
					addressWrite(sp, pc & 0x00FF);
					pc = (b3 << 8) + b2;
				} else {
					pc+=3;
				}
				break;
			case 0xC5 :               // PUSH BC
				pc++;
				sp -= 2;
				sp &= 0xFFFF;
				addressWrite(sp, c);
				addressWrite(sp + 1, b);
				break;
			case 0xC6 :               // ADD A, nn
				pc+=2;
				f = 0;

				if ((((a & 0x0F) + (b2 & 0x0F)) & 0xF0) != 0x00) {
					f |= F_HALFCARRY;
				}

				a += b2;

				if ((a & 0xFF00) != 0) {     // Perform 8-bit overflow and set zero flag
					if (a == 0x0100) {
						f |= F_ZERO + F_CARRY + F_HALFCARRY;
						a = 0;
					} else {
						f |= F_CARRY + F_HALFCARRY;
						a &= 0x00FF;
					}
				}
				break;
			case 0xCF :               // RST 08
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = 0x08;
				break;
			case 0xC8 :               // RET Z
				if ((f & F_ZERO) == F_ZERO) {
					pc = (JavaBoy.unsign(memory[sp + 1]) << 8) + JavaBoy.unsign(memory[sp]);
					sp += 2;
				} else {
					pc++;
				}
				break;
			case 0xC9 :               // RET
				pc = (JavaBoy.unsign(memory[sp + 1]) << 8) + JavaBoy.unsign(memory[sp]);
				sp += 2;
				break;
			case 0xCA :               // JP Z, nnnn
				if ((f & F_ZERO) == F_ZERO) {
					pc = (b3 << 8) + b2;
				} else {
					pc += 3;
				}
				break;
			case 0xCB :               // Shift/bit test
				pc += 2;
				int regNum = b2 & 0x07;
				int data = registerRead(regNum);
				if ((b2 & 0xC0) == 0) {
					switch ((b2 & 0xF8)) {
					case 0x00 :          // RLC A
						if ((data & 0x80) == 0x80) {
							f = F_CARRY;
						} else {
							f = 0;
						}
						data <<= 1;
						if ((f & F_CARRY) == F_CARRY) {
							data |= 1;
						}

						data &= 0xFF;
						if (data == 0) {
							f |= F_ZERO;
						}
						registerWrite(regNum, data);
						break;
					case 0x08 :          // RRC A
						if ((data & 0x01) == 0x01) {
							f = F_CARRY;
						} else {
							f = 0;
						}
						data >>= 1;
						if ((f & F_CARRY) == F_CARRY) {
							data |= 0x80;
						}
						if (data == 0) {
							f |= F_ZERO;
						}
						registerWrite(regNum, data);
						break;
					case 0x10 :          // RL r

						if ((data & 0x80) == 0x80) {
							newf = F_CARRY;
						} else {
							newf = 0;
						}
						data <<= 1;

						if ((f & F_CARRY) == F_CARRY) {
							data |= 1;
						}

						data &= 0xFF;
						if (data == 0) {
							newf |= F_ZERO;
						}
						f = newf;
						registerWrite(regNum, data);
						break;
					case 0x18 :          // RR r
						if ((data & 0x01) == 0x01) {
							newf = F_CARRY;
						} else {
							newf = 0;
						}
						data >>= 1;

						if ((f & F_CARRY) == F_CARRY) {
							data |= 0x80;
						}

						if (data == 0) {
							newf |= F_ZERO;
						}
						f = newf;
						registerWrite(regNum, data);
						break;
					case 0x20 :          // SLA r
						if ((data & 0x80) == 0x80) {
							f = F_CARRY;
						} else {
							f = 0;
						}

						data <<= 1;

						data &= 0xFF;
						if (data == 0) {
							f |= F_ZERO;
						}
						registerWrite(regNum, data);
						break;
					case 0x28 :          // SRA r
						short topBit = 0;

						topBit = (short) (data & 0x80);
						if ((data & 0x01) == 0x01) {
							f = F_CARRY;
						} else {
							f = 0;
						}

						data >>= 1;
						data |= topBit;

						if (data == 0) {
							f |= F_ZERO;
						}
						registerWrite(regNum, data);
						break;
					case 0x30 :          // SWAP r

						data = (short) (((data & 0x0F) << 4) | ((data & 0xF0) >> 4));
						if (data == 0) {
							f = F_ZERO;
						} else {
							f = 0;
						}
						registerWrite(regNum, data);
						break;
					case 0x38 :          // SRL r
						if ((data & 0x01) == 0x01) {
							f = F_CARRY;
						} else {
							f = 0;
						}

						data >>= 1;

						if (data == 0) {
							f |= F_ZERO;
						}
						registerWrite(regNum, data);
						break;
					}
				} else {

					int bitNumber = (b2 & 0x38) >> 3;

					if ((b2 & 0xC0) == 0x40)  {  // BIT n, r
						mask = (short) (0x01 << bitNumber);
						if ((data & mask) != 0) {
							f = (short) ((f & F_CARRY) | F_HALFCARRY);
						} else {
							f = (short) ((f & F_CARRY) | (F_HALFCARRY + F_ZERO));
						}
					}
					if ((b2 & 0xC0) == 0x80) {  // RES n, r
						mask = (short) (0xFF - (0x01 << bitNumber));
						data = (short) (data & mask);
						registerWrite(regNum, data);
					}
					if ((b2 & 0xC0) == 0xC0) {  // SET n, r
						mask = (short) (0x01 << bitNumber);
						data = (short) (data | mask);
						registerWrite(regNum, data);
					}

				}

				break;
			case 0xCC :               // CALL Z, nnnnn
				if ((f & F_ZERO) == F_ZERO) {
					pc += 3;
					sp -= 2;
					addressWrite(sp + 1, pc >> 8);
					addressWrite(sp, pc & 0x00FF);
					pc = (b3 << 8) + b2;
				} else {
					pc+=3;
				}
				break;
			case 0xCD :               // CALL nnnn
				pc += 3;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = (b3 << 8) + b2;
				break;
			case 0xCE :               // ADC A, nn
				pc+=2;

				if ((f & F_CARRY) != 0) {
					b2++;
				}
				f = 0;

				if ((((a & 0x0F) + (b2 & 0x0F)) & 0xF0) != 0x00) {
					f |= F_HALFCARRY;
				}

				a += b2;

				if ((a & 0xFF00) != 0) {     // Perform 8-bit overflow and set zero flag
					if (a == 0x0100) {
						f |= F_ZERO + F_CARRY + F_HALFCARRY;
						a = 0;
					} else {
						f |= F_CARRY + F_HALFCARRY;
						a &= 0x00FF;
					}
				}
				break;
			case 0xC7 :               // RST 00
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				//        terminate = true;
				pc = 0x00;
				break;
			case 0xD0 :               // RET NC
				if ((f & F_CARRY) == 0) {
					pc = (JavaBoy.unsign(memory[sp + 1]) << 8) + JavaBoy.unsign(memory[sp]);
					sp += 2;
				} else {
					pc++;
				}
				break;
			case 0xD1 :               // POP DE
				pc++;
				e = JavaBoy.unsign(memory[sp]);
				d = JavaBoy.unsign(memory[sp + 1]);
				sp+=2;
				break;
			case 0xD2 :               // JP NC, nnnn
				if ((f & F_CARRY) == 0) {
					pc = (b3 << 8) + b2;
				} else {
					pc += 3;
				}
				break;
			case 0xD4 :               // CALL NC, nnnn
				if ((f & F_CARRY) == 0) {
					pc += 3;
					sp -= 2;
					addressWrite(sp + 1, pc >> 8);
					addressWrite(sp, pc & 0x00FF);
					pc = (b3 << 8) + b2;
				} else {
					pc+=3;
				}
				break;
			case 0xD5 :               // PUSH DE
				pc++;
				sp -= 2;
				sp &= 0xFFFF;
				addressWrite(sp, e);
				addressWrite(sp + 1, d);
				break;
			case 0xD6 :               // SUB A, nn
				pc+=2;

				f = F_SUBTRACT;

				if ((((a & 0x0F) - (b2 & 0x0F)) & 0xFFF0) != 0x00) {
					f |= F_HALFCARRY;
				}

				a -= b2;

				if ((a & 0xFF00) != 0) {
					a &= 0x00FF;
					f |= F_CARRY;
				}
				if (a == 0) {
					f |= F_ZERO;
				}
				break;
			case 0xD7 :               // RST 10
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = 0x10;
				break;
			case 0xD8 :               // RET C
				if ((f & F_CARRY) == F_CARRY) {
					pc = (JavaBoy.unsign(memory[sp + 1]) << 8) + JavaBoy.unsign(memory[sp]);
					sp += 2;
				} else {
					pc++;
				}
				break;
			case 0xD9 :               // RETI
				interruptsEnabled = true;
				//inInterrupt = false;
				pc = (JavaBoy.unsign(memory[sp + 1]) << 8) + JavaBoy.unsign(memory[sp]);
				sp += 2;
				break;
			case 0xDA :               // JP C, nnnn
				if ((f & F_CARRY) == F_CARRY) {
					pc = (b3 << 8) + b2;
				} else {
					pc += 3;
				}
				break;
			case 0xDC :               // CALL C, nnnn
				if ((f & F_CARRY) == F_CARRY) {
					pc += 3;
					sp -= 2;
					addressWrite(sp + 1, pc >> 8);
					addressWrite(sp, pc & 0x00FF);
					pc = (b3 << 8) + b2;
				} else {
					pc+=3;
				}
				break;
			case 0xDE :               // SBC A, nn
				pc+=2;
				if ((f & F_CARRY) != 0) {
					b2++;
				}

				f = F_SUBTRACT;
				if ((((a & 0x0F) - (b2 & 0x0F)) & 0xFFF0) != 0x00) {
					f |= F_HALFCARRY;
				}

				a -= b2;

				if ((a & 0xFF00) != 0) {
					a &= 0x00FF;
					f |= F_CARRY;
				}

				if (a == 0) {
					f |= F_ZERO;
				}
				break;
			case 0xDF :               // RST 18
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = 0x18;
				break;
			case 0xE0 :               // LDH (FFnn), A
				pc += 2;
				addressWrite(0xFF00 + b2, a);
				break;
			case 0xE1 :               // POP HL
				pc++;
				hl = (JavaBoy.unsign(memory[sp + 1]) << 8) + JavaBoy.unsign(memory[sp]);
				sp += 2;
				break;
			case 0xE2 :               // LDH (FF00 + C), A
				pc++;
				addressWrite(0xFF00 + c, a);
				break;
			case 0xE5 :               // PUSH HL
				pc++;
				sp -= 2;
				sp &= 0xFFFF;
				addressWrite(sp + 1, hl >> 8);
				addressWrite(sp, hl & 0x00FF);
				break;
			case 0xE6 :               // AND nn
				pc+=2;
				a &= b2;
				if (a == 0) {
					f = F_ZERO;
				} else {
					f = 0;
				}
				break;
			case 0xE7 :               // RST 20
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = 0x20;
				break;
			case 0xE8 :               // ADD SP, nn
				pc+=2;
				sp = (sp + offset);
				if ((sp & 0xFFFF0000) != 0) {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)) | (F_CARRY));
					sp &= 0xFFFF;
				} else {
					f = (short) ((f & (F_SUBTRACT + F_ZERO + F_HALFCARRY)));
				}
				break;
			case 0xE9 :               // JP (HL)
				pc++;
				pc = hl;
				break;
			case 0xEA :               // LD (nnnn), A
				pc += 3;
				addressWrite((b3 << 8) + b2, a);
				break;
			case 0xEE :               // XOR A, nn
				pc+=2;
				a ^= b2;
				if (a == 0) {
					f = F_ZERO;
				} else {
					f = 0;
				}
				break;
			case 0xEF :               // RST 28
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = 0x28;
				break;
			case 0xF0 :               // LDH A, (FFnn)
				pc += 2;
				a = JavaBoy.unsign(memory[0xFF00 + b2]);
				break;
			case 0xF1 :               // POP AF
				pc++;
				f = JavaBoy.unsign(memory[sp]);
				a = JavaBoy.unsign(memory[sp + 1]);
				sp+=2;
				break;
			case 0xF2 :               // LD A, (FF00 + C)
				pc++;
				a = JavaBoy.unsign(memory[0xFF00 + c]);
				break;
			case 0xF3 :               // DI
				pc++;
				interruptsEnabled = false;
				break;
			case 0xF5 :               // PUSH AF
				pc++;
				sp -= 2;
				sp &= 0xFFFF;
				addressWrite(sp, f);
				addressWrite(sp + 1, a);
				break;
			case 0xF6 :               // OR A, nn
				pc+=2;
				a |= b2;
				if (a == 0) {
					f = F_ZERO;
				} else {
					f = 0;
				}
				break;
			case 0xF7 :               // RST 30
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = 0x30;
				break;
			case 0xF8 :               // LD HL, SP + nn  ** HALFCARRY FLAG NOT SET ***
				pc += 2;
				hl = (sp + offset);
				if ((hl & 0x10000) != 0) {
					f = F_CARRY;
					hl &= 0xFFFF;
				} else {
					f = 0;
				}
				break;
			case 0xF9 :               // LD SP, HL
				pc++;
				sp = hl;
				break;
			case 0xFA :               // LD A, (nnnn)
				pc+=3;
				a = JavaBoy.unsign(memory[(b3 << 8) + b2]);
				break;
			case 0xFB :               // EI
				pc++;
				ieDelay = 1;
				break;
			case 0xFE :               // CP nn     ** FLAGS ARE WRONG! **
				pc += 2;
				f = 0;
				if (b2 == a) {
					f |= F_ZERO;
				} else {
					if (a < b2) {
						f |= F_CARRY;
					}
				}
				break;
			case 0xFF :               // RST 38
				pc++;
				sp -= 2;
				addressWrite(sp + 1, pc >> 8);
				addressWrite(sp, pc & 0x00FF);
				pc = 0x38;
				break;

			default :

				if ((b1 & 0xC0) == 0x80) {       // Byte 0x10?????? indicates ALU op
					pc++;
					int operand = registerRead(b1 & 0x07);
					switch ((b1 & 0x38) >> 3) {
					case 1 : // ADC A, r
						if ((f & F_CARRY) != 0) {
							operand++;
						}
						// Note!  No break!
					case 0 : // ADD A, r

						f = 0;

						if ((((a & 0x0F) + (operand & 0x0F)) & 0xF0) != 0x00) {
							f |= F_HALFCARRY;
						}

						a += operand;

						if (a == 0) {
							f |= F_ZERO;
						}

						if ((a & 0xFF00) != 0) {     // Perform 8-bit overflow and set zero flag
							if (a == 0x0100) {
								f |= F_ZERO + F_CARRY + F_HALFCARRY;
								a = 0;
							} else {
								f |= F_CARRY + F_HALFCARRY;
								a &= 0x00FF;
							}
						}
						break;
					case 3 : // SBC A, r
						if ((f & F_CARRY) != 0) {
							operand++;
						}
						// Note! No break!
					case 2 : // SUB A, r

						f = F_SUBTRACT;

						if ((((a & 0x0F) - (operand & 0x0F)) & 0xFFF0) != 0x00) {
							f |= F_HALFCARRY;
						}

						a -= operand;

						if ((a & 0xFF00) != 0) {
							a &= 0x00FF;
							f |= F_CARRY;
						}
						if (a == 0) {
							f |= F_ZERO;
						}

						break;
					case 4 : // AND A, r
						a &= operand;
						if (a == 0) {
							f = F_ZERO;
						} else {
							f = 0;
						}
						break;
					case 5 : // XOR A, r
						a ^= operand;
						if (a == 0) {
							f = F_ZERO;
						} else {
							f = 0;
						}
						break;
					case 6 : // OR A, r
						a |= operand;
						if (a == 0) {
							f = F_ZERO;
						} else {
							f = 0;
						}
						break;
					case 7 : // CP A, r (compare)
						f = F_SUBTRACT;
						if (a == operand) {
							f |= F_ZERO;
						}
						if (a < operand) {
							f |= F_CARRY;
						}
						if ((a & 0x0F) < (operand & 0x0F)) {
							f |= F_HALFCARRY;
						}
						break;
					}
				} else if ((b1 & 0xC0) == 0x40) {   // Byte 0x01xxxxxxx indicates 8-bit ld

					pc++;
					registerWrite((b1 & 0x38) >> 3, registerRead(b1 & 0x07));

				} else {
					terminate = true;
					pc++;
					break;
				}
			}

			if (ieDelay != -1) {

				if (ieDelay > 0) {
					ieDelay--;
				} else {
					interruptsEnabled = true;
					ieDelay = -1;
				}

			}

			if (interruptsEnabled) {
				checkInterrupts();
			}

			initiateInterrupts();

		}
		terminate = false;
	}
}
