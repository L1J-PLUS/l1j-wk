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
import l1j.server.server.model.Instance.L1DollInstance;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket , S_DollPack

public class S_DollPack extends ServerBasePacket {

	private static final String S_DOLLPACK = "[S] S_DollPack";

	private byte[] _byte = null;

	public S_DollPack(L1DollInstance doll) {
		/*
		 * int addbyte = 0; int addbyte1 = 1; int addbyte2 = 13; int setting =
		 * 4;
		 */
		writeC(Opcodes.S_OPCODE_CHARPACK);
		writeH(doll.getX());
		writeH(doll.getY());
		writeD(doll.getId());
		writeH(doll.getGfxId()); // SpriteID in List.spr
		writeC(doll.getStatus()); // Modes in List.spr
		writeC(doll.getHeading());
		writeC(0); // (Bright) - 0~15
		writeC(doll.getMoveSpeed()); // 1段加速狀態
		writeD(0);
		writeH(0);
		writeS(doll.getNameId());
		writeS(doll.getTitle());
		writeC((doll.getBraveSpeed() * 16)); // 狀態、2段加速狀態
		writeD(0); // ??
		writeS(null); // ??
		writeS(doll.getMaster() != null ? doll.getMaster().getName() : "");
		writeC(0); // ??
		writeC(0xFF);
		writeC(0);
		writeC(doll.getLevel()); // PC = 0, Mon = Lv
		writeC(0);
		writeC(0xFF);
		writeC(0xFF);
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		}

		return _byte;
	}

	@Override
	public String getType() {
		return S_DOLLPACK;
	}

}
