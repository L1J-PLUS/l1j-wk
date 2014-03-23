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
import l1j.server.server.model.Instance.L1ItemInstance;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_DropItem extends ServerBasePacket {

	private static final String _S__OB_DropItem = "[S] S_DropItem";

	private byte[] _byte = null;

	public S_DropItem(L1ItemInstance item) {
		buildPacket(item);
	}

	private void buildPacket(L1ItemInstance item) {
		// int addbyte = 0;
		// int addbyte1 = 1;
		// int addbyte2 = 13;
		// int setting = 4;

		String itemName = item.getItem().getUnidentifiedNameId();
		// 已鑑定
		int isId = item.isIdentified() ? 1 : 0;
		if (isId == 1) {
			itemName = item.getItem().getIdentifiedNameId();
		}
		writeC(Opcodes.S_OPCODE_DROPITEM);
		writeH(item.getX());
		writeH(item.getY());
		writeD(item.getId());
		writeH(item.getItem().getGroundGfxId());
		writeC(0);
		writeC(0);
		if (item.isNowLighting()) {
			writeC(item.getItem().getLightRange());
		}
		else {
			writeC(0);
		}
		writeC(0);
		writeD(item.getCount());
		writeC(0);
		writeC(0);
		if (item.getCount() > 1) {
			if (item.getItem().getItemId() == 40312 && item.getKeyId() != 0) { // 旅館鑰匙
				writeS(itemName + item.getInnKeyName() + " (" + item.getCount() + ")");
			} else {
				writeS(itemName + " (" + item.getCount() + ")");
			}
		}
		else {
			int itemId = item.getItem().getItemId();
			if ((itemId == 20383) && (isId == 1)) { // 軍馬頭盔
				writeS(itemName + " [" + item.getChargeCount() + "]");
			}
			else if (item.getChargeCount() != 0 && (isId == 1)) { // 可使用的次數
				writeS(itemName + " (" + item.getChargeCount() + ")");
			}
			else if ((item.getItem().getLightRange() != 0) && item.isNowLighting()) { // 燈具
				writeS(itemName + " ($10)");
			}
			else if (item.getItem().getItemId() == 40312 && item.getKeyId() != 0) { // 旅館鑰匙
				writeS(itemName + item.getInnKeyName());
			}
			else {
				writeS(itemName);
			}
		}
		writeC(0);
		writeD(0);
		writeD(0);
		writeC(255);
		writeC(0);
		writeC(0);
		writeC(0);
		writeH(65535);
		// writeD(0x401799a);
		writeD(0);
		writeC(8);
		writeC(0);
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
		return _S__OB_DropItem;
	}

}
