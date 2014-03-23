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
package l1j.server.server.model;

import java.util.List;

import l1j.server.Config;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_TradeAddItem;
import l1j.server.server.serverpackets.S_TradeStatus;
import l1j.server.server.utils.LogRecorder;

// Referenced classes of package l1j.server.server.model:
// L1Trade

public class L1Trade {
	private static L1Trade _instance;

	public L1Trade() {
	}

	public static L1Trade getInstance() {
		if (_instance == null) {
			_instance = new L1Trade();
		}
		return _instance;
	}

	/**
    * 加入交易物品
    * @param player
    *          自己
    * @param itemid
    *          欲交易出去的物品
    * @param itemcount
    *          物品數量
    */
	public void TradeAddItem(L1PcInstance player, int itemid, int itemcount) {
		//未在交易狀態中
		if (player.getTradeID() == 0) {
			return;
		}
	      
		// 交易對象
		L1PcInstance trading_partner = (L1PcInstance) L1World.getInstance()
				.findObject(player.getTradeID());
		if (trading_partner == null) {
			TradeCancel(player);
	        return;
		}
		
		L1ItemInstance l1iteminstance = player.getInventory().getItem(itemid);
		if (l1iteminstance == null) {
	        return;
		}
		// 裝備中, 不可交易
		if (l1iteminstance.isEquipped()) {
			return;
	    }
		
		itemcount = Math.abs(itemcount); // 確保物品數量為正
		itemcount = Math.min(itemcount, l1iteminstance.getCount()); // 確保交易的物品小於身上所有

		if (itemcount < 0 
			|| itemcount > 2000000000 
			|| l1iteminstance.getCount() < 0 
			|| l1iteminstance.getCount() < itemcount) {
			return;
		}
		
		if ((l1iteminstance.getCount() < itemcount) || (0 > itemcount)) {
			player.sendPackets(new S_TradeStatus(1));
			trading_partner.sendPackets(new S_TradeStatus(1));
			player.setTradeOk(false);
			trading_partner.setTradeOk(false);
			player.setTradeID(0);
			trading_partner.setTradeID(0);
			return;
		}
		player.getInventory().tradeItem(l1iteminstance, itemcount,
				player.getTradeWindowInventory());
		player.sendPackets(new S_TradeAddItem(l1iteminstance,
				itemcount, 0));
		trading_partner.sendPackets(new S_TradeAddItem(l1iteminstance,
				itemcount, 1));
	}

	/**
    * 交易完成
    * @param player
    */
	public void TradeOK(L1PcInstance player) {
		//未在交易狀態中
	    if (player.getTradeID() == 0) {
	    	return;
	    }	      
		
		// 交易對象
		L1PcInstance trading_partner = (L1PcInstance) L1World.getInstance().findObject(player.getTradeID());
		if (trading_partner == null) {
	         TradeCancel(player);
	         return;
		}

		List<L1ItemInstance> player_tradelist = player.getTradeWindowInventory().getItems();
		int player_tradecount = player.getTradeWindowInventory().getSize();

		List<L1ItemInstance> trading_partner_tradelist = trading_partner.getTradeWindowInventory().getItems();
		int trading_partner_tradecount = trading_partner.getTradeWindowInventory().getSize();

		for (int cnt = 0; cnt < player_tradecount; cnt++) {
			L1ItemInstance l1iteminstance1 = player_tradelist.get(0);
			player.getTradeWindowInventory().tradeItem(l1iteminstance1,
					l1iteminstance1.getCount(),
					trading_partner.getInventory());
			// 交易紀錄
			if (Config.writeTradeLog) {
				LogRecorder.writeTradeLog(player, trading_partner,
						l1iteminstance1);
			}
		}
		for (int cnt = 0; cnt < trading_partner_tradecount; cnt++) {
			L1ItemInstance l1iteminstance2 = trading_partner_tradelist.get(0);
			trading_partner.getTradeWindowInventory().tradeItem(
					l1iteminstance2, l1iteminstance2.getCount(),
					player.getInventory());
			// 交易紀錄
			if (Config.writeTradeLog) {
				LogRecorder.writeTradeLog(trading_partner, player,
						l1iteminstance2);
			}
		}

		player.sendPackets(new S_TradeStatus(0));
		trading_partner.sendPackets(new S_TradeStatus(0));
		player.setTradeOk(false);
		trading_partner.setTradeOk(false);
		player.setTradeID(0);
		trading_partner.setTradeID(0);
		player.turnOnOffLight();
		trading_partner.turnOnOffLight();		
	}

	/**
    * 取消交易
    * @param player
    */
	public void TradeCancel(L1PcInstance player) {
		//未在交易狀態中
		if (player.getTradeID() == 0) {
			return;
		}
	      
		// 交易對象
		L1PcInstance trading_partner = (L1PcInstance) L1World.getInstance().findObject(player.getTradeID());
		
		//自己的欲交易的物品, 放回自己的包包
		List<L1ItemInstance> player_tradelist = player.getTradeWindowInventory().getItems();
		int player_tradecount = player.getTradeWindowInventory().getSize();

		for (int cnt = 0; cnt < player_tradecount; cnt++) {
			L1ItemInstance l1iteminstance1 = player_tradelist.get(0);
			player.getTradeWindowInventory().tradeItem(l1iteminstance1,
					l1iteminstance1.getCount(), player.getInventory());
		}
		player.sendPackets(new S_TradeStatus(1));
		player.setTradeOk(false);
		player.setTradeID(0);


		//交易對象的欲交易的物品, 放回他自己的包包
	    if (trading_partner != null) {
	    	List<L1ItemInstance> trading_partner_tradelist = trading_partner.getTradeWindowInventory().getItems();
	    	int trading_partner_tradecount = trading_partner.getTradeWindowInventory().getSize();
	         
	    	for (int cnt = 0; cnt < trading_partner_tradecount; cnt++) {
	    		L1ItemInstance l1iteminstance2 = trading_partner_tradelist.get(0);
	    		trading_partner.getTradeWindowInventory().tradeItem(
	    				l1iteminstance2, l1iteminstance2.getCount(),
	    				trading_partner.getInventory());
	    	}
	   
	        trading_partner.sendPackets(new S_TradeStatus(1));
	        trading_partner.setTradeOk(false);
	        trading_partner.setTradeID(0);
	    }
	}
}
