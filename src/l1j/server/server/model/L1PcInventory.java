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

import java.text.DecimalFormat;
import l1j.server.server.utils.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.datatables.RaceTicketTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.identity.L1ItemId;
import l1j.server.server.serverpackets.S_AddItem;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_DeleteInventoryItem;
import l1j.server.server.serverpackets.S_ItemColor;
import l1j.server.server.serverpackets.S_ItemStatus;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_ItemAmount;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.storage.CharactersItemStorage;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1RaceTicket;

public class L1PcInventory extends L1Inventory {

	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger
			.getLogger(L1PcInventory.class.getName());

	private static final int MAX_SIZE = 5000;

	private final L1PcInstance _owner; // 所有者プレイヤー

	private int _arrowId; // 優先して使用されるアローのItemID

	private int _stingId; // 優先して使用されるスティングのItemID

	public L1PcInventory(L1PcInstance owner) {
		_owner = owner;
		_arrowId = 0;
		_stingId = 0;
	}

	public L1PcInstance getOwner() {
		return _owner;
	}

	// 分為242段的重量數值
	public int getWeight242() {
		return calcWeight242(getWeight());
	}

	// 242階段的重量數值計算
	public int calcWeight242(int weight) {
		int weight242 = 0;
		if (Config.RATE_WEIGHT_LIMIT != 0) {
			double maxWeight = _owner.getMaxWeight();
			if (weight > maxWeight) {
				weight242 = 242;
			} else {
				double wpTemp = (weight * 100 / maxWeight) * 242.00 / 100.00;
				DecimalFormat df = new DecimalFormat("00.##");
				df.format(wpTemp);
				wpTemp = Math.round(wpTemp);
				weight242 = (int) (wpTemp);
			}
		} else { // ウェイトレートが０なら重量常に０
			weight242 = 0;
		}
		return weight242;
	}

	@Override
	public int checkAddItem(L1ItemInstance item, int count) {
		return checkAddItem(item, count, true);
	}

	public int checkAddItem(L1ItemInstance item, int count, boolean message) {
		if (item == null) {
			return -1;
		}
		if (getSize() > MAX_SIZE
				|| (getSize() == MAX_SIZE && (!item.isStackable() || !checkItem(item
						.getItem().getItemId())))) { // 容量確認
			if (message) {
				sendOverMessage(263); // \f1一人のキャラクターが持って歩けるアイテムは最大180個までです。
			}
			return SIZE_OVER;
		}

		int weight = getWeight() + item.getItem().getWeight() * count / 1000
				+ 1;
		if (weight < 0 || (item.getItem().getWeight() * count / 1000) < 0) {
			if (message) {
				sendOverMessage(82); // 此物品太重了，所以你無法攜帶。
			}
			return WEIGHT_OVER;
		}
		if (calcWeight242(weight) >= 242) {
			if (message) {
				sendOverMessage(82); // 此物品太重了，所以你無法攜帶。
			}
			return WEIGHT_OVER;
		}

		L1ItemInstance itemExist = findItemId(item.getItemId());
		if (itemExist != null && (itemExist.getCount() + count) > MAX_AMOUNT) {
			if (message) {
				getOwner().sendPackets(
						new S_ServerMessage(166, "所持有的金幣",
								"超過了2,000,000,000上限。")); // \f1%0が%4%1%3%2
			}
			return AMOUNT_OVER;
		}

		return OK;
	}

	public void sendOverMessage(int message_id) {
		// 釣魚中負重訊息變更
		if (_owner.isFishing() && message_id == 82) {
			message_id = 1518; // 負重太高的狀態下無法進行釣魚。
		}
		_owner.sendPackets(new S_ServerMessage(message_id));
	}

	// 讀取資料庫中的character_items資料表
	@Override
	public void loadItems() {
		try {
			CharactersItemStorage storage = CharactersItemStorage.create();
			
			boolean weaponEquipped = false;

			for (L1ItemInstance item : storage.loadItems(_owner.getId())) {
				_items.add(item);

				if (item.isEquipped()) {
					
					// Quick patch to deal with equipping multiple weapons. Not
					// the right thing to do, but should deal with the problem
					// in the short term.
					if (item.getItem().getType2() == 1) {
						if (weaponEquipped) {
							_log.log(Level.WARNING,
								"Trying to equip extra weapon during load.");
							item.setEquipped(false);
							continue;
						} else {
							weaponEquipped = true;
						}
					}
					
					item.setEquipped(false);
					setEquipped(item, true, true, false);
				}
				if (item.getItem().getType2() == 0
						&& item.getItem().getType() == 2) { // light系アイテム
					item.setRemainingTime(item.getItem().getLightFuel());
				}
				/**
				 * 玩家身上的食人妖精RaceTicket 顯示場次、及選手編號
				 */
				if (item.getItemId() == 40309) {
					L1RaceTicket ticket = RaceTicketTable.getInstance()
							.getTemplate(item.getId());
					if (ticket != null) {
						L1Item temp = (L1Item) item.getItem().clone();
						String buf = temp.getIdentifiedNameId() + " "
								+ ticket.get_round() + "-"
								+ ticket.get_runner_num();
						temp.setName(buf);
						temp.setUnidentifiedNameId(buf);
						temp.setIdentifiedNameId(buf);
						item.setItem(temp);
					}
				}
				L1World.getInstance().storeObject(item);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// 對資料庫中的character_items資料表寫入
	@Override
	public void insertItem(L1ItemInstance item) {
		_owner.sendPackets(new S_AddItem(item));
		if (item.getItem().getWeight() != 0) {
			_owner.sendPackets(new S_PacketBox(S_PacketBox.WEIGHT,
					getWeight242()));
		}
		try {
			CharactersItemStorage storage = CharactersItemStorage.create();
			storage.storeItem(_owner.getId(), item);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public static final int COL_ALL = 0;

	public static final int COL_DURABILITY = 1;

	public static final int COL_IS_ID = 2;

	public static final int COL_ENCHANTLVL = 4;

	public static final int COL_EQUIPPED = 8;

	public static final int COL_COUNT = 16;

	public static final int COL_DELAY_EFFECT = 32;

	public static final int COL_ITEMID = 64;

	public static final int COL_CHARGE_COUNT = 128;

	public static final int COL_REMAINING_TIME = 256;

	public static final int COL_BLESS = 512;

	public static final int COL_ATTR_ENCHANT_KIND = 1024;

	public static final int COL_ATTR_ENCHANT_LEVEL = 2048;

	public static final int COL_ADDHP = 1;

	public static final int COL_ADDMP = 2;

	public static final int COL_HPR = 4;

	public static final int COL_MPR = 8;

	public static final int COL_ADDSP = 16;
	
	public static final int COL_M_DEF = 32;

	public static final int COL_EARTHMR = 64;

	public static final int COL_FIREMR = 128;

	public static final int COL_WATERMR = 256;

	public static final int COL_WINDMR = 512;

	@Override
	public void updateItem(L1ItemInstance item) {
		updateItem(item, COL_COUNT);
		if (item.getItem().isToBeSavedAtOnce()) {
			saveItem(item, COL_COUNT);
		}
	}

	/**
	 * インベントリ内のアイテムの状態を更新する。
	 * 
	 * @param item
	 *            - 更新対象のアイテム
	 * @param column
	 *            - 更新するステータスの種類
	 */
	@Override
	public void updateItem(L1ItemInstance item, int column) {
		if (column >= COL_ATTR_ENCHANT_LEVEL) { // 属性強化数
			_owner.sendPackets(new S_ItemStatus(item));
			column -= COL_ATTR_ENCHANT_LEVEL;
		}
		if (column >= COL_ATTR_ENCHANT_KIND) { // 属性強化の種類
			_owner.sendPackets(new S_ItemStatus(item));
			column -= COL_ATTR_ENCHANT_KIND;
		}
		if (column >= COL_BLESS) { // 祝福・封印
			_owner.sendPackets(new S_ItemColor(item));
			column -= COL_BLESS;
		}
		if (column >= COL_REMAINING_TIME) { // 使用可能な残り時間
			_owner.sendPackets(new S_ItemName(item));
			column -= COL_REMAINING_TIME;
		}
		if (column >= COL_CHARGE_COUNT) { // チャージ数
			_owner.sendPackets(new S_ItemName(item));
			column -= COL_CHARGE_COUNT;
		}
		if (column >= COL_ITEMID) { // 別のアイテムになる場合(便箋を開封したときなど)
			_owner.sendPackets(new S_ItemStatus(item));
			_owner.sendPackets(new S_ItemColor(item));
			_owner.sendPackets(new S_PacketBox(S_PacketBox.WEIGHT,
					getWeight242()));
			column -= COL_ITEMID;
		}
		if (column >= COL_DELAY_EFFECT) { // 効果ディレイ
			column -= COL_DELAY_EFFECT;
		}
		if (column >= COL_COUNT) { // カウント
			_owner.sendPackets(new S_ItemAmount(item));

			int weight = item.getWeight();
			if (weight != item.getLastWeight()) {
				item.setLastWeight(weight);
				_owner.sendPackets(new S_ItemStatus(item));
			} else {
				_owner.sendPackets(new S_ItemName(item));
			}
			if (item.getItem().getWeight() != 0) {
				// XXX 242段階のウェイトが変化しない場合は送らなくてよい
				_owner.sendPackets(new S_PacketBox(S_PacketBox.WEIGHT,
						getWeight242()));
			}
			column -= COL_COUNT;
		}
		if (column >= COL_EQUIPPED) { // 装備状態
			_owner.sendPackets(new S_ItemName(item));
			column -= COL_EQUIPPED;
		}
		if (column >= COL_ENCHANTLVL) { // エンチャント
			_owner.sendPackets(new S_ItemStatus(item));
			column -= COL_ENCHANTLVL;
		}
		if (column >= COL_IS_ID) { // 確認状態
			_owner.sendPackets(new S_ItemStatus(item));
			_owner.sendPackets(new S_ItemColor(item));
			column -= COL_IS_ID;
		}
		if (column >= COL_DURABILITY) { // 耐久性
			_owner.sendPackets(new S_ItemStatus(item));
			column -= COL_DURABILITY;
		}
	}

	/**
	 * インベントリ内のアイテムの状態をDBに保存する。
	 * 
	 * @param item
	 *            - 更新対象のアイテム
	 * @param column
	 *            - 更新するステータスの種類
	 */
	public void saveItem(L1ItemInstance item, int column) {
		if (column == 0) {
			return;
		}

		try {
			CharactersItemStorage storage = CharactersItemStorage.create();
			if (column >= COL_ATTR_ENCHANT_LEVEL) { // 属性強化数
				storage.updateItemAttrEnchantLevel(item);
				column -= COL_ATTR_ENCHANT_LEVEL;
			}
			if (column >= COL_ATTR_ENCHANT_KIND) { // 属性強化の種類
				storage.updateItemAttrEnchantKind(item);
				column -= COL_ATTR_ENCHANT_KIND;
			}
			if (column >= COL_BLESS) { // 祝福・封印
				storage.updateItemBless(item);
				column -= COL_BLESS;
			}
			if (column >= COL_REMAINING_TIME) { // 使用可能な残り時間
				storage.updateItemRemainingTime(item);
				column -= COL_REMAINING_TIME;
			}
			if (column >= COL_CHARGE_COUNT) { // チャージ数
				storage.updateItemChargeCount(item);
				column -= COL_CHARGE_COUNT;
			}
			if (column >= COL_ITEMID) { // 別のアイテムになる場合(便箋を開封したときなど)
				storage.updateItemId(item);
				column -= COL_ITEMID;
			}
			if (column >= COL_DELAY_EFFECT) { // 効果ディレイ
				storage.updateItemDelayEffect(item);
				column -= COL_DELAY_EFFECT;
			}
			if (column >= COL_COUNT) { // カウント
				storage.updateItemCount(item);
				column -= COL_COUNT;
			}
			if (column >= COL_EQUIPPED) { // 装備状態
				storage.updateItemEquipped(item);
				column -= COL_EQUIPPED;
			}
			if (column >= COL_ENCHANTLVL) { // エンチャント
				storage.updateItemEnchantLevel(item);
				column -= COL_ENCHANTLVL;
			}
			if (column >= COL_IS_ID) { // 確認状態
				storage.updateItemIdentified(item);
				column -= COL_IS_ID;
			}
			if (column >= COL_DURABILITY) { // 耐久性
				storage.updateItemDurability(item);
				column -= COL_DURABILITY;
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public void saveEnchantAccessory(L1ItemInstance item, int column) { // 飾品強化
		if (column == 0) {
			return;
		}

		try {
			CharactersItemStorage storage = CharactersItemStorage.create();
			if (column >= COL_WINDMR) {
				storage.updateWindMr(item);
				column -= COL_WINDMR;
			}
			if (column >= COL_WATERMR) {
				storage.updateWaterMr(item);
				column -= COL_WATERMR;
			}
			if (column >= COL_FIREMR) {
				storage.updateFireMr(item);
				column -= COL_FIREMR;
			}
			if (column >= COL_EARTHMR) {
				storage.updateEarthMr(item);
				column -= COL_EARTHMR;
			}
			if (column >= COL_M_DEF) {
				storage.updateM_Def(item);
				column -= COL_M_DEF;
			}
			if (column >= COL_ADDSP) {
				storage.updateaddSp(item);
				column -= COL_ADDSP;
			}
			if (column >= COL_MPR) {
				storage.updateMpr(item);
				column -= COL_MPR;
			}
			if (column >= COL_HPR) {
				storage.updateHpr(item);
				column -= COL_HPR;
			}
			if (column >= COL_ADDMP) {
				storage.updateaddMp(item);
				column -= COL_ADDMP;
			}
			if (column >= COL_ADDHP) {
				storage.updateaddHp(item);
				column -= COL_ADDHP;
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// ＤＢのcharacter_itemsから削除
	@Override
	public void deleteItem(L1ItemInstance item) {
		try {
			CharactersItemStorage storage = CharactersItemStorage.create();

			storage.deleteItem(item);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		if (item.isEquipped()) {
			setEquipped(item, false);
		}
		_owner.sendPackets(new S_DeleteInventoryItem(item));
		_items.remove(item);
		if (item.getItem().getWeight() != 0) {
			_owner.sendPackets(new S_PacketBox(S_PacketBox.WEIGHT,
					getWeight242()));
		}
	}

	// アイテムを装着脱着させる（L1ItemInstanceの変更、補正値の設定、character_itemsの更新、パケット送信まで管理）
	public void setEquipped(L1ItemInstance item, boolean equipped) {
		setEquipped(item, equipped, false, false);
	}

	public void setEquipped(L1ItemInstance item, boolean equipped,
			boolean loaded, boolean changeWeapon) {
		if (item.isEquipped() != equipped) { // 設定値と違う場合だけ処理
			L1Item temp = item.getItem();
			if (equipped) { // 装着
				item.setEquipped(true);
				_owner.getEquipSlot().set(item);
			} else { // 脱着
				if (!loaded) {
					// インビジビリティクローク バルログブラッディクローク装備中でインビジ状態の場合はインビジ状態の解除
					if (temp.getItemId() == 20077 || temp.getItemId() == 20062
							|| temp.getItemId() == 120077) {
						if (_owner.isInvisble()) {
							_owner.delInvis();
							return;
						}
					}
				}
				item.setEquipped(false);
				_owner.getEquipSlot().remove(item);
			}
			if (!loaded) { // 最初の読込時はＤＢパケット関連の処理はしない
				// XXX:意味のないセッター
				_owner.setCurrentHp(_owner.getCurrentHp());
				_owner.setCurrentMp(_owner.getCurrentMp());
				updateItem(item, COL_EQUIPPED);
				_owner.sendPackets(new S_OwnCharStatus(_owner));
				if (temp.getType2() == 1 && changeWeapon == false) { // 武器の場合はビジュアル更新。ただし、武器の持ち替えで武器を脱着する時は更新しない
					_owner.sendPackets(new S_CharVisualUpdate(_owner));
					_owner.broadcastPacket(new S_CharVisualUpdate(_owner));
				}
				// _owner.getNetConnection().saveCharToDisk(_owner); //
				// DBにキャラクター情報を書き込む
			}
		}
	}

	// 特定のアイテムを装備しているか確認
	public boolean checkEquipped(int id) {
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getItemId() == id && item.isEquipped()) {
				return true;
			}
		}
		return false;
	}

	// 特定のアイテムを全て装備しているか確認（セットボーナスがあるやつの確認用）
	public boolean checkEquipped(int[] ids) {
		for (int id : ids) {
			if (!checkEquipped(id)) {
				return false;
			}
		}
		return true;
	}

	// 特定のタイプのアイテムを装備している数
	public int getTypeEquipped(int type2, int type) {
		int equipeCount = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getType2() == type2
					&& item.getItem().getType() == type && item.isEquipped()) {
				equipeCount++;
			}
		}
		return equipeCount;
	}

	// 装備している特定のタイプのアイテム
	public L1ItemInstance getItemEquipped(int type2, int type) {
		L1ItemInstance equipeitem = null;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getType2() == type2
					&& item.getItem().getType() == type && item.isEquipped()) {
				equipeitem = item;
				break;
			}
		}
		return equipeitem;
	}

	// 装備しているリング
	public L1ItemInstance[] getRingEquipped() {
		L1ItemInstance equipeItem[] = new L1ItemInstance[2];
		int equipeCount = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getType2() == 2 && item.getItem().getType() == 9
					&& item.isEquipped()) {
				equipeItem[equipeCount] = item;
				equipeCount++;
				if (equipeCount == 2) {
					break;
				}
			}
		}
		return equipeItem;
	}

	// 変身時に装備できない装備を外す
	public void takeoffEquip(int polyid) {
		takeoffWeapon(polyid);
		takeoffArmor(polyid);
	}

	// 変身時に装備できない武器を外す
	private void takeoffWeapon(int polyid) {
		if (_owner.getWeapon() == null) { // 素手
			return;
		}

		boolean takeoff = false;
		int weapon_type = _owner.getWeapon().getItem().getType();
		// 装備出来ない武器を装備してるか？
		takeoff = !L1PolyMorph.isEquipableWeapon(polyid, weapon_type);

		if (takeoff) {
			setEquipped(_owner.getWeapon(), false, false, false);
		}
	}

	// 変身時に装備できない防具を外す
	private void takeoffArmor(int polyid) {
		L1ItemInstance armor = null;

		// ヘルムからガーダーまでチェックする
		for (int type = 0; type <= 13; type++) {
			// 装備していて、装備不可の場合は外す
			if (getTypeEquipped(2, type) != 0
					&& !L1PolyMorph.isEquipableArmor(polyid, type)) {
				if (type == 9) { // リングの場合は、両手分外す
					armor = getItemEquipped(2, type);
					if (armor != null) {
						setEquipped(armor, false, false, false);
					}
					armor = getItemEquipped(2, type);
					if (armor != null) {
						setEquipped(armor, false, false, false);
					}
				} else {
					armor = getItemEquipped(2, type);
					if (armor != null) {
						setEquipped(armor, false, false, false);
					}
				}
			}
		}
	}

	// 使用するアローの取得
	public L1ItemInstance getArrow() {
		return getBullet(0);
	}

	// 使用するスティングの取得
	public L1ItemInstance getSting() {
		return getBullet(15);
	}

	private L1ItemInstance getBullet(int type) {
		L1ItemInstance bullet;
		int priorityId = 0;
		if (type == 0) {
			priorityId = _arrowId; // アロー
		}
		if (type == 15) {
			priorityId = _stingId; // スティング
		}
		if (priorityId > 0) // 優先する弾があるか
		{
			bullet = findItemId(priorityId);
			if (bullet != null) {
				return bullet;
			} else // なくなっていた場合は優先を消す
			{
				if (type == 0) {
					_arrowId = 0;
				}
				if (type == 15) {
					_stingId = 0;
				}
			}
		}

		for (Object itemObject : _items) // 弾を探す
		{
			bullet = (L1ItemInstance) itemObject;
			if (bullet.getItem().getType() == type
					&& bullet.getItem().getType2() == 0) {
				if (type == 0) {
					_arrowId = bullet.getItem().getItemId(); // 優先にしておく
				}
				if (type == 15) {
					_stingId = bullet.getItem().getItemId(); // 優先にしておく
				}
				return bullet;
			}
		}
		return null;
	}

	// 優先するアローの設定
	public void setArrow(int id) {
		_arrowId = id;
	}

	// 優先するスティングの設定
	public void setSting(int id) {
		_stingId = id;
	}

	// 装備によるＨＰ自然回復補正
	public int hpRegenPerTick() {
		int hpr = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.isEquipped()) {
				hpr += item.getItem().get_addhpr() + item.getHpr();
			}
		}
		return hpr;
	}

	// 装備によるＭＰ自然回復補正
	public int mpRegenPerTick() {
		int mpr = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.isEquipped()) {
				mpr += item.getItem().get_addmpr() + item.getMpr();
			}
		}
		return mpr;
	}

	public L1ItemInstance CaoPenalty() {
		int rnd = Random.nextInt(_items.size());
		L1ItemInstance penaltyItem = _items.get(rnd);
		if (penaltyItem.getItem().getItemId() == L1ItemId.ADENA // アデナ、トレード不可のアイテムは落とさない
				|| !penaltyItem.getItem().isTradable()) {
			return null;
		}
		Object[] petlist = _owner.getPetList().values().toArray();
		for (Object petObject : petlist) {
			if (petObject instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) petObject;
				if (penaltyItem.getId() == pet.getItemObjId()) {
					return null;
				}
			}
		}
		setEquipped(penaltyItem, false);
		return penaltyItem;
	}
}
