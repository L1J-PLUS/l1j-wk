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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.datatables.InnKeyTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1Item;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.utils.collections.Lists;

public class L1DwarfInventory extends L1Inventory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public L1DwarfInventory(L1PcInstance owner) {
		_owner = owner;
	}

	// ＤＢのcharacter_itemsの読込
	@Override
	public void loadItems() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_warehouse WHERE account_name = ?");
			pstm.setString(1, _owner.getAccountName());

			rs = pstm.executeQuery();

			while (rs.next()) {
				L1ItemInstance item = new L1ItemInstance();
				int objectId = rs.getInt("id");
				item.setId(objectId);
				L1Item itemTemplate = ItemTable.getInstance().getTemplate(
						rs.getInt("item_id"));
				item.setItem(itemTemplate);
				item.setCount(rs.getInt("count"));
				item.setEquipped(false);
				item.setEnchantLevel(rs.getInt("enchantlvl"));
				item.setIdentified(rs.getInt("is_id") != 0 ? true : false);
				item.set_durability(rs.getInt("durability"));
				item.setChargeCount(rs.getInt("charge_count"));
				item.setRemainingTime(rs.getInt("remaining_time"));
				item.setLastUsed(rs.getTimestamp("last_used"));
				item.setBless(rs.getInt("bless"));
				item.setAttrEnchantKind(rs.getInt("attr_enchant_kind"));
				item.setAttrEnchantLevel(rs.getInt("attr_enchant_level"));
				item.setFireMr(rs.getInt("firemr"));
				item.setWaterMr(rs.getInt("watermr"));
				item.setEarthMr(rs.getInt("earthmr"));
				item.setWindMr(rs.getInt("windmr"));
				item.setaddSp(rs.getInt("addsp"));
				item.setaddHp(rs.getInt("addhp"));
				item.setaddMp(rs.getInt("addmp"));
				item.setHpr(rs.getInt("hpr"));
				item.setMpr(rs.getInt("mpr"));
				item.setM_Def(rs.getInt("m_def"));
				// 登入鑰匙紀錄
				if (item.getItem().getItemId() == 40312) {
					InnKeyTable.checkey(item);
				}
				_items.add(item);
				L1World.getInstance().storeObject(item);
			}

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	// ＤＢのcharacter_warehouseへ登録
	@Override
	public void insertItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO character_warehouse SET id = ?, account_name = ?, item_id = ?, item_name = ?, count = ?, is_equipped=0, enchantlvl = ?, is_id = ?, durability = ?, charge_count = ?, remaining_time = ?, last_used = ?, bless = ?, attr_enchant_kind = ?, attr_enchant_level = ?, firemr = ?,watermr = ?,earthmr = ?,windmr = ?,addsp = ?,addhp = ?,addmp = ?,hpr = ?,mpr = ?,m_def = ?");
			pstm.setInt(1, item.getId());
			pstm.setString(2, _owner.getAccountName());
			pstm.setInt(3, item.getItemId());
			pstm.setString(4, item.getName());
			pstm.setInt(5, item.getCount());
			pstm.setInt(6, item.getEnchantLevel());
			pstm.setInt(7, item.isIdentified() ? 1 : 0);
			pstm.setInt(8, item.get_durability());
			pstm.setInt(9, item.getChargeCount());
			pstm.setInt(10, item.getRemainingTime());
			pstm.setTimestamp(11, item.getLastUsed());
			pstm.setInt(12, item.getBless());
			pstm.setInt(13, item.getAttrEnchantKind());
			pstm.setInt(14, item.getAttrEnchantLevel());
			pstm.setInt(15, item.getFireMr());
			pstm.setInt(16, item.getWaterMr());
			pstm.setInt(17, item.getEarthMr());
			pstm.setInt(18, item.getWindMr());
			pstm.setInt(19, item.getaddSp());
			pstm.setInt(20, item.getaddHp());
			pstm.setInt(21, item.getaddMp());
			pstm.setInt(22, item.getHpr());
			pstm.setInt(23, item.getMpr());
			pstm.setInt(24, item.getM_Def());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

	}

	// ＤＢのcharacter_warehouseを更新
	@Override
	public void updateItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE character_warehouse SET count = ? WHERE id = ?");
			pstm.setInt(1, item.getCount());
			pstm.setInt(2, item.getId());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	// ＤＢのcharacter_warehouseから削除
	@Override
	public void deleteItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM character_warehouse WHERE id = ?");
			pstm.setInt(1, item.getId());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

		_items.remove(_items.indexOf(item));
	}

	public static void present(String account, int itemid, int enchant,
			int count) throws Exception {

		L1Item temp = ItemTable.getInstance().getTemplate(itemid);
		if (temp == null) {
			return;
		}

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();

			if (account.compareToIgnoreCase("*") == 0) {
				pstm = con.prepareStatement("SELECT * FROM accounts");
			} else {
				pstm = con.prepareStatement("SELECT * FROM accounts WHERE login=?");
				pstm.setString(1, account);
			}
			rs = pstm.executeQuery();

			List<String> accountList = Lists.newList();
			while (rs.next()) {
				accountList.add(rs.getString("login"));
			}

			present(accountList, itemid, enchant, count);

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw e;
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

	}

	public static void present(int minlvl, int maxlvl, int itemid, int enchant,
			int count) throws Exception {

		L1Item temp = ItemTable.getInstance().getTemplate(itemid);
		if (temp == null) {
			return;
		}

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();

			pstm = con.prepareStatement("SELECT distinct(account_name) as account_name FROM characters WHERE level between ? and ?");
			pstm.setInt(1, minlvl);
			pstm.setInt(2, maxlvl);
			rs = pstm.executeQuery();

			List<String> accountList = Lists.newList();
			while (rs.next()) {
				accountList.add(rs.getString("account_name"));
			}

			present(accountList, itemid, enchant, count);

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw e;
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

	}

	private static void present(List<String> accountList, int itemid,
			int enchant, int count) throws Exception {
		L1Item itemtemp = ItemTable.getInstance().getTemplate(itemid);

		if (ItemTable.getInstance().getTemplate(itemid) == null) {
			throw new Exception("道具編號不存在。");
		} else {
			Connection con = null;
			PreparedStatement ps = null;

			try {
				con = L1DatabaseFactory.getInstance().getConnection();
				con.setAutoCommit(false);

				for (String account : accountList) {
					if (itemtemp.isStackable()) {
						L1ItemInstance item = ItemTable.getInstance().createItem(itemid);
						item.setEnchantLevel(enchant);
						item.setCount(count);

						ps = con.prepareStatement("INSERT INTO character_warehouse SET "
										+ "id = ?,"
										+ "account_name = ?,"
										+ "item_id = ?,"
										+ "item_name = ?,"
										+ "count = ?,"
										+ "is_equipped=0,"
										+ "enchantlvl = ?,"
										+ "is_id = ?,"
										+ "durability = ?,"
										+ "charge_count = ?,"
										+ "remaining_time = ?,"
										+ "last_used = ?,"
										+ "bless = ?,"
										+ "attr_enchant_kind = ?,"
										+ "attr_enchant_level = ?,"
										+ "firemr = ?,"
										+ "watermr = ?,"
										+ "earthmr = ?,"
										+ "windmr = ?,"
										+ "addsp = ?,"
										+ "addhp = ?,"
										+ "addmp = ?,"
										+ "hpr = ?,"
										+ "mpr = ?," + "m_def = ?");

						ps.setInt(1, item.getId());
						ps.setString(2, account);
						ps.setInt(3, item.getItemId());
						ps.setString(4, item.getName());
						ps.setInt(5, item.getCount());
						ps.setInt(6, item.getEnchantLevel());
						ps.setInt(7, item.isIdentified() ? 1 : 0);
						ps.setInt(8, item.get_durability());
						ps.setInt(9, item.getChargeCount());
						ps.setInt(10, item.getRemainingTime());
						ps.setTimestamp(11, item.getLastUsed());
						ps.setInt(12, item.getBless());
						ps.setInt(13, item.getAttrEnchantKind());
						ps.setInt(14, item.getAttrEnchantLevel());
						ps.setInt(15, item.getFireMr());
						ps.setInt(16, item.getWaterMr());
						ps.setInt(17, item.getEarthMr());
						ps.setInt(18, item.getWindMr());
						ps.setInt(19, item.getaddSp());
						ps.setInt(20, item.getaddHp());
						ps.setInt(21, item.getaddMp());
						ps.setInt(22, item.getHpr());
						ps.setInt(23, item.getMpr());
						ps.setInt(24, item.getM_Def());
						ps.execute();
					} else {
						L1ItemInstance item = null;
						int createCount;

						for (createCount = 0; createCount < count; createCount++) {
							item = ItemTable.getInstance().createItem(itemid);
							item.setEnchantLevel(enchant);

							ps = con.prepareStatement("INSERT INTO character_warehouse SET "
											+ "id = ?,"
											+ "account_name = ?,"
											+ "item_id = ?,"
											+ "item_name = ?,"
											+ "count = ?,"
											+ "is_equipped=0,"
											+ "enchantlvl = ?,"
											+ "is_id = ?,"
											+ "durability = ?,"
											+ "charge_count = ?,"
											+ "remaining_time = ?,"
											+ "last_used = ?,"
											+ "bless = ?,"
											+ "attr_enchant_kind = ?,"
											+ "attr_enchant_level = ?,"
											+ "firemr = ?,"
											+ "watermr = ?,"
											+ "earthmr = ?,"
											+ "windmr = ?,"
											+ "addsp = ?,"
											+ "addhp = ?,"
											+ "addmp = ?,"
											+ "hpr = ?,"
											+ "mpr = ?," 
											+ "m_def = ?");

							ps.setInt(1, item.getId());
							ps.setString(2, account);
							ps.setInt(3, item.getItemId());
							ps.setString(4, item.getName());
							ps.setInt(5, item.getCount());
							ps.setInt(6, item.getEnchantLevel());
							ps.setInt(7, item.isIdentified() ? 1 : 0);
							ps.setInt(8, item.get_durability());
							ps.setInt(9, item.getChargeCount());
							ps.setInt(10, item.getRemainingTime());
							ps.setTimestamp(11, item.getLastUsed());
							ps.setInt(12, item.getBless());
							ps.setInt(13, item.getAttrEnchantKind());
							ps.setInt(14, item.getAttrEnchantLevel());
							ps.setInt(15, item.getFireMr());
							ps.setInt(16, item.getWaterMr());
							ps.setInt(17, item.getEarthMr());
							ps.setInt(18, item.getWindMr());
							ps.setInt(19, item.getaddSp());
							ps.setInt(20, item.getaddHp());
							ps.setInt(21, item.getaddMp());
							ps.setInt(22, item.getHpr());
							ps.setInt(23, item.getMpr());
							ps.setInt(24, item.getM_Def());
							ps.execute();
						}
					}
				}

				con.commit();
				con.setAutoCommit(true);
			} catch (SQLException e) {
				try {
					con.rollback();
				} catch (SQLException ignore) {
				}

				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				throw new Exception(".present 處理時發生了例外的錯誤。");
			} finally {
				SQLUtil.close(ps);
				SQLUtil.close(con);
			}
		}
	}

	private static Logger _log = Logger.getLogger(L1DwarfInventory.class.getName());

	private final L1PcInstance _owner;
}
