/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package l1j.server.server.serverpackets;

import l1j.server.server.Opcodes;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_Door extends ServerBasePacket {
	private static final String S_DOOR = "[S] S_Door";
	private byte[] _byte = null;

	private static final int PASS = 0;
	private static final int NOT_PASS = 1;

	public S_Door(int x, int y, int direction, boolean isPassable) {
		buildPacket(x, y, direction, isPassable);
	}

	private void buildPacket(int x, int y, int direction, boolean isPassable) {
		writeC(Opcodes.S_OPCODE_ATTRIBUTE);
		writeH(x);
		writeH(y);
		writeC(direction); // ドアの方向 0: ／ 1: ＼
		writeC(isPassable ? PASS : NOT_PASS);
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	@Override
	public String getType() {
		return S_DOOR;
	}
}
