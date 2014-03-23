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

import java.util.logging.Logger;

import l1j.server.server.Account;
import l1j.server.server.ActionCodes;
import l1j.server.server.ClientThread;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.identity.L1ItemId;
import l1j.server.server.serverpackets.S_AttackPacket;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.log.LogPickUpItem;

/**
 * 處理收到由客戶端傳來撿起道具的封包
 */
public class C_PickUpItem extends ClientBasePacket {

	private static final String C_PICK_UP_ITEM = "[C] C_PickUpItem";
	private static Logger _log = Logger.getLogger(C_PickUpItem.class.getName());

	public C_PickUpItem(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);
		
		L1PcInstance pc = client.getActiveChar();
		if ((pc == null) || pc.isDead() || pc.isGhost()) {
			return;
		}
		
		int x = readH();
		int y = readH();
		int objectId = readD();
		int pickupCount = readD();
		
		//additional dupe checks.  Thanks Mike
		if (pc.getOnlineStatus() != 1) {
			Account.ban(pc.getAccountName());
			IpTable.getInstance().banIp(pc.getNetConnection().getIp());
			_log.info(pc.getName() + " Attempted Dupe Exploit (C_PickUpItem).");
			L1World.getInstance().broadcastServerMessage("Player " + pc.getName() + " Attempted A Dupe exploit!");
			pc.sendPackets(new S_Disconnect());
			return;
		}
		//TRICIDTODO: Set configurable auto ban
		if (pickupCount < 0)
		{
			Account.ban(pc.getAccountName());
			IpTable.getInstance().banIp(pc.getNetConnection().getIp());
			_log.info(pc.getName() + " Attempted Dupe Exploit (C_PickUpItem).");
			L1World.getInstance().broadcastServerMessage("Player " + pc.getName() + " Attempted A Dupe exploit!");
			pc.sendPackets(new S_Disconnect());
			return;
		}
		if (objectId == pc.getId()) {
			return;
		}

		if (pc.isInvisble()) { // 隱身狀態
			return;
		}
		if (pc.isInvisDelay()) { // 還在解除隱身的延遲
			return;
		}

		L1Inventory groundInventory = L1World.getInstance().getInventory(x, y, pc.getMapId());
		L1Object object = groundInventory.getItem(objectId);

		if ((object != null) && !pc.isDead()) {
			L1ItemInstance item = (L1ItemInstance) object;
			if ((!item.isStackable() && pickupCount != 1) || item.getCount() <= 0 || pickupCount <= 0 || pickupCount > 2000000000 || pickupCount > item.getCount()) {
				Account.ban(pc.getAccountName());
				IpTable.getInstance().banIp(pc.getNetConnection().getIp());
				_log.info(pc.getName() + " Attempted Dupe Exploit (C_PickUpItem).");
				L1World.getInstance().broadcastServerMessage("Player " + pc.getName() + " Attempted A Dupe exploit!");
				pc.sendPackets(new S_Disconnect());
				return;
			}
			if (objectId != item.getId()) {
				_log.warning(pc.getName() + " had item " +
						Integer.toString(objectId) + " not match.");
			}
			if ((item.getItemOwnerId() != 0) && (pc.getId() != item.getItemOwnerId())) {
				pc.sendPackets(new S_ServerMessage(623)); // アイテムが拾えませんでした。
				return;
			}
			if (pc.getLocation().getTileLineDistance(item.getLocation()) > 3) {
				return;
			}

			if (item.getItem().getItemId() == L1ItemId.ADENA) {
				L1ItemInstance inventoryItem = pc.getInventory().findItemId(L1ItemId.ADENA);
				int inventoryItemCount = 0;
				if (inventoryItem != null) {
					inventoryItemCount = inventoryItem.getCount();
				}
				// 超過20億
				if ((long) inventoryItemCount + (long) pickupCount > 2000000000L) {
					pc.sendPackets(new S_ServerMessage(166, // \f1%0が%4%1%3%2
							"你身上的金幣已經超過", "2,000,000,000了，所以不能撿取金幣。"));
					return;
				}
			}

			if (pc.getInventory().checkAddItem( // 檢查容量與重量
					item, pickupCount) == L1Inventory.OK) {
				if ((item.getX() != 0) && (item.getY() != 0)) { // ワールドマップ上のアイテム
					L1ItemInstance pcitem = pc.getInventory().getItem(objectId);
					int before_inven = 0;
					if (item.isStackable()) {
						before_inven = pc.getInventory().countItems(item.getItem().getItemId());
					} else {
						if (pcitem != null) {
							before_inven = pcitem.getCount();
						}
					}
			int brfore_ground = groundInventory.getItem(objectId).getCount();
					groundInventory.tradeItem(item, pickupCount, pc.getInventory());
					pc.turnOnOffLight();
					int after_inven = 0;
					if (item.isStackable()) {
						after_inven = pc.getInventory().countItems(item.getItem().getItemId());
					} else {
						after_inven = pc.getInventory().getItem(objectId).getCount();
					}
					L1ItemInstance gditem = groundInventory.getItem(objectId);
					int after_ground = 0;
					if (gditem != null) {
						after_ground = groundInventory.getItem(objectId).getCount();
					}
					LogPickUpItem lpui = new LogPickUpItem();
					lpui.storeLogPickUpItem(pc, item, before_inven, after_inven, brfore_ground, after_ground, pickupCount);
					S_AttackPacket s_attackPacket = new S_AttackPacket(pc, objectId, ActionCodes.ACTION_Pickup);
					pc.sendPackets(s_attackPacket);
					if (!pc.isGmInvis()) {
						pc.broadcastPacket(s_attackPacket);
					}
				}
			}
		}
	}

	@Override
	public String getType() {
		return C_PICK_UP_ITEM;
	}
}
