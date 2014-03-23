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
package l1j.server.server.clientpackets;

import l1j.server.server.ClientThread;
import l1j.server.server.datatables.PetItemTable;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.serverpackets.S_PetEquipment;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1PetItem;
import l1j.server.server.templates.L1PetType;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來使用寵物道具的封包
 */
public class C_UsePetItem extends ClientBasePacket {

	/**
	 * 【Client】 id:60 size:8 time:1302335819781
	 * 0000	3c 00 04 bd 54 00 00 00                            <...T...
	 * 
	 * 【Server】 id:82 size:16 time:1302335819812
	 * 0000	52 25 00 04 bd 54 00 00 0a 37 80 08 7e ec d0 46    R%...T...7..~..F
	*/

	private static final String C_USE_PET_ITEM = "[C] C_UsePetItem";

	public C_UsePetItem(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);
		
		L1PcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}

		int data = readC();
		int petId = readD();
		int listNo = readC();

		L1PetInstance pet = (L1PetInstance) L1World.getInstance().findObject(petId);
		if (pet == null)  {
			return;
		}
		L1ItemInstance item = pet.getInventory().getItems().get(listNo);
		if (item == null) {
			return;
		}

		if ((item.getItem().getType2() == 0)
				&& (item.getItem().getType() == 11)) { // 寵物道具
			L1PetType petType = PetTypeTable.getInstance().get(pet.getNpcTemplate().get_npcId());
			// 判斷是否可用寵物裝備
			if (!petType.canUseEquipment()) {
				pc.sendPackets(new S_ServerMessage(74, item.getLogName()));
				return;
			}
			int itemId = item.getItem().getItemId();
			L1PetItem petItem = PetItemTable.getInstance().getTemplate(itemId);
			if (petItem.getUseType() == 1) { // 牙齒
				pet.usePetWeapon(pet, item);
				pc.sendPackets(new S_PetEquipment(data, pet, listNo)); // 裝備時更新寵物資訊
			} else if (petItem.getUseType() == 0) { // 盔甲
				pet.usePetArmor(pet, item);
				pc.sendPackets(new S_PetEquipment(data, pet, listNo)); // 裝備時更新寵物資訊
			} else {
				pc.sendPackets(new S_ServerMessage(74, item.getLogName()));
			}
		} else {
			pc.sendPackets(new S_ServerMessage(74, item.getLogName()));
		}
	}

	@Override
	public String getType() {
		return C_USE_PET_ITEM;
	}
}
