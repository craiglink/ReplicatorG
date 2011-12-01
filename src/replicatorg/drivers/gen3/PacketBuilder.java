package replicatorg.drivers.gen3;

import replicatorg.app.tools.IButtonCrc;

public class PacketBuilder implements PacketConstants {
	final static int MAX_PACKET_LENGTH = 256;

	/**
	 * A class for building a new packet to send down the wire to the
	 * Sanguino3G.
	 */
	byte[] data = new byte[MAX_PACKET_LENGTH];

	// current end of packet. Bytes 0 and 1 are reserved for start byte
	// and packet payload length.
	int idx = 2;

	IButtonCrc crc = new IButtonCrc();

	/**
	 * Start building a new command packet.
	 * 
	 * @param target
	 *            the target identifier for this packet.
	 * @param command
	 *            the command identifier for this packet.
	 */
	PacketBuilder(int command) {
		idx = 2;
		data[0] = START_BYTE;
		// data[1] = length; // just to avoid confusion
		add8((byte)command);
	}

	/**
	 * Add an 8-bit value to the end of the packet payload.
	 * 
	 * @param v
	 *            the value to append.
	 */
	void add8(byte v) {
		data[idx++] = v;
		crc.update(v);
	}

	void add8(int v) {
		assert((0xFFFFFF00 & v) == 0);
		add8((byte) (v & 0xff));
	}

	/**
	 * Add a 16-bit value to the end of the packet payload.
	 * 
	 * @param v
	 *            the value to append.
	 */
	void add16(short v) {
		add8((byte) (v & 0xff));
		add8((byte) ((v >> 8) & 0xff));
	}

	void add16(int v) {
		assert((0xFFFF0000 & v) == 0);
		add16((short)(v & 0xffff));
	}

	/**
	 * Add a 32-bit value to the end of the packet payload.
	 * 
	 * @param v
	 *            the value to append. Must be long to support unsigned ints.
	 */
	void add32(int v) {
		add16((short) (v & 0xffff));
		add16((short) ((v >> 16) & 0xffff));
	}

	void add32(long v) {
		assert((0xFFFFFFFF00000000L & v) == 0);
		add32((int) (v & 0xffffffff));
	}

	/**
	 * Complete the packet.
	 * 
	 * @return a byte array representing the completed packet.
	 */
	byte[] getPacket() {
		data[idx] = crc.getCrc();
		data[1] = (byte) (idx - 2); // len does not count packet header
		byte[] rv = new byte[idx + 1];
		System.arraycopy(data, 0, rv, 0, idx + 1);
		return rv;
	}

}
