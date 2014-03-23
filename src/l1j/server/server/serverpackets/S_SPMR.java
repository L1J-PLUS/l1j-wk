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

import static l1j.server.server.model.skill.L1SkillId.STATUS_WISDOM_POTION;
import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1PcInstance;

public class S_SPMR extends ServerBasePacket {

	private static final String S_SPMR = "[S] S_S_SPMR";

	private byte[] _byte = null;

	/** 更新魔防以及魔攻 */
	public S_SPMR(L1PcInstance pc) {
		buildPacket(pc);
	}

	private void buildPacket(L1PcInstance pc) {
		writeC(Opcodes.S_OPCODE_SPMR);
		// ウィズダムポーションのSPはS_SkillBrave送信時に更新されるため差し引いておく
		if (pc.hasSkillEffect(STATUS_WISDOM_POTION)) {
			writeC(pc.getSp() - pc.getTrueSp() - 2); // 装備増加したSP
		}
		else {
			writeC(pc.getSp() - pc.getTrueSp()); // 装備増加したSP
		}
		writeH(pc.getTrueMr() - pc.getBaseMr()); // 装備や魔法で増加したMR
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
		return S_SPMR;
	}
}
