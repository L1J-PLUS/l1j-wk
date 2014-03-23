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
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1PcInstance;

public class S_CharVisualUpdate extends ServerBasePacket {
	private static final String _S__0B_S_CharVisualUpdate = "[C] S_CharVisualUpdate";

	public S_CharVisualUpdate(L1PcInstance pc) {
		writeC(Opcodes.S_OPCODE_CHARVISUALUPDATE);
		writeD(pc.getId());
		writeC(pc.getCurrentWeapon());
		writeC(0xff);
		writeC(0xff);
	}

	public S_CharVisualUpdate(L1Character cha, int status) {
		writeC(Opcodes.S_OPCODE_CHARVISUALUPDATE);
		writeD(cha.getId());
		writeC(status);
		writeC(0xff);
		writeC(0xff);
	}

	@Override
	public byte[] getContent() {
		return getBytes();
	}

	@Override
	public String getType() {
		return _S__0B_S_CharVisualUpdate;
	}
}
