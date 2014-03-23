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
package l1j.server.server.model.Instance;

import static l1j.server.server.model.skill.L1SkillId.BLIND_HIDING;
import static l1j.server.server.model.skill.L1SkillId.CANCELLATION;
import static l1j.server.server.model.skill.L1SkillId.COOKING_WONDER_DRUG;
import static l1j.server.server.model.skill.L1SkillId.COUNTER_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.DECREASE_WEIGHT;
import static l1j.server.server.model.skill.L1SkillId.DRESS_EVASION;
import static l1j.server.server.model.skill.L1SkillId.EFFECT_POTION_OF_BATTLE;
import static l1j.server.server.model.skill.L1SkillId.ENTANGLE;
import static l1j.server.server.model.skill.L1SkillId.FOG_OF_SLEEPING;
import static l1j.server.server.model.skill.L1SkillId.GMSTATUS_FINDINVIS;
import static l1j.server.server.model.skill.L1SkillId.GMSTATUS_HPBAR;
import static l1j.server.server.model.skill.L1SkillId.GREATER_HASTE;
import static l1j.server.server.model.skill.L1SkillId.HASTE;
import static l1j.server.server.model.skill.L1SkillId.ILLUSION_AVATAR;
import static l1j.server.server.model.skill.L1SkillId.INVISIBILITY;
import static l1j.server.server.model.skill.L1SkillId.MASS_SLOW;
import static l1j.server.server.model.skill.L1SkillId.MORTAL_BODY;
import static l1j.server.server.model.skill.L1SkillId.PHANTASM;
import static l1j.server.server.model.skill.L1SkillId.SHAPE_CHANGE;
import static l1j.server.server.model.skill.L1SkillId.SLOW;
import static l1j.server.server.model.skill.L1SkillId.SOLID_CARRIAGE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CHAT_PROHIBITED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HASTE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_RIBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_THIRD_SPEED;
import static l1j.server.server.model.skill.L1SkillId.STRIKER_GALE;
import static l1j.server.server.model.skill.L1SkillId.WIND_SHACKLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;


import l1j.server.Config;
import l1j.server.Config_Einhasad;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.ActionCodes;
import l1j.server.server.ClientThread;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.PacketOutput;
import l1j.server.server.WarTimeController;
import l1j.server.server.command.executor.L1HpBar;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.HpRegeneration;
import l1j.server.server.model.HpRegenerationByDoll;
import l1j.server.server.model.ItemMakeByDoll;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1ChatParty;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1DwarfForElfInventory;
import l1j.server.server.model.L1DwarfInventory;
import l1j.server.server.model.L1EquipmentSlot;
import l1j.server.server.model.L1ExcludingList;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Karma;
import l1j.server.server.model.L1Magic;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Party;
import l1j.server.server.model.L1PartyRefresh;
import l1j.server.server.model.L1PcDeleteTimer;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PinkName;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1TownLocation;
import l1j.server.server.model.L1Trade;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.MpReductionByAwake;
import l1j.server.server.model.MpRegeneration;
import l1j.server.server.model.MpRegenerationByDoll;
import l1j.server.server.model.classes.L1ClassFeature;
import l1j.server.server.model.gametime.L1GameTimeCarrier;
import l1j.server.server.model.monitor.L1PcAutoUpdate;
import l1j.server.server.model.monitor.L1PcExpMonitor;
import l1j.server.server.model.monitor.L1PcGhostMonitor;
import l1j.server.server.model.monitor.L1PcHellMonitor;
import l1j.server.server.model.monitor.L1PcInvisDelay;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_BlueMessage;
import l1j.server.server.serverpackets.S_CastleMaster;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_DoActionShop;
import l1j.server.server.serverpackets.S_Exp;
import l1j.server.server.serverpackets.S_Fight;
import l1j.server.server.serverpackets.S_Fishing;
import l1j.server.server.serverpackets.S_HPMeter;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_Lawful;
import l1j.server.server.serverpackets.S_Liquor;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Poison;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_bonusstats;
import l1j.server.server.serverpackets.ServerBasePacket;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1MagicDoll;
import l1j.server.server.templates.L1PrivateShopBuyList;
import l1j.server.server.templates.L1PrivateShopSellList;
import l1j.server.server.utils.CalcStat;
import l1j.server.server.utils.Random;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.utils.collections.Lists;
import l1j.server.server.model.TheCryOfSurvival;
import l1j.server.server.serverpackets.S_SkillIconEinhasad;

// Referenced classes of package l1j.server.server.model:
// L1Character, L1DropTable, L1Object, L1ItemInstance,
// L1World
//

public class L1PcInstance extends L1Character {
	private static final long serialVersionUID = 1L;

	public static final int CLASSID_KNIGHT_MALE = 61;

	public static final int CLASSID_KNIGHT_FEMALE = 48;

	public static final int CLASSID_ELF_MALE = 138;

	public static final int CLASSID_ELF_FEMALE = 37;

	public static final int CLASSID_WIZARD_MALE = 734;

	public static final int CLASSID_WIZARD_FEMALE = 1186;

	public static final int CLASSID_DARK_ELF_MALE = 2786;

	public static final int CLASSID_DARK_ELF_FEMALE = 2796;

	public static final int CLASSID_PRINCE = 0;

	public static final int CLASSID_PRINCESS = 1;

	public static final int CLASSID_DRAGON_KNIGHT_MALE = 6658;

	public static final int CLASSID_DRAGON_KNIGHT_FEMALE = 6661;

	public static final int CLASSID_ILLUSIONIST_MALE = 6671;

	public static final int CLASSID_ILLUSIONIST_FEMALE = 6650;

	private short _hpr = 0;

	private short _trueHpr = 0;

	/** 3.3C組隊系統 */
	boolean _rpActive = false;

	private L1PartyRefresh _rp;

	private int _partyType;

	public short getHpr() {
		return (short) _hpr ;
	}

	public void addHpr(int i) {
		_trueHpr += i;
		_hpr = (short) Math.max(0, _trueHpr);
	}

	private short _mpr = 0;

	private short _trueMpr = 0;

	public short getMpr() {
		return (short) _mpr;
	}

	public void addMpr(int i) {
		_trueMpr += i;
		_mpr = (short) Math.max(0, _trueMpr);
	}

	public short _originalHpr = 0; // ● オリジナルCON HPR

	public short getOriginalHpr() {

		return _originalHpr;
	}

	public short _originalMpr = 0; // ● オリジナルWIS MPR

	public short getOriginalMpr() {

		return _originalMpr;
	}

	public void startHpRegeneration() {
		final int INTERVAL = 1000;

		if (!_hpRegenActive) {
			_hpRegen = new HpRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_hpRegen, INTERVAL, INTERVAL);
			_hpRegenActive = true;
		}
	}

	public void stopHpRegeneration() {
		if (_hpRegenActive) {
			_hpRegen.cancel();
			_hpRegen = null;
			_hpRegenActive = false;
		}
	}

	public void startMpRegeneration() {
		final int INTERVAL = 1000;

		if (!_mpRegenActive) {
			_mpRegen = new MpRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_mpRegen, INTERVAL, INTERVAL);
			_mpRegenActive = true;
		}
	}

	public void stopMpRegeneration() {
		if (_mpRegenActive) {
			_mpRegen.cancel();
			_mpRegen = null;
			_mpRegenActive = false;
		}
	}

	// 獲得道具
	public void startItemMakeByDoll() {
		final int INTERVAL_BY_DOLL = 240000;
		boolean isExistItemMakeDoll = false;
		if (L1MagicDoll.isItemMake(this)) {
			isExistItemMakeDoll = true;
		}
		if (!_ItemMakeActiveByDoll && isExistItemMakeDoll) {
			_itemMakeByDoll = new ItemMakeByDoll(this);
			_regenTimer.scheduleAtFixedRate(_itemMakeByDoll, INTERVAL_BY_DOLL,
					INTERVAL_BY_DOLL);
			_ItemMakeActiveByDoll = true;
		}
	}

	// 獲得道具停止
	public void stopItemMakeByDoll() {
		if (_ItemMakeActiveByDoll) {
			_itemMakeByDoll.cancel();
			_itemMakeByDoll = null;
			_ItemMakeActiveByDoll = false;
		}
	}

	// 回血開始
	public void startHpRegenerationByDoll() {
		final int INTERVAL_BY_DOLL = 64000;
		boolean isExistHprDoll = false;
		if (L1MagicDoll.isHpRegeneration(this)) {
			isExistHprDoll = true;
		}
		if (!_hpRegenActiveByDoll && isExistHprDoll) {
			_hpRegenByDoll = new HpRegenerationByDoll(this);
			_regenTimer.scheduleAtFixedRate(_hpRegenByDoll, INTERVAL_BY_DOLL,
					INTERVAL_BY_DOLL);
			_hpRegenActiveByDoll = true;
		}
	}

	// 回血停止
	public void stopHpRegenerationByDoll() {
		if (_hpRegenActiveByDoll) {
			_hpRegenByDoll.cancel();
			_hpRegenByDoll = null;
			_hpRegenActiveByDoll = false;
		}
	}

	// 回魔開始
	public void startMpRegenerationByDoll() {
		final int INTERVAL_BY_DOLL = 64000;
		boolean isExistMprDoll = false;
		if (L1MagicDoll.isMpRegeneration(this)) {
			isExistMprDoll = true;
		}
		if (!_mpRegenActiveByDoll && isExistMprDoll) {
			_mpRegenByDoll = new MpRegenerationByDoll(this);
			_regenTimer.scheduleAtFixedRate(_mpRegenByDoll, INTERVAL_BY_DOLL, INTERVAL_BY_DOLL);
			_mpRegenActiveByDoll = true;
		}
	}

	// 回魔停止
	public void stopMpRegenerationByDoll() {
		if (_mpRegenActiveByDoll) {
			_mpRegenByDoll.cancel();
			_mpRegenByDoll = null;
			_mpRegenActiveByDoll = false;
		}
	}

	public void startMpReductionByAwake() {
		final int INTERVAL_BY_AWAKE = 4000;
		if (!_mpReductionActiveByAwake) {
			_mpReductionByAwake = new MpReductionByAwake(this);
			_regenTimer.scheduleAtFixedRate(_mpReductionByAwake, INTERVAL_BY_AWAKE, INTERVAL_BY_AWAKE);
			_mpReductionActiveByAwake = true;
		}
	}

	public void stopMpReductionByAwake() {
		if (_mpReductionActiveByAwake) {
			_mpReductionByAwake.cancel();
			_mpReductionByAwake = null;
			_mpReductionActiveByAwake = false;
		}
	}

	public void startObjectAutoUpdate() {
		removeAllKnownObjects();
		_autoUpdateFuture = GeneralThreadPool.getInstance().pcScheduleAtFixedRate(new L1PcAutoUpdate(getId()), 0L, INTERVAL_AUTO_UPDATE);
	}

	/**
	 * 各種モニタータスクを停止する。
	 */
	public void stopEtcMonitor() {
		if (_autoUpdateFuture != null) {
			_autoUpdateFuture.cancel(true);
			_autoUpdateFuture = null;
		}
		if (_expMonitorFuture != null) {
			_expMonitorFuture.cancel(true);
			_expMonitorFuture = null;
		}
		if (_ghostFuture != null) {
			_ghostFuture.cancel(true);
			_ghostFuture = null;
		}

		if (_hellFuture != null) {
			_hellFuture.cancel(true);
			_hellFuture = null;
		}

	}

	private static final long INTERVAL_AUTO_UPDATE = 300;

	private ScheduledFuture<?> _autoUpdateFuture;

	private static final long INTERVAL_EXP_MONITOR = 500;

	private ScheduledFuture<?> _expMonitorFuture;

	public void onChangeExp() {
		int level = ExpTable.getLevelByExp(getExp());
		int char_level = getLevel();
		int gap = level - char_level;
		if (gap == 0) {
			// sendPackets(new S_OwnCharStatus(this));
			sendPackets(new S_Exp(this));
			return;
		}

		// レベルが変化した場合
		if (gap > 0) {
			levelUp(gap);
		}
		else if (gap < 0) {
			levelDown(gap);
		}
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		// 判斷旅館內是否使用相同鑰匙
		if (perceivedFrom.getMapId() >= 16384 && perceivedFrom.getMapId() <= 25088 // 旅館內判斷
				&& perceivedFrom.getInnKeyId() != getInnKeyId()) {
			return;
		}
		if (isGmInvis() || isGhost()) {
			return;
		}
		if (isInvisble() && !perceivedFrom.hasSkillEffect(GMSTATUS_FINDINVIS)) {
			return;
		}

		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_OtherCharPacks(this, perceivedFrom.hasSkillEffect(GMSTATUS_FINDINVIS))); // 自分の情報を送る
		if (isInParty() && getParty().isMember(perceivedFrom)) { // PTメンバーならHPメーターも送る
			perceivedFrom.sendPackets(new S_HPMeter(this));
		}

		if (isPrivateShop()) { // 開個人商店中
			perceivedFrom.sendPackets(new S_DoActionShop(getId(), ActionCodes.ACTION_Shop, getShopChat()));
		} else if (isFishing()) { // 釣魚中
			perceivedFrom.sendPackets(new S_Fishing(getId(), ActionCodes.ACTION_Fishing, getFishX(), getFishY()));
		}

		if (isCrown()) { // 君主
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			if (clan != null) {
				if ((getId() == clan.getLeaderId() // 血盟主で城主クラン
						)
						&& (clan.getCastleId() != 0)) {
					perceivedFrom.sendPackets(new S_CastleMaster(clan.getCastleId(), getId()));
				}
			}
		}
	}

	// 範囲外になった認識済みオブジェクトを除去
	private void removeOutOfRangeObjects() {
		for (L1Object known : getKnownObjects()) {
			if (known == null) {
				continue;
			}

			if (Config.PC_RECOGNIZE_RANGE == -1) {
				if (!getLocation().isInScreen(known.getLocation())) { // 画面外
					removeKnownObject(known);
					sendPackets(new S_RemoveObject(known));
				}
			}
			else {
				if (getLocation().getTileLineDistance(known.getLocation()) > Config.PC_RECOGNIZE_RANGE) {
					removeKnownObject(known);
					sendPackets(new S_RemoveObject(known));
				}
			}
		}
	}

	// 更新範圍內的物件
	public void updateObject() {
		removeOutOfRangeObjects();

		if (getMapId() <= 10000) {
			for (L1Object visible : L1World.getInstance().getVisibleObjects(this, Config.PC_RECOGNIZE_RANGE)) {
				if (!knownsObject(visible)) {
					visible.onPerceive(this);
				}
				else {
					if (visible instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) visible;
						if (getLocation().isInScreen(npc.getLocation()) && (npc.getHiddenStatus() != 0)) {
							npc.approachPlayer(this);
						}
					}
				}
				if (hasSkillEffect(GMSTATUS_HPBAR) && L1HpBar.isHpBarTarget(visible)) {
					sendPackets(new S_HPMeter((L1Character) visible));
				}
			}
		} else { // 旅館內判斷
			for (L1Object visible : L1World.getInstance().getVisiblePlayer(this)) {
				if (!knownsObject(visible)) {
					visible.onPerceive(this);
				}
				if (hasSkillEffect(GMSTATUS_HPBAR) && L1HpBar.isHpBarTarget(visible)) {
					if (getInnKeyId() == ((L1Character) visible).getInnKeyId()) {
						sendPackets(new S_HPMeter((L1Character) visible));
					}
				}
			}
		}
	}

	private void sendVisualEffect() {
		int poisonId = 0;
		if (getPoison() != null) { // 毒状態
			poisonId = getPoison().getEffectId();
		}
		if (getParalysis() != null) { // 麻痺状態
			// 麻痺エフェクトを優先して送りたい為、poisonIdを上書き。
			poisonId = getParalysis().getEffectId();
		}
		if (poisonId != 0) { // このifはいらないかもしれない
			sendPackets(new S_Poison(getId(), poisonId));
			broadcastPacket(new S_Poison(getId(), poisonId));
		}
	}

	public void sendVisualEffectAtLogin() {
		// S_Emblemの送信はC_Clanに移行
		// for (L1Clan clan : L1World.getInstance().getAllClans()) {
		// sendPackets(new S_Emblem(clan.getClanId()));
		// }

		if (getClanid() != 0) { // クラン所属
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			if (clan != null) {
				if (isCrown() && (getId() == clan.getLeaderId()) && // プリンスまたはプリンセス、かつ、血盟主で自クランが城主
						(clan.getCastleId() != 0)) {
					sendPackets(new S_CastleMaster(clan.getCastleId(), getId()));
				}
			}
		}

		sendVisualEffect();
	}

	public void sendVisualEffectAtTeleport() {
		if (isDrink()) { // liquorで酔っている
			sendPackets(new S_Liquor(getId(), 1));
		}

		sendVisualEffect();
	}

	private List<Integer> skillList = Lists.newList();

	public void setSkillMastery(int skillid) {
		if (!skillList.contains(skillid)) {
			skillList.add(skillid);
		}
	}

	public void removeSkillMastery(int skillid) {
		if (skillList.contains(skillid)) {
			skillList.remove((Object) skillid);
		}
	}

	public boolean isSkillMastery(int skillid) {
		return skillList.contains(skillid);
	}

	public void clearSkillMastery() {
		skillList.clear();
	}

	// 寵物競速
	private int _lap = 1;

	public void setLap(int i) {
		_lap = i;
	}

	public int getLap() {
		return _lap;
	}

	private int _lapCheck = 0;

	public void setLapCheck(int i) {
		_lapCheck = i;
	}

	public int getLapCheck() {
		return _lapCheck;
	}

	/**
	 * 只是將總圈數的完程度數量化
	 */
	public int getLapScore() {
		return _lap * 29 + _lapCheck;
	}

	// 補充
	private boolean _order_list = false;

	public boolean isInOrderList() {
		return _order_list;
	}

	public void setInOrderList(boolean bool) {
		_order_list = bool;
	}

	public L1PcInstance() {
		_accessLevel = 0;
		_currentWeapon = 0;
		_inventory = new L1PcInventory(this);
		_dwarf = new L1DwarfInventory(this);
		_dwarfForElf = new L1DwarfForElfInventory(this);
		_tradewindow = new L1Inventory();
		_bookmarks = Lists.newList();
		_quest = new L1Quest(this);
		_equipSlot = new L1EquipmentSlot(this); // コンストラクタでthisポインタを渡すのは安全だろうか・・・
	}

	@Override
	public void setCurrentHp(int i) {
		if (getCurrentHp() == i) {
			return;
		}
		int currentHp = i;
		if (currentHp >= getMaxHp()) {
			currentHp = getMaxHp();
		}
		setCurrentHpDirect(currentHp);
		sendPackets(new S_HPUpdate(currentHp, getMaxHp()));
		if (isInParty()) { // パーティー中
			getParty().updateMiniHP(this);
		}
	}

	@Override
	public void setCurrentMp(int i) {
		if (getCurrentMp() == i) {
			return;
		}
		int currentMp = i;
		if ((currentMp >= getMaxMp()) || isGm()) {
			currentMp = getMaxMp();
		}
		setCurrentMpDirect(currentMp);
		sendPackets(new S_MPUpdate(currentMp, getMaxMp()));
	}

	@Override
	public L1PcInventory getInventory() {
		return _inventory;
	}

	public L1DwarfInventory getDwarfInventory() {
		return _dwarf;
	}

	public L1DwarfForElfInventory getDwarfForElfInventory() {
		return _dwarfForElf;
	}

	public L1Inventory getTradeWindowInventory() {
		return _tradewindow;
	}

	public boolean isGmInvis() {
		return _gmInvis;
	}

	public void setGmInvis(boolean flag) {
		_gmInvis = flag;
	}

	public int getCurrentWeapon() {
		return _currentWeapon;
	}

	public void setCurrentWeapon(int i) {
		_currentWeapon = i;
	}

	public int getType() {
		return _type;
	}

	public void setType(int i) {
		_type = i;
	}

	public short getAccessLevel() {
		return _accessLevel;
	}

	public void setAccessLevel(short i) {
		_accessLevel = i;
	}

	public int getClassId() {
		return _classId;
	}

	public void setClassId(int i) {
		_classId = i;
		_classFeature = L1ClassFeature.newClassFeature(i);
	}

	private L1ClassFeature _classFeature = null;

	public L1ClassFeature getClassFeature() {
		return _classFeature;
	}

	@Override
	public synchronized int getExp() {
		return _exp;
	}

	@Override
	public synchronized void setExp(int i) {
		_exp = i;
	}

	private int _PKcount; // ● PKカウント

	public int get_PKcount() {
		return _PKcount;
	}

	public void set_PKcount(int i) {
		_PKcount = i;
	}

	private int _PkCountForElf; // ● PKカウント(エルフ用)

	public int getPkCountForElf() {
		return _PkCountForElf;
	}

	public void setPkCountForElf(int i) {
		_PkCountForElf = i;
	}

	private int _clanid; // ● クランＩＤ

	public int getClanid() {
		return _clanid;
	}

	public void setClanid(int i) {
		_clanid = i;
	}

	private String clanname; // ● クラン名

	public String getClanname() {
		return clanname;
	}

	public void setClanname(String s) {
		clanname = s;
	}

	// 参照を持つようにしたほうがいいかもしれない
	public L1Clan getClan() {
		return L1World.getInstance().getClan(getClanname());
	}

	private int _clanRank; // ● クラン内のランク(血盟君主、ガーディアン、一般、見習い)

	public int getClanRank() {
		return _clanRank;
	}

	public void setClanRank(int i) {
		_clanRank = i;
	}

	// 角色生日
	private Timestamp _birthday;

	public Timestamp getBirthday() {	
		return _birthday;
	}

	public int getSimpleBirthday(){
		if (_birthday !=null){
			SimpleDateFormat SimpleDate = new SimpleDateFormat("yyyyMMdd");
			int BornTime = Integer.parseInt(SimpleDate.format(_birthday.getTime()));
			return BornTime;
		}
		else {
			return 0;
		}
	}	

	public void setBirthday(Timestamp time) {
		_birthday = time;
	}

	public void setBirthday(){	
		_birthday = new Timestamp(System.currentTimeMillis());	
	}

	private byte _sex; // ● 性別

	public byte get_sex() {
		return _sex;
	}

	public void set_sex(int i) {
		_sex = (byte) i;
	}

	public boolean isGm() {
		return _gm;
	}

	public void setGm(boolean flag) {
		_gm = flag;
	}

	public boolean isMonitor() {
		return _monitor;
	}

	public void setMonitor(boolean flag) {
		_monitor = flag;
	}

	private L1PcInstance getStat() {
		return null;
	}

	public void reduceCurrentHp(double d, L1Character l1character) {
		getStat().reduceCurrentHp(d, l1character);
	}

	/**
	 * 指定されたプレイヤー群にログアウトしたことを通知する
	 * 
	 * @param playersList
	 *            通知するプレイヤーの配列
	 */
	private void notifyPlayersLogout(List<L1PcInstance> playersArray) {
		for (L1PcInstance player : playersArray) {
			if (player.knownsObject(this)) {
				player.removeKnownObject(this);
				player.sendPackets(new S_RemoveObject(this));
			}
		}
	}

	public void logout() {
		L1World world = L1World.getInstance();
		if (getClanid() != 0) // クラン所属
		{
			L1Clan clan = world.getClan(getClanname());
			if (clan != null) {
				if (clan.getWarehouseUsingChar() == getId()) // 自キャラがクラン倉庫使用中
				{
					clan.setWarehouseUsingChar(0); // クラン倉庫のロックを解除
				}
			}
		}
		notifyPlayersLogout(getKnownPlayers());
		world.removeVisibleObject(this);
		world.removeObject(this);
		notifyPlayersLogout(world.getRecognizePlayer(this));
		_inventory.clearItems();
		_dwarf.clearItems();
		removeAllKnownObjects();
		stopHpRegeneration();
		stopMpRegeneration();
        stopCryOfSurvival();
        setCryTime(0);
		setDead(true); // 使い方おかしいかもしれないけど、ＮＰＣに消滅したことをわからせるため
		setNetConnection(null);
		setPacketOutput(null);
	}

	public ClientThread getNetConnection() {
		return _netConnection;
	}

	public void setNetConnection(ClientThread clientthread) {
		_netConnection = clientthread;
	}

	public boolean isInParty() {
		return getParty() != null;
	}

	public L1Party getParty() {
		return _party;
	}

	public void setParty(L1Party p) {
		_party = p;
	}

	public boolean isInChatParty() {
		return getChatParty() != null;
	}

	public L1ChatParty getChatParty() {
		return _chatParty;
	}

	public void setChatParty(L1ChatParty cp) {
		_chatParty = cp;
	}

	public int getPartyID() {
		return _partyID;
	}

	public void setPartyID(int partyID) {
		_partyID = partyID;
	}

	public int getTradeID() {
		return _tradeID;
	}

	public void setTradeID(int tradeID) {
		_tradeID = tradeID;
	}

	public void setTradeOk(boolean tradeOk) {
		_tradeOk = tradeOk;
	}

	public boolean getTradeOk() {
		return _tradeOk;
	}

	public int getTempID() {
		return _tempID;
	}

	public void setTempID(int tempID) {
		_tempID = tempID;
	}

	public boolean isTeleport() {
		return _isTeleport;
	}

	public void setTeleport(boolean flag) {
		_isTeleport = flag;
	}

	public boolean isDrink() {
		return _isDrink;
	}

	public void setDrink(boolean flag) {
		_isDrink = flag;
	}

	public boolean isGres() {
		return _isGres;
	}

	public void setGres(boolean flag) {
		_isGres = flag;
	}

	public boolean isPinkName() {
		return _isPinkName;
	}

	public void setPinkName(boolean flag) {
		_isPinkName = flag;
	}

	private List<L1PrivateShopSellList> _sellList = Lists.newList();

	public List<L1PrivateShopSellList> getSellList() {
		return _sellList;
	}

	private List<L1PrivateShopBuyList> _buyList = Lists.newList();

	public List<L1PrivateShopBuyList> getBuyList() {
		return _buyList;
	}

	private byte[] _shopChat;

	public void setShopChat(byte[] chat) {
		_shopChat = chat;
	}

	public byte[] getShopChat() {
		return _shopChat;
	}

	private boolean _isPrivateShop = false;

	public boolean isPrivateShop() {
		return _isPrivateShop;
	}

	public void setPrivateShop(boolean flag) {
		_isPrivateShop = flag;
	}

	private boolean _isTradingInPrivateShop = false;

	public boolean isTradingInPrivateShop() {
		return _isTradingInPrivateShop;
	}

	public void setTradingInPrivateShop(boolean flag) {
		_isTradingInPrivateShop = flag;
	}

	private int _partnersPrivateShopItemCount = 0; // 閲覧中の個人商店のアイテム数

	public int getPartnersPrivateShopItemCount() {
		return _partnersPrivateShopItemCount;
	}

	public void setPartnersPrivateShopItemCount(int i) {
		_partnersPrivateShopItemCount = i;
	}

	private PacketOutput _out;

	public void setPacketOutput(PacketOutput out) {
		_out = out;
	}

	public void sendPackets(ServerBasePacket serverbasepacket) {
		if (_out == null) {
			return;
		}

		try {
			_out.sendPacket(serverbasepacket);
		}
		catch (Exception e) {}
	}

	@Override
	public void onAction(L1PcInstance attacker) {
		onAction(attacker, 0);
	}

	@Override
	public void onAction(L1PcInstance attacker, int skillId) {
		// XXX:NullPointerException回避。onActionの引数の型はL1Characterのほうが良い？
		if (attacker == null) {
			return;
		}
		// テレポート処理中
		if (isTeleport()) {
			return;
		}
		// 攻撃される側または攻撃する側がセーフティーゾーン
		if ((getZoneType() == 1) || (attacker.getZoneType() == 1)) {
			// 攻撃モーション送信
			L1Attack attack_mortion = new L1Attack(attacker, this, skillId);
			attack_mortion.action();
			return;
		}

		if (checkNonPvP(this, attacker) == true) {
			// 攻撃モーション送信
			L1Attack attack_mortion = new L1Attack(attacker, this, skillId);
			attack_mortion.action();
			return;
		}

		if ((getCurrentHp() > 0) && !isDead()) {
			attacker.delInvis();

			L1Attack attack = new L1Attack(attacker, this);

			if (hasSkillEffect(COUNTER_BARRIER)) {
				L1Magic magic = new L1Magic(this, attacker);
				if (magic.calcProbabilityMagic(COUNTER_BARRIER) &&
						attack.isShortDistance() && !attacker.isFoeSlayer()) {
					attack.actionCounterBarrier();
					attack.commitCounterBarrier();
					return;
				}
			}
			if (attack.calcHit()) {
				attacker.setPetTarget(this);
				attack.calcDamage();
				attack.calcStaffOfMana();
				attack.addPcPoisonAttack(attacker, this);
				attack.addChaserAttack();
			}
			attack.action();
			attack.commit();
		}
	}

	public boolean checkNonPvP(L1PcInstance pc, L1Character target) {
		L1PcInstance targetpc = null;
		if (target instanceof L1PcInstance) {
			targetpc = (L1PcInstance) target;
		}
		else if (target instanceof L1PetInstance) {
			targetpc = (L1PcInstance) ((L1PetInstance) target).getMaster();
		}
		else if (target instanceof L1SummonInstance) {
			targetpc = (L1PcInstance) ((L1SummonInstance) target).getMaster();
		}
		if (targetpc == null) {
			return false; // 相手がPC、サモン、ペット以外
		}
		if (!Config.ALT_NONPVP) { // Non-PvP設定
			if (getMap().isCombatZone(getLocation())) {
				return false;
			}

			// 全戦争リストを取得
			for (L1War war : L1World.getInstance().getWarList()) {
				if ((pc.getClanid() != 0) && (targetpc.getClanid() != 0)) { // 共にクラン所属中
					boolean same_war = war.CheckClanInSameWar(pc.getClanname(), targetpc.getClanname());
					if (same_war == true) { // 同じ戦争に参加中
						return false;
					}
				}
			}
			// Non-PvP設定でも戦争中は布告なしで攻撃可能
			if (target instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) target;
				if (isInWarAreaAndWarTime(pc, targetPc)) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	private boolean isInWarAreaAndWarTime(L1PcInstance pc, L1PcInstance target) {
		// pcとtargetが戦争中に戦争エリアに居るか
		int castleId = L1CastleLocation.getCastleIdByArea(pc);
		int targetCastleId = L1CastleLocation.getCastleIdByArea(target);
		if ((castleId != 0) && (targetCastleId != 0) && (castleId == targetCastleId)) {
			if (WarTimeController.getInstance().isNowWar(castleId)) {
				return true;
			}
		}
		return false;
	}

	public void setPetTarget(L1Character target) {
		Object[] petList = getPetList().values().toArray();
		for (Object pet : petList) {
			if (pet instanceof L1PetInstance) {
				L1PetInstance pets = (L1PetInstance) pet;
				pets.setMasterTarget(target);
			}
			else if (pet instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) pet;
				summon.setMasterTarget(target);
			}
		}
	}

	public void delInvis() {
		// 魔法接続時間内はこちらを利用
		if (hasSkillEffect(INVISIBILITY)) { // インビジビリティ
			killSkillEffectTimer(INVISIBILITY);
			sendPackets(new S_Invis(getId(), 0));
			broadcastPacket(new S_OtherCharPacks(this));
		}
		if (hasSkillEffect(BLIND_HIDING)) { // ブラインド ハイディング
			killSkillEffectTimer(BLIND_HIDING);
			sendPackets(new S_Invis(getId(), 0));
			broadcastPacket(new S_OtherCharPacks(this));
		}
	}

	public void delBlindHiding() {
		// 魔法接続時間終了はこちら
		killSkillEffectTimer(BLIND_HIDING);
		sendPackets(new S_Invis(getId(), 0));
		broadcastPacket(new S_OtherCharPacks(this));
	}

	// 魔法のダメージの場合はここを使用 (ここで魔法ダメージ軽減処理) attr:0.無属性魔法,1.地魔法,2.火魔法,3.水魔法,4.風魔法
	public void receiveDamage(L1Character attacker, int damage, int attr) {
		int player_mr = getMr();
		int rnd = Random.nextInt(100) + 1;
		if (player_mr >= rnd) {
			damage /= 2;
		}
		receiveDamage(attacker, damage, false);
	}

	public void receiveManaDamage(L1Character attacker, int mpDamage) { // 攻撃でＭＰを減らすときはここを使用
		if ((mpDamage > 0) && !isDead()) {
			delInvis();
			if (attacker instanceof L1PcInstance) {
				L1PinkName.onAction(this, attacker);
			}
			if ((attacker instanceof L1PcInstance) && ((L1PcInstance) attacker).isPinkName()) {
				// ガードが画面内にいれば、攻撃者をガードのターゲットに設定する
				for (L1Object object : L1World.getInstance().getVisibleObjects(attacker)) {
					if (object instanceof L1GuardInstance) {
						L1GuardInstance guard = (L1GuardInstance) object;
						guard.setTarget(((L1PcInstance) attacker));
					}
				}
			}

			int newMp = getCurrentMp() - mpDamage;
			if (newMp > getMaxMp()) {
				newMp = getMaxMp();
			}

			if (newMp <= 0) {
				newMp = 0;
			}
			setCurrentMp(newMp);
		}
	}

	public double _oldTime = 0; // 連続魔法ダメージの軽減に使用する

	public void receiveDamage(L1Character attacker, double damage, boolean isMagicDamage) { // 攻撃でＨＰを減らすときはここを使用
		if ((getCurrentHp() > 0) && !isDead()) {
			if (attacker != this) {
				if (!(attacker instanceof L1EffectInstance) && !knownsObject(attacker) && (attacker.getMapId() == getMapId())) {
					attacker.onPerceive(this);
				}
			}

			if (isMagicDamage == true) { // 連続魔法ダメージによる軽減
				double nowTime = (double) System.currentTimeMillis();
				double interval = (20D - (nowTime - _oldTime) / 100D) % 20D;

				if (damage > 0) {
					if (interval > 0) 
						damage *= (1D - interval / 30D);

					if (damage < 1) {
						damage = 0;
					}

					_oldTime = nowTime; // 次回のために時間を保存
				}
			}
			if (damage > 0) {
				delInvis();
				if (attacker instanceof L1PcInstance) {
					L1PinkName.onAction(this, attacker);
				}
				if ((attacker instanceof L1PcInstance) && ((L1PcInstance) attacker).isPinkName()) {
					// ガードが画面内にいれば、攻撃者をガードのターゲットに設定する
					for (L1Object object : L1World.getInstance().getVisibleObjects(attacker)) {
						if (object instanceof L1GuardInstance) {
							L1GuardInstance guard = (L1GuardInstance) object;
							guard.setTarget(((L1PcInstance) attacker));
						}
					}
				}
				removeSkillEffect(FOG_OF_SLEEPING);
				removeSkillEffect(PHANTASM);
			}

			if (hasSkillEffect(MORTAL_BODY) && (getId() != attacker.getId())) {
				int rnd = Random.nextInt(100) + 1;
				if ((damage > 0) && (rnd <= 23)) {
					if (attacker instanceof L1PcInstance) {
						L1PcInstance attackPc = (L1PcInstance) attacker;
						attackPc.sendPackets(new S_DoActionGFX(attackPc.getId(), ActionCodes.ACTION_Damage));
						attackPc.broadcastPacket(new S_DoActionGFX(attackPc.getId(), ActionCodes.ACTION_Damage));
						attackPc.receiveDamage(this, 30, false);
					}
					else if (attacker instanceof L1NpcInstance) {
						L1NpcInstance attackNpc = (L1NpcInstance) attacker;
						attackNpc.broadcastPacket(new S_DoActionGFX(attackNpc.getId(), ActionCodes.ACTION_Damage));
						attackNpc.receiveDamage(this, 30);
					}
				}
			}
			if (getInventory().checkEquipped(145) // バーサーカーアックス
					|| getInventory().checkEquipped(149)) { // ミノタウルスアックス
				damage *= 1.5; // 被ダメ1.5倍
			}
			if (hasSkillEffect(ILLUSION_AVATAR)) {// 幻術師魔法 (幻覺：化身)
				damage *= 1.2; // 被ダメ1.2倍
			}
			if (attacker instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) attacker;
				// 目標在安區、攻擊者在安區、NOPVP
				if ((getZoneType() == 1) || (pet.getZoneType() == 1) || (checkNonPvP(this, pet))) {
					damage = 0;
				}
			} else if (attacker instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) attacker;
				// 目標在安區、攻擊者在安區、NOPVP
				if ((getZoneType() == 1) || (summon.getZoneType() == 1) || (checkNonPvP(this, summon))) {
					damage = 0;
				}
			}
			int newHp = getCurrentHp() - (int) (damage);
			if (newHp > getMaxHp()) {
				newHp = getMaxHp();
			}
			if (newHp <= 0) {
				if (isGm()) {
					setCurrentHp(getMaxHp());
				}
				else {
					death(attacker);
				}
			}
			if (newHp > 0) {
				setCurrentHp(newHp);
			}
			int healHp = 0;
			if (getInventory().checkEquipped(21119)	// パブリオンハイドロシリーズ
					|| getInventory().checkEquipped(21120)
					|| getInventory().checkEquipped(21121)
					|| getInventory().checkEquipped(21122)) {
				int rnd = Random.nextInt(100) + 1;
				if (damage > 0 && rnd <= 5) { // 5%の確立で回復
					sendPackets(new S_SkillSound(getId(), 2187));
					broadcastPacket(new S_SkillSound(getId(), 2187));
					healHp = Random.nextInt(60) + 60;
					newHp += healHp;
					if(newHp > getMaxHp()) {
						newHp = getMaxHp();
					}
					setCurrentHp(newHp);
				}
			}
		}
		else if (!isDead()) { // 念のため
			System.out.println("警告：プレイヤーのＨＰ減少処理が正しく行われていない箇所があります。※もしくは最初からＨＰ０");
			death(attacker);
		}
	}

	public void death(L1Character lastAttacker) {
		synchronized (this) {
			if (isDead()) {
				return;
			}
			setDead(true);
			setStatus(ActionCodes.ACTION_Die);
		}
		
		//死亡, 取消交易
		if (getTradeID() != 0) {
	         final L1Trade trade = new L1Trade();
	         trade.TradeCancel(this);
	    }
	      
		GeneralThreadPool.getInstance().execute(new Death(lastAttacker));
	}

	private class Death implements Runnable {
		L1Character _lastAttacker;

		Death(L1Character cha) {
			_lastAttacker = cha;
		}

		@Override
		public void run() {
			L1Character lastAttacker = _lastAttacker;
			_lastAttacker = null;
			setCurrentHp(0);
			setGresValid(false); // EXPロストするまでG-RES無効

			while (isTeleport()) { // テレポート中なら終わるまで待つ
				try {
					Thread.sleep(300);
				}
				catch (Exception e) {}
			}

			stopHpRegeneration();
			stopMpRegeneration();
			stopCryOfSurvival();
            setCryTime(0);

			int targetobjid = getId();
			getMap().setPassable(getLocation(), true);

			// エンチャントを解除する
			// 変身状態も解除されるため、キャンセレーションをかけてから変身状態に戻す
			int tempchargfx = 0;
			if (hasSkillEffect(SHAPE_CHANGE)) {
				tempchargfx = getTempCharGfx();
				setTempCharGfxAtDead(tempchargfx);
			}
			else {
				setTempCharGfxAtDead(getClassId());
			}

			// キャンセレーションをエフェクトなしでかける
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(L1PcInstance.this, CANCELLATION, getId(), getX(), getY(), null, 0, L1SkillUse.TYPE_LOGIN);

			// 戰鬥藥水
			if (hasSkillEffect(EFFECT_POTION_OF_BATTLE)) {
				removeSkillEffect(EFFECT_POTION_OF_BATTLE);
			}
			// 象牙塔妙藥
			if (hasSkillEffect(COOKING_WONDER_DRUG)) {
				removeSkillEffect(COOKING_WONDER_DRUG);
			}

			sendPackets(new S_DoActionGFX(targetobjid, ActionCodes.ACTION_Die));
			broadcastPacket(new S_DoActionGFX(targetobjid, ActionCodes.ACTION_Die));

			if (lastAttacker != L1PcInstance.this) {
				// セーフティーゾーン、コンバットゾーンで最後に殺したキャラが
				// プレイヤーorペットだったら、ペナルティなし
				if (getZoneType() != 0) {
					L1PcInstance player = null;
					if (lastAttacker instanceof L1PcInstance) {
						player = (L1PcInstance) lastAttacker;
					}
					else if (lastAttacker instanceof L1PetInstance) {
						player = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
					}
					else if (lastAttacker instanceof L1SummonInstance) {
						player = (L1PcInstance) ((L1SummonInstance) lastAttacker).getMaster();
					}
					if (player != null) {
						// 戦争中に戦争エリアに居る場合は例外
						if (!isInWarAreaAndWarTime(L1PcInstance.this, player)) {
							return;
						}
					}
				}

				boolean sim_ret = simWarResult(lastAttacker); // 模擬戦
				if (sim_ret == true) { // 模擬戦中ならペナルティなし
					return;
				}
			}

			if (!getMap().isEnabledDeathPenalty()) {
				return;
			}

			// 決闘中ならペナルティなし
			L1PcInstance fightPc = null;
			if (lastAttacker instanceof L1PcInstance) {
				fightPc = (L1PcInstance) lastAttacker;
			}

			// 判斷是否屬於新手保護階段, 並且是被其他玩家所殺死
			boolean isNovice = false;
			if (hasSkillEffect(L1SkillId.STATUS_NOVICE) && (fightPc != null)) {

				// 判斷是否在新手等級保護範圍內
				if (fightPc.getLevel() > (getLevel() + Config.NOVICE_PROTECTION_LEVEL_RANGE)) {
					isNovice = true;
				}
			}

			if (fightPc != null) {
				if ((getFightId() == fightPc.getId()) && (fightPc.getFightId() == getId())) { // 決闘中
					setFightId(0);
					sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, 0, 0));
					fightPc.setFightId(0);
					fightPc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, 0, 0));
					return;
				}
			}

			/*
			 * deathPenalty(); // EXPロスト
			 * 
			 * setGresValid(true); // EXPロストしたらG-RES有効
			 * 
			 * if (getExpRes() == 0) { setExpRes(1); }
			 */

			// ガードに殺された場合のみ、PKカウントを減らしガードに攻撃されなくなる
			if (lastAttacker instanceof L1GuardInstance) {
				if (get_PKcount() > 0) {
					set_PKcount(get_PKcount() - 1);
				}
				setLastPk(null);
			}
			if (lastAttacker instanceof L1GuardianInstance) {
				if (getPkCountForElf() > 0) {
					setPkCountForElf(getPkCountForElf() - 1);
				}
				setLastPkForElf(null);
			}

			// 增加新手保護階段, 將不會損失道具(不會噴裝)
			if (!isNovice) {
				// 一定の確率でアイテムをDROP
				// アライメント32000以上で0%、以降-1000毎に0.4%
				// アライメントが0未満の場合は-1000毎に0.8%
				// アライメント-32000以下で最高51.2%のDROP率
				int lostRate = (int) (((getLawful() + 32768D) / 1000D - 65D) * 4D);
				if (lostRate < 0) {
					lostRate *= -1;
					if (getLawful() < 0) {
						lostRate *= 2;
					}
					int rnd = Random.nextInt(1000) + 1;
					if (rnd <= lostRate) {
						int count = 1;
						if (getLawful() <= -30000) {
							count = Random.nextInt(4) + 1;
						}
						else if (getLawful() <= -20000) {
							count = Random.nextInt(3) + 1;
						}
						else if (getLawful() <= -10000) {
							count = Random.nextInt(2) + 1;
						}
						else if (getLawful() < 0) {
							count = Random.nextInt(1) + 1;
						}
						caoPenaltyResult(count);
					}
				}
			}

			boolean castle_ret = castleWarResult(); // 攻城戦
			if (castle_ret == true) { // 攻城戦中で旗内なら赤ネームペナルティなし
				return;
			}

			if (!getMap().isEnabledDeathPenalty()) {
				return;
			}

			// 增加新手保護階段, 將不會損失經驗值
			if (!isNovice) {
				deathPenalty(); // EXPロスト
				setGresValid(true); // EXPロストしたらG-RES有効
			}

			if (get_PKcount() > 0) {
				set_PKcount(get_PKcount() - 1);
			}
			setLastPk(null);

			// 最後に殺したキャラがプレイヤーだったら、赤ネームにする
			L1PcInstance player = null;
			if (lastAttacker instanceof L1PcInstance) {
				player = (L1PcInstance) lastAttacker;
				L1World.getInstance().broadcastServerMessage(player.getName() + " just owned " + getName() + " in battle!");
				player.setKill(player.getKill()+1);
				setDeath(getDeath()+1);
				if (player instanceof L1PcInstance) {

					try {

						Timestamp ts = new Timestamp(System.currentTimeMillis());	
						Connection con = null;
						PreparedStatement pstm = null;		
						con = L1DatabaseFactory.getInstance().getConnection();
						pstm = con.prepareStatement("INSERT INTO character_pvp (killer_char_obj_id, killer_char_name, killer_lvl, victim_char_obj_id, victim_char_name, victim_lvl, date, locx, locy, mapid, penalty) VALUES (?,?,?,?,?,?,?,?,?,?,?);");
						pstm.setInt(1, player.getId());
						pstm.setString(2, player.getName());
						pstm.setInt(3, player.getLevel());
						pstm.setInt(4, getId());
						pstm.setString(5, getName());
						pstm.setInt(6, getLevel());
						pstm.setTimestamp(7, ts);
						pstm.setInt(8, getX());
						pstm.setInt(9, getY());
						pstm.setInt(10, getMapId());
						if (isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() != 2 && getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 1);
						} else if (!isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() != 2 && !getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 2);
						} else if (isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() != 2 && !getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 3);
							//doorway
						} else if (!isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() != 2 && getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 4);
						} else if (isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() == 2 && getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 5);
						} else if (!isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() == 2 && !getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 6);
						} else if (isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() == 2 && !getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 7);
							//normal zone
						} else if (!isInWarAreaAndWarTime(L1PcInstance.this, player) && getZoneType() == 2 && getMap().isEnabledDeathPenalty()) {
							pstm.setInt(11, 8);
						} else {
							pstm.setInt(11, 9);
						}
						pstm.execute();			
						SQLUtil.close(pstm);
						SQLUtil.close(con);
					} catch (Exception e) { _log.log(Level.SEVERE, e.getLocalizedMessage(), e); } 


				}
			}
			if (player != null) {
				if ((getLawful() >= 0) && (isPinkName() == false)) {
					boolean isChangePkCount = false;
					// アライメントが30000未満の場合はPKカウント増加
					if (player.getLawful() < 30000) {
						player.set_PKcount(player.get_PKcount() + 1);
//						L1World.getInstance().broadcastServerMessage(player.getName() + " is dominating with pk count now at "+
//								player.get_PKcount()+"!");
						isChangePkCount = true;
						if (player.isElf() && isElf()) {
							player.setPkCountForElf(player.getPkCountForElf() + 1);
						}
					}
					player.setLastPk();
					/** 正義值滿不會被警衛追殺 */
					if (player.getLawful() == 32767) {
						player.setLastPk(null);
					}
					if (player.isElf() && isElf()) {
						player.setLastPkForElf();
					}

					// アライメント処理
					// 公式の発表および各LVでのPKからつじつまの合うように変更
					// （PK側のLVに依存し、高LVほどリスクも高い）
					// 48あたりで-8kほど DKの時点で10k強
					// 60で約20k強 65で30k弱
					int lawful;

					if (player.getLevel() < 50) {
						lawful = -1 * (int) ((Math.pow(player.getLevel(), 2) * 4));
					}
					else {
						lawful = -1 * (int) ((Math.pow(player.getLevel(), 3) * 0.08));
					}
					// もし(元々のアライメント-1000)が計算後より低い場合
					// 元々のアライメント-1000をアライメント値とする
					// （連続でPKしたときにほとんど値が変わらなかった記憶より）
					// これは上の式よりも自信度が低いうろ覚えですので
					// 明らかにこうならない！という場合は修正お願いします
					if ((player.getLawful() - 1000) < lawful) {
						lawful = player.getLawful() - 1000;
					}

					if (lawful <= -32768) {
						lawful = -32768;
					}
					player.setLawful(lawful);

					S_Lawful s_lawful = new S_Lawful(player.getId(), player.getLawful());
					player.sendPackets(s_lawful);
					player.broadcastPacket(s_lawful);

					if (isChangePkCount && (player.get_PKcount() >= 5) && (player.get_PKcount() < 10)) {
						// あなたのPK回数が%0になりました。回数が%1になると地獄行きです。
						player.sendPackets(new S_BlueMessage(551, String.valueOf(player.get_PKcount()), "10"));
					}
					else if (isChangePkCount && (player.get_PKcount() >= 10)) {
						player.beginHell(true);
					}
				}
				else {
					setPinkName(false);
				}
			}
			_pcDeleteTimer = new L1PcDeleteTimer(L1PcInstance.this);
			_pcDeleteTimer.begin();
		}
	}

	public void stopPcDeleteTimer() {
		if (_pcDeleteTimer != null) {
			_pcDeleteTimer.cancel();
			_pcDeleteTimer = null;
		}
	}

	private void caoPenaltyResult(int count) {
		for (int i = 0; i < count; i++) {
			L1ItemInstance item = getInventory().CaoPenalty();

			if (item != null) {
				getInventory().tradeItem(item, item.isStackable() ? item.getCount() : 1,
						L1World.getInstance().getInventory(getX(), getY(), getMapId()));
				sendPackets(new S_ServerMessage(638, item.getLogName())); // %0を失いました。
			}
			else {}
		}
	}

	public boolean castleWarResult() {
		if ((getClanid() != 0) && isCrown()) { // クラン所属中プリのチェック
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			// 全戦争リストを取得
			for (L1War war : L1World.getInstance().getWarList()) {
				int warType = war.GetWarType();
				boolean isInWar = war.CheckClanInWar(getClanname());
				boolean isAttackClan = war.CheckAttackClan(getClanname());
				if ((getId() == clan.getLeaderId()) && // 血盟主で攻撃側で攻城戦中
						(warType == 1) && isInWar && isAttackClan) {
					String enemyClanName = war.GetEnemyClanName(getClanname());
					if (enemyClanName != null) {
						war.CeaseWar(getClanname(), enemyClanName); // 終結
					}
					break;
				}
			}
		}

		int castleId = 0;
		boolean isNowWar = false;
		castleId = L1CastleLocation.getCastleIdByArea(this);
		if (castleId != 0) { // 旗内に居る
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		return isNowWar;
	}

	public boolean simWarResult(L1Character lastAttacker) {
		if (getClanid() == 0) { // クラン所属していない
			return false;
		}
		if (Config.SIM_WAR_PENALTY) { // 模擬戦ペナルティありの場合はfalse
			return false;
		}
		L1PcInstance attacker = null;
		String enemyClanName = null;
		boolean sameWar = false;

		if (lastAttacker instanceof L1PcInstance) {
			attacker = (L1PcInstance) lastAttacker;
		}
		else if (lastAttacker instanceof L1PetInstance) {
			attacker = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
		}
		else if (lastAttacker instanceof L1SummonInstance) {
			attacker = (L1PcInstance) ((L1SummonInstance) lastAttacker).getMaster();
		}
		else {
			return false;
		}

		// 全戦争リストを取得
		for (L1War war : L1World.getInstance().getWarList()) {
			L1Clan clan = L1World.getInstance().getClan(getClanname());

			int warType = war.GetWarType();
			boolean isInWar = war.CheckClanInWar(getClanname());
			if ((attacker != null) && (attacker.getClanid() != 0)) { // lastAttackerがPC、サモン、ペットでクラン所属中
				sameWar = war.CheckClanInSameWar(getClanname(), attacker.getClanname());
			}

			if ((getId() == clan.getLeaderId()) && // 血盟主で模擬戦中
					(warType == 2) && (isInWar == true)) {
				enemyClanName = war.GetEnemyClanName(getClanname());
				if (enemyClanName != null) {
					war.CeaseWar(getClanname(), enemyClanName); // 終結
				}
			}

			if ((warType == 2) && sameWar) {// 模擬戦で同じ戦争に参加中の場合、ペナルティなし
				return true;
			}
		}
		return false;
	}

	public void resExp() {
		int oldLevel = getLevel();
		int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
		int exp = 0;
		if (oldLevel < 45) {
			exp = (int) (needExp * 0.05);
		}
		else if (oldLevel == 45) {
			exp = (int) (needExp * 0.045);
		}
		else if (oldLevel == 46) {
			exp = (int) (needExp * 0.04);
		}
		else if (oldLevel == 47) {
			exp = (int) (needExp * 0.035);
		}
		else if (oldLevel == 48) {
			exp = (int) (needExp * 0.03);
		}
		else if (oldLevel >= 49 && oldLevel < 65) {
			exp = (int) (needExp * 0.025);
		} else if (oldLevel >= 65 && oldLevel < 70) {
			exp = (int) (needExp * 0.0125);
		} else if (oldLevel >= 65 && oldLevel < 75) {
			exp = (int) (needExp * 0.00625);
		} else if (oldLevel >= 75 && oldLevel < 79) {
			exp = (int) (needExp * 0.003125);
		} else if (oldLevel >= 79 && oldLevel < 80) {
			exp = (int) (needExp * 0.0015625);
		} else if (oldLevel >= 80) {
			exp = (int) (needExp * 0.00078125 / Math.pow(2, oldLevel - 80));
		}

		if (exp == 0) {
			return;
		}
		addExp(exp);
	}

	public void deathPenalty() {
		int oldLevel = getLevel();
		int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
		int exp = 0;
		if ((oldLevel >= 1) && (oldLevel < 11)) {
			exp = 0;
		}
		else if ((oldLevel >= 11) && (oldLevel < 45)) {
			exp = (int) (needExp * 0.1);
		}
		else if (oldLevel == 45) {
			exp = (int) (needExp * 0.09);
		}
		else if (oldLevel == 46) {
			exp = (int) (needExp * 0.08);
		}
		else if (oldLevel == 47) {
			exp = (int) (needExp * 0.07);
		}
		else if (oldLevel == 48) {
			exp = (int) (needExp * 0.06);
		}
//		else if (oldLevel >= 49) {
//			exp = (int) (needExp * 0.05);
//		}
		// Modified to scale down the XP death loss % at higher lvls.
		  else if (oldLevel >= 49 && oldLevel < 65) {
			exp = (int) (needExp * 0.05);
		} else if (oldLevel >= 65 && oldLevel < 70) {
			exp = (int) (needExp * 0.025);
		} else if (oldLevel >= 65 && oldLevel < 75) {
			exp = (int) (needExp * 0.0125);
		} else if (oldLevel >= 75 && oldLevel < 79) {
			exp = (int) (needExp * 0.00625);
		} else if (oldLevel >= 79 && oldLevel < 80) {
			exp = (int) (needExp * 0.003125);
		} else if (oldLevel >= 80) {
			exp = (int) (needExp * 0.0015625 / Math.pow(2, oldLevel - 80));
		}

		if (exp == 0) {
			return;
		}
		
		if (getExpRes()!=1) setExpRes(1);
//		if (getExpRes() >= 0) {
//			setExpRes(getExpRes() + 1);
//		}
		addExp(-exp);
		onChangeExp();
	}

	private int _originalEr = 0; // ● オリジナルDEX ER補正

	public int getOriginalEr() {

		return _originalEr;
	}

	public int getEr() {
		if (hasSkillEffect(STRIKER_GALE)) {
			return 0;
		}

		int er = 0;
		if (isKnight()) {
			er = getLevel() / 4; // ナイト
		}
		else if (isCrown() || isElf()) {
			er = getLevel() / 8; // 君主・エルフ
		}
		else if (isDarkelf()) {
			er = getLevel() / 6; // ダークエルフ
		}
		else if (isWizard()) {
			er = getLevel() / 10; // ウィザード
		}
		else if (isDragonKnight()) {
			er = getLevel() / 5; // ドラゴンナイト
		}
		else if (isIllusionist()) {
			er = getLevel() / 9; // イリュージョニスト
		}

		er += (getDex() - 8) / 2;

		er += getOriginalEr();

		if (hasSkillEffect(DRESS_EVASION)) {
			er += 12;
		}
		if (hasSkillEffect(SOLID_CARRIAGE)) {
			er += 15;
		}
		return er;
	}

	public L1BookMark getBookMark(String name) {
		for (int i = 0; i < _bookmarks.size(); i++) {
			L1BookMark element = _bookmarks.get(i);
			if (element.getName().equalsIgnoreCase(name)) {
				return element;
			}

		}
		return null;
	}

	public L1BookMark getBookMark(int id) {
		for (int i = 0; i < _bookmarks.size(); i++) {
			L1BookMark element = _bookmarks.get(i);
			if (element.getId() == id) {
				return element;
			}

		}
		return null;
	}

	public int getBookMarkSize() {
		return _bookmarks.size();
	}

	public void addBookMark(L1BookMark book) {
		_bookmarks.add(book);
	}

	public void removeBookMark(L1BookMark book) {
		_bookmarks.remove(book);
	}

	public L1ItemInstance getWeapon() {
		return _weapon;
	}

	public void setWeapon(L1ItemInstance weapon) {
		_weapon = weapon;
	}

	public L1Quest getQuest() {
		return _quest;
	}

	public boolean isCrown() {
		return ((getClassId() == CLASSID_PRINCE) || (getClassId() == CLASSID_PRINCESS));
	}

	public boolean isKnight() {
		return ((getClassId() == CLASSID_KNIGHT_MALE) || (getClassId() == CLASSID_KNIGHT_FEMALE));
	}

	public boolean isElf() {
		return ((getClassId() == CLASSID_ELF_MALE) || (getClassId() == CLASSID_ELF_FEMALE));
	}

	public boolean isWizard() {
		return ((getClassId() == CLASSID_WIZARD_MALE) || (getClassId() == CLASSID_WIZARD_FEMALE));
	}

	public boolean isDarkelf() {
		return ((getClassId() == CLASSID_DARK_ELF_MALE) || (getClassId() == CLASSID_DARK_ELF_FEMALE));
	}

	public boolean isDragonKnight() {
		return ((getClassId() == CLASSID_DRAGON_KNIGHT_MALE) || (getClassId() == CLASSID_DRAGON_KNIGHT_FEMALE));
	}

	public boolean isIllusionist() {
		return ((getClassId() == CLASSID_ILLUSIONIST_MALE) || (getClassId() == CLASSID_ILLUSIONIST_FEMALE));
	}

	private static Logger _log = Logger.getLogger(L1PcInstance.class.getName());
	private ClientThread _netConnection;
	private int _classId;
	private int _type;
	private int _exp;
	private final L1Karma _karma = new L1Karma();
	private boolean _gm;
	private boolean _monitor;
	private boolean _gmInvis;
	private short _accessLevel;
	private int _currentWeapon;
	private final L1PcInventory _inventory;
	private final L1DwarfInventory _dwarf;
	private final L1DwarfForElfInventory _dwarfForElf;
	private final L1Inventory _tradewindow;
	private L1ItemInstance _weapon;
	private L1Party _party;
	private L1ChatParty _chatParty;
	private int _partyID;
	private int _tradeID;
	private boolean _tradeOk;
	private int _tempID;
	private boolean _isTeleport = false;
	private boolean _isDrink = false;
	private boolean _isGres = false;
	private boolean _isPinkName = false;
	private final List<L1BookMark> _bookmarks;
	private L1Quest _quest;
	private MpRegeneration _mpRegen;
	private MpRegenerationByDoll _mpRegenByDoll;
	private MpReductionByAwake _mpReductionByAwake;
	private HpRegeneration _hpRegen;
	private HpRegenerationByDoll _hpRegenByDoll;
	private ItemMakeByDoll _itemMakeByDoll;
	private static Timer _regenTimer = new Timer(true);
	private boolean _mpRegenActive;
	private boolean _mpRegenActiveByDoll;
	private boolean _mpReductionActiveByAwake;
	private boolean _hpRegenActive;
	private boolean _hpRegenActiveByDoll;
	private boolean _ItemMakeActiveByDoll;
	private L1EquipmentSlot _equipSlot;
	private L1PcDeleteTimer _pcDeleteTimer;
	private String _accountName; // ● アカウントネーム

	public String getAccountName() {
		return _accountName;
	}

	public void setAccountName(String s) {
		_accountName = s;
	}

	private short _baseMaxHp = 0; // ● ＭＡＸＨＰベース（1～32767）

	public short getBaseMaxHp() {
		return _baseMaxHp;
	}

	public void addBaseMaxHp(short i) {
		i += _baseMaxHp;
		if (i >= 32767) {
			i = 32767;
		}
		else if (i < 1) {
			i = 1;
		}
		addMaxHp(i - _baseMaxHp);
		_baseMaxHp = i;
	}

	private short _baseMaxMp = 0; // ● ＭＡＸＭＰベース（0～32767）

	public short getBaseMaxMp() {
		return _baseMaxMp;
	}

	public void addBaseMaxMp(short i) {
		i += _baseMaxMp;
		if (i >= 32767) {
			i = 32767;
		}
		else if (i < 0) {
			i = 0;
		}
		addMaxMp(i - _baseMaxMp);
		_baseMaxMp = i;
	}

	private int _baseAc = 0; // ● ＡＣベース（-128～127）

	public int getBaseAc() {
		return _baseAc;
	}

	private int _originalAc = 0; // ● オリジナルDEX ＡＣ補正

	public int getOriginalAc() {

		return _originalAc;
	}

	private byte _baseStr = 0; // ● ＳＴＲベース（1～127）

	public byte getBaseStr() {
		return _baseStr;
	}

	public void addBaseStr(byte i) {
		i += _baseStr;
		if (i >= 127) {
			i = 127;
		}
		else if (i < 1) {
			i = 1;
		}
		addStr((byte) (i - _baseStr));
		_baseStr = i;
	}

	private byte _baseCon = 0; // ● ＣＯＮベース（1～127）

	public byte getBaseCon() {
		return _baseCon;
	}

	public void addBaseCon(byte i) {
		i += _baseCon;
		if (i >= 127) {
			i = 127;
		}
		else if (i < 1) {
			i = 1;
		}
		addCon((byte) (i - _baseCon));
		_baseCon = i;
	}

	private byte _baseDex = 0; // ● ＤＥＸベース（1～127）

	public byte getBaseDex() {
		return _baseDex;
	}

	public void addBaseDex(byte i) {
		i += _baseDex;
		if (i >= 127) {
			i = 127;
		}
		else if (i < 1) {
			i = 1;
		}
		addDex((byte) (i - _baseDex));
		_baseDex = i;
	}

	private byte _baseCha = 0; // ● ＣＨＡベース（1～127）

	public byte getBaseCha() {
		return _baseCha;
	}

	public void addBaseCha(byte i) {
		i += _baseCha;
		if (i >= 127) {
			i = 127;
		}
		else if (i < 1) {
			i = 1;
		}
		addCha((byte) (i - _baseCha));
		_baseCha = i;
	}

	private byte _baseInt = 0; // ● ＩＮＴベース（1～127）

	public byte getBaseInt() {
		return _baseInt;
	}

	public void addBaseInt(byte i) {
		i += _baseInt;
		if (i >= 127) {
			i = 127;
		}
		else if (i < 1) {
			i = 1;
		}
		addInt((byte) (i - _baseInt));
		_baseInt = i;
	}

	private byte _baseWis = 0; // ● ＷＩＳベース（1～127）

	public byte getBaseWis() {
		return _baseWis;
	}

	public void addBaseWis(byte i) {
		i += _baseWis;
		if (i >= 127) {
			i = 127;
		}
		else if (i < 1) {
			i = 1;
		}
		addWis((byte) (i - _baseWis));
		_baseWis = i;
	}

	private int _originalStr = 0; // ● オリジナル STR

	public int getOriginalStr() {
		return _originalStr;
	}

	public void setOriginalStr(int i) {
		_originalStr = i;
	}

	private int _originalCon = 0; // ● オリジナル CON

	public int getOriginalCon() {
		return _originalCon;
	}

	public void setOriginalCon(int i) {
		_originalCon = i;
	}

	private int _originalDex = 0; // ● オリジナル DEX

	public int getOriginalDex() {
		return _originalDex;
	}

	public void setOriginalDex(int i) {
		_originalDex = i;
	}

	private int _originalCha = 0; // ● オリジナル CHA

	public int getOriginalCha() {
		return _originalCha;
	}

	public void setOriginalCha(int i) {
		_originalCha = i;
	}

	private int _originalInt = 0; // ● オリジナル INT

	public int getOriginalInt() {
		return _originalInt;
	}

	public void setOriginalInt(int i) {
		_originalInt = i;
	}

	private int _originalWis = 0; // ● オリジナル WIS

	public int getOriginalWis() {
		return _originalWis;
	}

	public void setOriginalWis(int i) {
		_originalWis = i;
	}

	private int _originalDmgup = 0; // ● オリジナルSTR ダメージ補正

	public int getOriginalDmgup() {

		return _originalDmgup;
	}

	private int _originalBowDmgup = 0; // ● オリジナルDEX 弓ダメージ補正

	public int getOriginalBowDmgup() {

		return _originalBowDmgup;
	}

	private int _originalHitup = 0; // ● オリジナルSTR 命中補正

	public int getOriginalHitup() {

		return _originalHitup;
	}

	private int _originalBowHitup = 0; // ● オリジナルDEX 命中補正

	public int getOriginalBowHitup() {

		return _originalBowHitup;
	}

	private int _originalMr = 0; // ● オリジナルWIS 魔法防御

	public int getOriginalMr() {

		return _originalMr;
	}

	private int _originalMagicHit = 0; // ● オリジナルINT 魔法命中

	public int getOriginalMagicHit() {

		return _originalMagicHit;
	}

	private int _originalMagicCritical = 0; // ● オリジナルINT 魔法クリティカル

	public int getOriginalMagicCritical() {

		return _originalMagicCritical;
	}

	private int _originalMagicConsumeReduction = 0; // ● オリジナルINT 消費MP軽減

	public int getOriginalMagicConsumeReduction() {

		return _originalMagicConsumeReduction;
	}

	private int _originalMagicDamage = 0; // ● オリジナルINT 魔法ダメージ

	public int getOriginalMagicDamage() {

		return _originalMagicDamage;
	}

	private int _originalHpup = 0; // ● オリジナルCON HP上昇値補正

	public int getOriginalHpup() {

		return _originalHpup;
	}

	private int _originalMpup = 0; // ● オリジナルWIS MP上昇値補正

	public int getOriginalMpup() {

		return _originalMpup;
	}

	private int _baseDmgup = 0; // ● ダメージ補正ベース（-128～127）

	public int getBaseDmgup() {
		return _baseDmgup;
	}

	private int _baseBowDmgup = 0; // ● 弓ダメージ補正ベース（-128～127）

	public int getBaseBowDmgup() {
		return _baseBowDmgup;
	}

	private int _baseHitup = 0; // ● 命中補正ベース（-128～127）

	public int getBaseHitup() {
		return _baseHitup;
	}

	private int _baseBowHitup = 0; // ● 弓命中補正ベース（-128～127）

	public int getBaseBowHitup() {
		return _baseBowHitup;
	}

	private int _baseMr = 0; // ● 魔法防御ベース（0～）

	public int getBaseMr() {
		return _baseMr;
	}

	private int _advenHp; // ● // アドバンスド スピリッツで増加しているＨＰ

	public int getAdvenHp() {
		return _advenHp;
	}

	public void setAdvenHp(int i) {
		_advenHp = i;
	}

	private int _advenMp; // ● // アドバンスド スピリッツで増加しているＭＰ

	public int getAdvenMp() {
		return _advenMp;
	}

	public void setAdvenMp(int i) {
		_advenMp = i;
	}

	private int _highLevel; // ● 過去最高レベル

	public int getHighLevel() {
		return _highLevel;
	}

	public void setHighLevel(int i) {
		_highLevel = i;
	}

	private int _bonusStats; // ● 割り振ったボーナスステータス

	public int getBonusStats() {
		return _bonusStats;
	}

	public void setBonusStats(int i) {
		_bonusStats = i;
	}

	private int _elixirStats; // ● エリクサーで上がったステータス

	public int getElixirStats() {
		return _elixirStats;
	}

	public void setElixirStats(int i) {
		_elixirStats = i;
	}

	private int _elfAttr; // ● エルフの属性

	public int getElfAttr() {
		return _elfAttr;
	}

	public void setElfAttr(int i) {
		_elfAttr = i;
	}

	private int _expRes; // ● EXP復旧

	public int getExpRes() {
		return _expRes;
	}

	public void setExpRes(int i) {
		_expRes = i;
	}

	private int _partnerId; // ● 結婚相手

	public int getPartnerId() {
		return _partnerId;
	}

	public void setPartnerId(int i) {
		_partnerId = i;
	}

	private int _onlineStatus; // ● オンライン状態

	public int getOnlineStatus() {
		return _onlineStatus;
	}

	public void setOnlineStatus(int i) {
		_onlineStatus = i;
	}

	private int _homeTownId; // ● ホームタウン

	public int getHomeTownId() {
		return _homeTownId;
	}

	public void setHomeTownId(int i) {
		_homeTownId = i;
	}

	private int _contribution; // ● 貢献度

	public int getContribution() {
		return _contribution;
	}

	public void setContribution(int i) {
		_contribution = i;
	}

	private int _pay; // 村莊福利金 此欄位由 HomeTownTimeController 處理 update

	public int getPay() {
		return _pay;
	}

	public void setPay(int i) {
		_pay = i;
	}

	// 地獄に滞在する時間（秒）
	private int _hellTime;

	public int getHellTime() {
		return _hellTime;
	}

	public void setHellTime(int i) {
		_hellTime = i;
	}

	private boolean _banned; // ● 凍結

	public boolean isBanned() {
		return _banned;
	}

	public void setBanned(boolean flag) {
		_banned = flag;
	}

	public L1EquipmentSlot getEquipSlot() {
		return _equipSlot;
	}

	public static L1PcInstance load(String charName) {
		L1PcInstance result = null;
		try {
			result = CharacterTable.getInstance().loadCharacter(charName);
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return result;
	}

	/**
	 * このプレイヤーの状態をストレージへ書き込む。
	 * 
	 * @throws Exception
	 */
	public void save() throws Exception {
		if (isGhost()) {
			return;
		}
		if (isInCharReset()) {
			return;
		}

		CharacterTable.getInstance().storeCharacter(this);
	}

	/**
	 * このプレイヤーのインベントリアイテムの状態をストレージへ書き込む。
	 */
	public void saveInventory() {
		for (L1ItemInstance item : getInventory().getItems()) {
			getInventory().saveItem(item, item.getRecordingColumns());
			getInventory().saveEnchantAccessory(item, item.getRecordingColumnsEnchantAccessory());
		}
	}

	public static final int REGENSTATE_NONE = 4;

	public static final int REGENSTATE_MOVE = 2;

	public static final int REGENSTATE_ATTACK = 1;

	public void setRegenState(int state) {
		_mpRegen.setState(state);
		_hpRegen.setState(state);
	}

	public double getMaxWeight() {
		int str = getStr();
		int con = getCon();
		double maxWeight = 150 * (Math.floor(0.6 * str + 0.4 * con + 1));

		double weightReductionByArmor = getWeightReduction(); // 防具による重量軽減
		weightReductionByArmor /= 100;

		double weightReductionByDoll = 0; // マジックドールによる重量軽減
		weightReductionByDoll += L1MagicDoll.getWeightReductionByDoll(this);
		weightReductionByDoll /= 100;

		int weightReductionByMagic = 0;
		if (hasSkillEffect(DECREASE_WEIGHT)) { // ディクリースウェイト
			weightReductionByMagic = 180;
		}

		double originalWeightReduction = 0; // オリジナルステータスによる重量軽減
		originalWeightReduction += 0.04 * (getOriginalStrWeightReduction() + getOriginalConWeightReduction());

		double weightReduction = 1 + weightReductionByArmor
				+ weightReductionByDoll + originalWeightReduction;

		maxWeight *= weightReduction;

		maxWeight += weightReductionByMagic;

		maxWeight *= Config.RATE_WEIGHT_LIMIT; // ウェイトレートを掛ける

		return maxWeight;
	}

	public boolean isRibrave() { // 生命之樹果實 移速 * 1.15
		return hasSkillEffect(STATUS_RIBRAVE);
	}

	public boolean isThirdSpeed() { // 三段加速 * 1.15
		return hasSkillEffect(STATUS_THIRD_SPEED);
	}

	public boolean isWindShackle() { // 風之枷鎖  攻速 / 2
		return hasSkillEffect(WIND_SHACKLE);
	}

	private int invisDelayCounter = 0;

	public boolean isInvisDelay() {
		return (invisDelayCounter > 0);
	}

	private Object _invisTimerMonitor = new Object();

	public void addInvisDelayCounter(int counter) {
		synchronized (_invisTimerMonitor) {
			invisDelayCounter += counter;
		}
	}

	private static final long DELAY_INVIS = 3000L;

	public void beginInvisTimer() {
		addInvisDelayCounter(1);
		GeneralThreadPool.getInstance().pcSchedule(new L1PcInvisDelay(getId()), DELAY_INVIS);
	}

	public synchronized void addExp(int exp) {
		_exp += exp;
		if (_exp > ExpTable.MAX_EXP) {
			_exp = ExpTable.MAX_EXP;
		}
	}

	public synchronized void addContribution(int contribution) {
		_contribution += contribution;
	}

	public void beginExpMonitor() {
		_expMonitorFuture = GeneralThreadPool.getInstance().pcScheduleAtFixedRate(new L1PcExpMonitor(getId()), 0L, INTERVAL_EXP_MONITOR);
	}

	private void levelUp(int gap) {
		resetLevel();

		// 復活のポーション
		if ((getLevel() == 99) && Config.ALT_REVIVAL_POTION) {
			try {
				L1Item l1item = ItemTable.getInstance().getTemplate(43000);
				if (l1item != null) {
					getInventory().storeItem(43000, 1);
					sendPackets(new S_ServerMessage(403, l1item.getName()));
				}
				else {
					sendPackets(new S_SystemMessage("返生藥水取得失敗。"));
				}
			}
			catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				sendPackets(new S_SystemMessage("返生藥水取得失敗。"));
			}
		}

		for (int i = 0; i < gap; i++) {
			short randomHp = CalcStat.calcStatHp(getType(), getBaseMaxHp(), getBaseCon(), getOriginalHpup());
			short randomMp = CalcStat.calcStatMp(getType(), getBaseMaxMp(), getBaseWis(), getOriginalMpup());
			addBaseMaxHp(randomHp);
			addBaseMaxMp(randomMp);
		}
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseAc();
		resetBaseMr();
		if (getLevel() > getHighLevel()) {
			setHighLevel(getLevel());
		}

		try {
			// DBにキャラクター情報を書き込む
			save();
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		// ボーナスステータス
		if ((getLevel() >= 51) && (getLevel() - 50 > getBonusStats())) {
			if ((getBaseStr() + getBaseDex() + getBaseCon() + getBaseInt() + getBaseWis() + getBaseCha()) < 210) {
				sendPackets(new S_bonusstats(getId(), 1));
			}
		}
		
		// added hp/mp refill on lvl up
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());

		sendPackets(new S_OwnCharStatus(this));

		// 根據等級判斷地圖限制
		if ((getMapId() == 2005 || getMapId() == 86)) { // 新手村
			if (getLevel() >= 13) { // 等級大於13
				if (getQuest().get_step(L1Quest.QUEST_TUTOR) != 255) {
					getQuest().set_step(L1Quest.QUEST_TUTOR, 255);
				}
				L1Teleport.teleport(this, 33084, 33391, (short) 4, 5, true);// 銀騎士村
			}
		} else if (getLevel() >= 52) { // 指定レベル
			if (getMapId() == 777) { // 見捨てられた者たちの地(影の神殿)
				L1Teleport.teleport(this, 34043, 32184, (short) 4, 5, true); // 象牙の塔前
			} else if ((getMapId() == 778) || (getMapId() == 779)) { // 見捨てられた者たちの地(欲望の洞窟)
				L1Teleport.teleport(this, 32608, 33178, (short) 4, 5, true); // WB
			}
		}

		// 處理新手保護系統(遭遇的守護)狀態資料的變動
		checkNoviceType();
	}

	private void levelDown(int gap) {
		resetLevel();

		for (int i = 0; i > gap; i--) {
			// レベルダウン時はランダム値をそのままマイナスする為に、base値に0を設定
			short randomHp = CalcStat.calcStatHp(getType(), 0, getBaseCon(), getOriginalHpup());
			short randomMp = CalcStat.calcStatMp(getType(), 0, getBaseWis(), getOriginalMpup());
			addBaseMaxHp((short) -randomHp);
			addBaseMaxMp((short) -randomMp);
		}
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseAc();
		resetBaseMr();
		if (Config.LEVEL_DOWN_RANGE != 0) {
			if (getHighLevel() - getLevel() >= Config.LEVEL_DOWN_RANGE) {
				sendPackets(new S_ServerMessage(64)); // ワールドとの接続が切断されました。
				sendPackets(new S_Disconnect());
				_log.info(String.format("レベルダウンの許容範囲を超えたため%sを強制切断しました。", getName()));
			}
		}

		try {
			// DBにキャラクター情報を書き込む
			save();
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		sendPackets(new S_OwnCharStatus(this));

		// 處理新手保護系統(遭遇的守護)狀態資料的變動
		checkNoviceType();
	}

	public void beginGameTimeCarrier() {
		new L1GameTimeCarrier(this).start();
	}

	private boolean _ghost = false; // ゴースト

	public boolean isGhost() {
		return _ghost;
	}

	private void setGhost(boolean flag) {
		_ghost = flag;
	}

	private boolean _ghostCanTalk = true; // NPCに話しかけられるか

	public boolean isGhostCanTalk() {
		return _ghostCanTalk;
	}

	private void setGhostCanTalk(boolean flag) {
		_ghostCanTalk = flag;
	}

	private boolean _isReserveGhost = false; // ゴースト解除準備

	public boolean isReserveGhost() {
		return _isReserveGhost;
	}

	private void setReserveGhost(boolean flag) {
		_isReserveGhost = flag;
	}

	public void beginGhost(int locx, int locy, short mapid, boolean canTalk) {
		beginGhost(locx, locy, mapid, canTalk, 0);
	}

	public void beginGhost(int locx, int locy, short mapid, boolean canTalk, int sec) {
		if (isGhost()) {
			return;
		}
		setGhost(true);
		_ghostSaveLocX = getX();
		_ghostSaveLocY = getY();
		_ghostSaveMapId = getMapId();
		_ghostSaveHeading = getHeading();
		setGhostCanTalk(canTalk);
		L1Teleport.teleport(this, locx, locy, mapid, 5, true);
		if (sec > 0) {
			_ghostFuture = GeneralThreadPool.getInstance().pcSchedule(new L1PcGhostMonitor(getId()), sec * 1000);
		}
	}

	public void makeReadyEndGhost() {
		setReserveGhost(true);
		L1Teleport.teleport(this, _ghostSaveLocX, _ghostSaveLocY, _ghostSaveMapId, _ghostSaveHeading, true);
	}

	public void endGhost() {
		setGhost(false);
		setGhostCanTalk(true);
		setReserveGhost(false);
	}

	private ScheduledFuture<?> _ghostFuture;

	private int _ghostSaveLocX = 0;

	private int _ghostSaveLocY = 0;

	private short _ghostSaveMapId = 0;

	private int _ghostSaveHeading = 0;

	private ScheduledFuture<?> _hellFuture;

	public void beginHell(boolean isFirst) {
		// 地獄以外に居るときは地獄へ強制移動
		if (getMapId() != 666) {
			int locx = 32701;
			int locy = 32777;
			short mapid = 666;
			L1Teleport.teleport(this, locx, locy, mapid, 5, false);
		}

		if (isFirst) {
			if (get_PKcount() <= 10) {
				setHellTime(300);
			}
			else {
				setHellTime(300 * (get_PKcount() - 10) + 300);
			}
			// あなたのPK回数が%0になり、地獄に落とされました。あなたはここで%1分間反省しなければなりません。
			sendPackets(new S_BlueMessage(552, String.valueOf(get_PKcount()), String.valueOf(getHellTime() / 60)));
		}
		else {
			// あなたは%0秒間ここにとどまらなければなりません。
			sendPackets(new S_BlueMessage(637, String.valueOf(getHellTime())));
		}
		if (_hellFuture == null) {
			_hellFuture = GeneralThreadPool.getInstance().pcScheduleAtFixedRate(new L1PcHellMonitor(getId()), 0L, 1000L);
		}
	}

	public void endHell() {
		if (_hellFuture != null) {
			_hellFuture.cancel(false);
			_hellFuture = null;
		}
		// 地獄から脱出したら火田村へ帰還させる。
		int[] loc = L1TownLocation.getGetBackLoc(L1TownLocation.TOWNID_ORCISH_FOREST);
		L1Teleport.teleport(this, loc[0], loc[1], (short) loc[2], 5, true);
		try {
			save();
		}
		catch (Exception ignore) {
			// ignore
		}
	}

	@Override
	public void setPoisonEffect(int effectId) {
		sendPackets(new S_Poison(getId(), effectId));

		if (!isGmInvis() && !isGhost() && !isInvisble()) {
			broadcastPacket(new S_Poison(getId(), effectId));
		}
		if (isGmInvis() || isGhost()) {}
		else if (isInvisble()) {
			broadcastPacketForFindInvis(new S_Poison(getId(), effectId), true);
		}
		else {
			broadcastPacket(new S_Poison(getId(), effectId));
		}
	}

	@Override
	public void healHp(int pt) {
		super.healHp(pt);

		sendPackets(new S_HPUpdate(this));
	}

	@Override
	public int getKarma() {
		return _karma.get();
	}

	@Override
	public void setKarma(int i) {
		_karma.set(i);
	}

	public void addKarma(int i) {
		synchronized (_karma) {
			_karma.add(i);
		}
	}

	public int getKarmaLevel() {
		return _karma.getLevel();
	}

	public int getKarmaPercent() {
		return _karma.getPercent();
	}

	private Timestamp _lastPk;

	/**
	 * プレイヤーの最終PK時間を返す。
	 * 
	 * @return _lastPk
	 * 
	 */
	public Timestamp getLastPk() {
		return _lastPk;
	}

	/**
	 * プレイヤーの最終PK時間を設定する。
	 * 
	 * @param time
	 *            最終PK時間（Timestamp型） 解除する場合はnullを代入
	 */
	public void setLastPk(Timestamp time) {
		_lastPk = time;
	}

	/**
	 * プレイヤーの最終PK時間を現在の時刻に設定する。
	 */
	public void setLastPk() {
		_lastPk = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * プレイヤーが手配中であるかを返す。
	 * 
	 * @return 手配中であれば、true
	 */
	public boolean isWanted() {
		if (_lastPk == null) {
			return false;
		}
		else if (System.currentTimeMillis() - _lastPk.getTime() > 24 * 3600 * 1000) {
			setLastPk(null);
			return false;
		}
		return true;
	}

	private Timestamp _lastPkForElf;

	public Timestamp getLastPkForElf() {
		return _lastPkForElf;
	}

	public void setLastPkForElf(Timestamp time) {
		_lastPkForElf = time;
	}

	public void setLastPkForElf() {
		_lastPkForElf = new Timestamp(System.currentTimeMillis());
	}

	public boolean isWantedForElf() {
		if (_lastPkForElf == null) {
			return false;
		}
		else if (System.currentTimeMillis() - _lastPkForElf.getTime() > 24 * 3600 * 1000) {
			setLastPkForElf(null);
			return false;
		}
		return true;
	}

	private Timestamp _deleteTime; // キャラクター削除までの時間

	public Timestamp getDeleteTime() {
		return _deleteTime;
	}

	public void setDeleteTime(Timestamp time) {
		_deleteTime = time;
	}

	@Override
	public int getMagicLevel() {
		return getClassFeature().getMagicLevel(getLevel());
	}

	private int _weightReduction = 0;

	public int getWeightReduction() {
		return _weightReduction;
	}

	public void addWeightReduction(int i) {
		_weightReduction += i;
	}

	private int _originalStrWeightReduction = 0; // ● オリジナルSTR 重量軽減

	public int getOriginalStrWeightReduction() {

		return _originalStrWeightReduction;
	}

	private int _originalConWeightReduction = 0; // ● オリジナルCON 重量軽減

	public int getOriginalConWeightReduction() {

		return _originalConWeightReduction;
	}

	private int _hasteItemEquipped = 0;

	public int getHasteItemEquipped() {
		return _hasteItemEquipped;
	}

	public void addHasteItemEquipped(int i) {
		_hasteItemEquipped += i;
	}

	public void removeHasteSkillEffect() {
		if (hasSkillEffect(SLOW)) {
			removeSkillEffect(SLOW);
		}
		if (hasSkillEffect(MASS_SLOW)) {
			removeSkillEffect(MASS_SLOW);
		}
		if (hasSkillEffect(ENTANGLE)) {
			removeSkillEffect(ENTANGLE);
		}
		if (hasSkillEffect(HASTE)) {
			removeSkillEffect(HASTE);
		}
		if (hasSkillEffect(GREATER_HASTE)) {
			removeSkillEffect(GREATER_HASTE);
		}
		if (hasSkillEffect(STATUS_HASTE)) {
			removeSkillEffect(STATUS_HASTE);
		}
	}

	private int _damageReductionByArmor = 0; // 防具によるダメージ軽減

	public int getDamageReductionByArmor() {
		return _damageReductionByArmor;
	}

	public void addDamageReductionByArmor(int i) {
		_damageReductionByArmor += i;
	}

	private int _hitModifierByArmor = 0; // 防具による命中率補正

	public int getHitModifierByArmor() {
		return _hitModifierByArmor;
	}

	public void addHitModifierByArmor(int i) {
		_hitModifierByArmor += i;
	}

	private int _dmgModifierByArmor = 0; // 防具によるダメージ補正

	public int getDmgModifierByArmor() {
		return _dmgModifierByArmor;
	}

	public void addDmgModifierByArmor(int i) {
		_dmgModifierByArmor += i;
	}

	private int _bowHitModifierByArmor = 0; // 防具による弓の命中率補正

	public int getBowHitModifierByArmor() {
		return _bowHitModifierByArmor;
	}

	public void addBowHitModifierByArmor(int i) {
		_bowHitModifierByArmor += i;
	}

	private int _bowDmgModifierByArmor = 0; // 防具による弓のダメージ補正

	public int getBowDmgModifierByArmor() {
		return _bowDmgModifierByArmor;
	}

	public void addBowDmgModifierByArmor(int i) {
		_bowDmgModifierByArmor += i;
	}

	private boolean _gresValid; // G-RESが有効か

	private void setGresValid(boolean valid) {
		_gresValid = valid;
	}

	public boolean isGresValid() {
		return _gresValid;
	}

	private long _fishingTime = 0;

	public long getFishingTime() {
		return _fishingTime;
	}

	public void setFishingTime(long i) {
		_fishingTime = i;
	}

	private boolean _isFishing = false;

	public boolean isFishing() {
		return _isFishing;
	}

	public void setFishing(boolean flag) {
		_isFishing = flag;
	}

	private boolean _isFishingReady = false;

	public boolean isFishingReady() {
		return _isFishingReady;
	}

	public void setFishingReady(boolean flag) {
		_isFishingReady = flag;
	}

	private int _cookingId = 0;

	public int getCookingId() {
		return _cookingId;
	}

	public void setCookingId(int i) {
		_cookingId = i;
	}

	private int _dessertId = 0;

	public int getDessertId() {
		return _dessertId;
	}

	public void setDessertId(int i) {
		_dessertId = i;
	}

	/**
	 * LVによる命中ボーナスを設定する LVが変動した場合などに呼び出せば再計算される
	 * 
	 * @return
	 */
	public void resetBaseDmgup() {
		int newBaseDmgup = 0;
		int newBaseBowDmgup = 0;
		if (isKnight() || isDarkelf() || isDragonKnight() || isCrown()) { // ナイト、ダークエルフ、ドラゴンナイト
			newBaseDmgup = getLevel() / 10;
			newBaseBowDmgup = 0;
		}
		else if (isElf()) { // エルフ
			newBaseDmgup = 0;
			newBaseBowDmgup = getLevel() / 10;
		}
		addDmgup(newBaseDmgup - _baseDmgup);
		addBowDmgup(newBaseBowDmgup - _baseBowDmgup);
		_baseDmgup = newBaseDmgup;
		_baseBowDmgup = newBaseBowDmgup;
	}

	/**
	 * LVによる命中ボーナスを設定する LVが変動した場合などに呼び出せば再計算される
	 * 
	 * @return
	 */
	public void resetBaseHitup() {
		int newBaseHitup = 0;
		int newBaseBowHitup = 0;
		if (isCrown()) { // プリ
			newBaseHitup = getLevel() / 5;
			newBaseBowHitup = getLevel() / 5;
		}
		else if (isKnight()) { // ナイト
			newBaseHitup = getLevel() / 3;
			newBaseBowHitup = getLevel() / 3;
		}
		else if (isElf()) { // エルフ
			newBaseHitup = getLevel() / 5;
			newBaseBowHitup = getLevel() / 5;
		}
		else if (isDarkelf()) { // ダークエルフ
			newBaseHitup = getLevel() / 3;
			newBaseBowHitup = getLevel() / 3;
		}
		else if (isDragonKnight()) { // ドラゴンナイト
			newBaseHitup = getLevel() / 3;
			newBaseBowHitup = getLevel() / 3;
		}
		else if (isIllusionist()) { // イリュージョニスト
			newBaseHitup = getLevel() / 5;
			newBaseBowHitup = getLevel() / 5;
		}
		addHitup(newBaseHitup - _baseHitup);
		addBowHitup(newBaseBowHitup - _baseBowHitup);
		_baseHitup = newBaseHitup;
		_baseBowHitup = newBaseBowHitup;
	}

	/**
	 * キャラクターステータスからACを再計算して設定する 初期設定時、LVUP,LVDown時などに呼び出す
	 */
	public void resetBaseAc() {
		int newAc = CalcStat.calcAc(getLevel(), getBaseDex());
		addAc(newAc - _baseAc);
		_baseAc = newAc;
	}

	/**
	 * キャラクターステータスから素のMRを再計算して設定する 初期設定時、スキル使用時やLVUP,LVDown時に呼び出す
	 */
	public void resetBaseMr() {
		int newMr = 0;
		if (isCrown()) { // プリ
			newMr = 10;
		}
		else if (isElf()) { // エルフ
			newMr = 25;
		}
		else if (isWizard()) { // ウィザード
			newMr = 15;
		}
		else if (isDarkelf()) { // ダークエルフ
			newMr = 10;
		}
		else if (isDragonKnight()) { // ドラゴンナイト
			newMr = 18;
		}
		else if (isIllusionist()) { // イリュージョニスト
			newMr = 20;
		}
		newMr += CalcStat.calcStatMr(getWis()); // WIS分のMRボーナス
		newMr += getLevel() / 2; // LVの半分だけ追加
		addMr(newMr - _baseMr);
		_baseMr = newMr;
	}

	/**
	 * EXPから現在のLvを再計算して設定する 初期設定時、死亡時やLVUP時に呼び出す
	 */
	public void resetLevel() {
		setLevel(ExpTable.getLevelByExp(_exp));

		if (_hpRegen != null) {
			_hpRegen.updateLevel();
		}
	}

	/**
	 * 初期ステータスから現在のボーナスを再計算して設定する 初期設定時、再配分時に呼び出す
	 */
	public void resetOriginalHpup() {
		int originalCon = getOriginalCon();
		if (isCrown()) {
			if ((originalCon == 12) || (originalCon == 13)) {
				_originalHpup = 1;
			}
			else if ((originalCon == 14) || (originalCon == 15)) {
				_originalHpup = 2;
			}
			else if (originalCon >= 16) {
				_originalHpup = 3;
			}
			else {
				_originalHpup = 0;
			}
		}
		else if (isKnight()) {
			if ((originalCon == 15) || (originalCon == 16)) {
				_originalHpup = 1;
			}
			else if (originalCon >= 17) {
				_originalHpup = 3;
			}
			else {
				_originalHpup = 0;
			}
		}
		else if (isElf()) {
			if ((originalCon >= 13) && (originalCon <= 17)) {
				_originalHpup = 1;
			}
			else if (originalCon == 18) {
				_originalHpup = 2;
			}
			else {
				_originalHpup = 0;
			}
		}
		else if (isDarkelf()) {
			if ((originalCon == 10) || (originalCon == 11)) {
				_originalHpup = 1;
			}
			else if (originalCon >= 12) {
				_originalHpup = 2;
			}
			else {
				_originalHpup = 0;
			}
		}
		else if (isWizard()) {
			if ((originalCon == 14) || (originalCon == 15)) {
				_originalHpup = 1;
			}
			else if (originalCon >= 16) {
				_originalHpup = 2;
			}
			else {
				_originalHpup = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalCon == 15) || (originalCon == 16)) {
				_originalHpup = 1;
			}
			else if (originalCon >= 17) {
				_originalHpup = 3;
			}
			else {
				_originalHpup = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalCon == 13) || (originalCon == 14)) {
				_originalHpup = 1;
			}
			else if (originalCon >= 15) {
				_originalHpup = 2;
			}
			else {
				_originalHpup = 0;
			}
		}
	}

	public void resetOriginalMpup() {
		int originalWis = getOriginalWis();
		{
			if (isCrown()) {
				if (originalWis >= 16) {
					_originalMpup = 1;
				}
				else {
					_originalMpup = 0;
				}
			}
			else if (isKnight()) {
				_originalMpup = 0;
			}
			else if (isElf()) {
				if ((originalWis >= 14) && (originalWis <= 16)) {
					_originalMpup = 1;
				}
				else if (originalWis >= 17) {
					_originalMpup = 2;
				}
				else {
					_originalMpup = 0;
				}
			}
			else if (isDarkelf()) {
				if (originalWis >= 12) {
					_originalMpup = 1;
				}
				else {
					_originalMpup = 0;
				}
			}
			else if (isWizard()) {
				if ((originalWis >= 13) && (originalWis <= 16)) {
					_originalMpup = 1;
				}
				else if (originalWis >= 17) {
					_originalMpup = 2;
				}
				else {
					_originalMpup = 0;
				}
			}
			else if (isDragonKnight()) {
				if ((originalWis >= 13) && (originalWis <= 15)) {
					_originalMpup = 1;
				}
				else if (originalWis >= 16) {
					_originalMpup = 2;
				}
				else {
					_originalMpup = 0;
				}
			}
			else if (isIllusionist()) {
				if ((originalWis >= 13) && (originalWis <= 15)) {
					_originalMpup = 1;
				}
				else if (originalWis >= 16) {
					_originalMpup = 2;
				}
				else {
					_originalMpup = 0;
				}
			}
		}
	}

	public void resetOriginalStrWeightReduction() {
		int originalStr = getOriginalStr();
		if (isCrown()) {
			if ((originalStr >= 14) && (originalStr <= 16)) {
				_originalStrWeightReduction = 1;
			}
			else if ((originalStr >= 17) && (originalStr <= 19)) {
				_originalStrWeightReduction = 2;
			}
			else if (originalStr == 20) {
				_originalStrWeightReduction = 3;
			}
			else {
				_originalStrWeightReduction = 0;
			}
		}
		else if (isKnight()) {
			_originalStrWeightReduction = 0;
		}
		else if (isElf()) {
			if (originalStr >= 16) {
				_originalStrWeightReduction = 2;
			}
			else {
				_originalStrWeightReduction = 0;
			}
		}
		else if (isDarkelf()) {
			if ((originalStr >= 13) && (originalStr <= 15)) {
				_originalStrWeightReduction = 2;
			}
			else if (originalStr >= 16) {
				_originalStrWeightReduction = 3;
			}
			else {
				_originalStrWeightReduction = 0;
			}
		}
		else if (isWizard()) {
			if (originalStr >= 9) {
				_originalStrWeightReduction = 1;
			}
			else {
				_originalStrWeightReduction = 0;
			}
		}
		else if (isDragonKnight()) {
			if (originalStr >= 16) {
				_originalStrWeightReduction = 1;
			}
			else {
				_originalStrWeightReduction = 0;
			}
		}
		else if (isIllusionist()) {
			if (originalStr == 18) {
				_originalStrWeightReduction = 1;
			}
			else {
				_originalStrWeightReduction = 0;
			}
		}
	}

	public void resetOriginalDmgup() {
		int originalStr = getOriginalStr();
		if (isCrown()) {
			if ((originalStr >= 15) && (originalStr <= 17)) {
				_originalDmgup = 1;
			}
			else if (originalStr >= 18) {
				_originalDmgup = 2;
			}
			else {
				_originalDmgup = 0;
			}
		}
		else if (isKnight()) {
			if ((originalStr == 18) || (originalStr == 19)) {
				_originalDmgup = 2;
			}
			else if (originalStr == 20) {
				_originalDmgup = 4;
			}
			else {
				_originalDmgup = 0;
			}
		}
		else if (isElf()) {
			if ((originalStr == 12) || (originalStr == 13)) {
				_originalDmgup = 1;
			}
			else if (originalStr >= 14) {
				_originalDmgup = 2;
			}
			else {
				_originalDmgup = 0;
			}
		}
		else if (isDarkelf()) {
			if ((originalStr >= 14) && (originalStr <= 17)) {
				_originalDmgup = 1;
			}
			else if (originalStr == 18) {
				_originalDmgup = 2;
			}
			else {
				_originalDmgup = 0;
			}
		}
		else if (isWizard()) {
			if ((originalStr == 10) || (originalStr == 11)) {
				_originalDmgup = 1;
			}
			else if (originalStr >= 12) {
				_originalDmgup = 2;
			}
			else {
				_originalDmgup = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalStr >= 15) && (originalStr <= 17)) {
				_originalDmgup = 1;
			}
			else if (originalStr >= 18) {
				_originalDmgup = 3;
			}
			else {
				_originalDmgup = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalStr == 13) || (originalStr == 14)) {
				_originalDmgup = 1;
			}
			else if (originalStr >= 15) {
				_originalDmgup = 2;
			}
			else {
				_originalDmgup = 0;
			}
		}
	}

	public void resetOriginalConWeightReduction() {
		int originalCon = getOriginalCon();
		if (isCrown()) {
			if (originalCon >= 11) {
				_originalConWeightReduction = 1;
			}
			else {
				_originalConWeightReduction = 0;
			}
		}
		else if (isKnight()) {
			if (originalCon >= 15) {
				_originalConWeightReduction = 1;
			}
			else {
				_originalConWeightReduction = 0;
			}
		}
		else if (isElf()) {
			if (originalCon >= 15) {
				_originalConWeightReduction = 2;
			}
			else {
				_originalConWeightReduction = 0;
			}
		}
		else if (isDarkelf()) {
			if (originalCon >= 9) {
				_originalConWeightReduction = 1;
			}
			else {
				_originalConWeightReduction = 0;
			}
		}
		else if (isWizard()) {
			if ((originalCon == 13) || (originalCon == 14)) {
				_originalConWeightReduction = 1;
			}
			else if (originalCon >= 15) {
				_originalConWeightReduction = 2;
			}
			else {
				_originalConWeightReduction = 0;
			}
		}
		else if (isDragonKnight()) {
			_originalConWeightReduction = 0;
		}
		else if (isIllusionist()) {
			if (originalCon == 17) {
				_originalConWeightReduction = 1;
			}
			else if (originalCon == 18) {
				_originalConWeightReduction = 2;
			}
			else {
				_originalConWeightReduction = 0;
			}
		}
	}

	public void resetOriginalBowDmgup() {
		int originalDex = getOriginalDex();
		if (isCrown()) {
			if (originalDex >= 13) {
				_originalBowDmgup = 1;
			}
			else {
				_originalBowDmgup = 0;
			}
		}
		else if (isKnight()) {
			_originalBowDmgup = 0;
		}
		else if (isElf()) {
			if ((originalDex >= 14) && (originalDex <= 16)) {
				_originalBowDmgup = 2;
			}
			else if (originalDex >= 17) {
				_originalBowDmgup = 3;
			}
			else {
				_originalBowDmgup = 0;
			}
		}
		else if (isDarkelf()) {
			if (originalDex == 18) {
				_originalBowDmgup = 2;
			}
			else {
				_originalBowDmgup = 0;
			}
		}
		else if (isWizard()) {
			_originalBowDmgup = 0;
		}
		else if (isDragonKnight()) {
			_originalBowDmgup = 0;
		}
		else if (isIllusionist()) {
			_originalBowDmgup = 0;
		}
	}

	public void resetOriginalHitup() {
		int originalStr = getOriginalStr();
		if (isCrown()) {
			if ((originalStr >= 16) && (originalStr <= 18)) {
				_originalHitup = 1;
			}
			else if (originalStr >= 19) {
				_originalHitup = 2;
			}
			else {
				_originalHitup = 0;
			}
		}
		else if (isKnight()) {
			if ((originalStr == 17) || (originalStr == 18)) {
				_originalHitup = 2;
			}
			else if (originalStr >= 19) {
				_originalHitup = 4;
			}
			else {
				_originalHitup = 0;
			}
		}
		else if (isElf()) {
			if ((originalStr == 13) || (originalStr == 14)) {
				_originalHitup = 1;
			}
			else if (originalStr >= 15) {
				_originalHitup = 2;
			}
			else {
				_originalHitup = 0;
			}
		}
		else if (isDarkelf()) {
			if ((originalStr >= 15) && (originalStr <= 17)) {
				_originalHitup = 1;
			}
			else if (originalStr == 18) {
				_originalHitup = 2;
			}
			else {
				_originalHitup = 0;
			}
		}
		else if (isWizard()) {
			if ((originalStr == 11) || (originalStr == 12)) {
				_originalHitup = 1;
			}
			else if (originalStr >= 13) {
				_originalHitup = 2;
			}
			else {
				_originalHitup = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalStr >= 14) && (originalStr <= 16)) {
				_originalHitup = 1;
			}
			else if (originalStr >= 17) {
				_originalHitup = 3;
			}
			else {
				_originalHitup = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalStr == 12) || (originalStr == 13)) {
				_originalHitup = 1;
			}
			else if ((originalStr == 14) || (originalStr == 15)) {
				_originalHitup = 2;
			}
			else if (originalStr == 16) {
				_originalHitup = 3;
			}
			else if (originalStr >= 17) {
				_originalHitup = 4;
			}
			else {
				_originalHitup = 0;
			}
		}
	}

	public void resetOriginalBowHitup() {
		int originalDex = getOriginalDex();
		if (isCrown()) {
			_originalBowHitup = 0;
		}
		else if (isKnight()) {
			_originalBowHitup = 0;
		}
		else if (isElf()) {
			if ((originalDex >= 13) && (originalDex <= 15)) {
				_originalBowHitup = 2;
			}
			else if (originalDex >= 16) {
				_originalBowHitup = 3;
			}
			else {
				_originalBowHitup = 0;
			}
		}
		else if (isDarkelf()) {
			if (originalDex == 17) {
				_originalBowHitup = 1;
			}
			else if (originalDex == 18) {
				_originalBowHitup = 2;
			}
			else {
				_originalBowHitup = 0;
			}
		}
		else if (isWizard()) {
			_originalBowHitup = 0;
		}
		else if (isDragonKnight()) {
			_originalBowHitup = 0;
		}
		else if (isIllusionist()) {
			_originalBowHitup = 0;
		}
	}

	public void resetOriginalMr() {
		int originalWis = getOriginalWis();
		if (isCrown()) {
			if ((originalWis == 12) || (originalWis == 13)) {
				_originalMr = 1;
			}
			else if (originalWis >= 14) {
				_originalMr = 2;
			}
			else {
				_originalMr = 0;
			}
		}
		else if (isKnight()) {
			if ((originalWis == 10) || (originalWis == 11)) {
				_originalMr = 1;
			}
			else if (originalWis >= 12) {
				_originalMr = 2;
			}
			else {
				_originalMr = 0;
			}
		}
		else if (isElf()) {
			if ((originalWis >= 13) && (originalWis <= 15)) {
				_originalMr = 1;
			}
			else if (originalWis >= 16) {
				_originalMr = 2;
			}
			else {
				_originalMr = 0;
			}
		}
		else if (isDarkelf()) {
			if ((originalWis >= 11) && (originalWis <= 13)) {
				_originalMr = 1;
			}
			else if (originalWis == 14) {
				_originalMr = 2;
			}
			else if (originalWis == 15) {
				_originalMr = 3;
			}
			else if (originalWis >= 16) {
				_originalMr = 4;
			}
			else {
				_originalMr = 0;
			}
		}
		else if (isWizard()) {
			if (originalWis >= 15) {
				_originalMr = 1;
			}
			else {
				_originalMr = 0;
			}
		}
		else if (isDragonKnight()) {
			if (originalWis >= 14) {
				_originalMr = 2;
			}
			else {
				_originalMr = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalWis >= 15) && (originalWis <= 17)) {
				_originalMr = 2;
			}
			else if (originalWis == 18) {
				_originalMr = 4;
			}
			else {
				_originalMr = 0;
			}
		}

		addMr(_originalMr);
	}

	public void resetOriginalMagicHit() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
			if ((originalInt == 12) || (originalInt == 13)) {
				_originalMagicHit = 1;
			}
			else if (originalInt >= 14) {
				_originalMagicHit = 2;
			}
			else {
				_originalMagicHit = 0;
			}
		}
		else if (isKnight()) {
			if ((originalInt == 10) || (originalInt == 11)) {
				_originalMagicHit = 1;
			}
			else if (originalInt == 12) {
				_originalMagicHit = 2;
			}
			else {
				_originalMagicHit = 0;
			}
		}
		else if (isElf()) {
			if ((originalInt == 13) || (originalInt == 14)) {
				_originalMagicHit = 1;
			}
			else if (originalInt >= 15) {
				_originalMagicHit = 2;
			}
			else {
				_originalMagicHit = 0;
			}
		}
		else if (isDarkelf()) {
			if ((originalInt == 12) || (originalInt == 13)) {
				_originalMagicHit = 1;
			}
			else if (originalInt >= 14) {
				_originalMagicHit = 2;
			}
			else {
				_originalMagicHit = 0;
			}
		}
		else if (isWizard()) {
			if (originalInt >= 14) {
				_originalMagicHit = 1;
			}
			else {
				_originalMagicHit = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalInt == 12) || (originalInt == 13)) {
				_originalMagicHit = 2;
			}
			else if ((originalInt == 14) || (originalInt == 15)) {
				_originalMagicHit = 3;
			}
			else if (originalInt >= 16) {
				_originalMagicHit = 4;
			}
			else {
				_originalMagicHit = 0;
			}
		}
		else if (isIllusionist()) {
			if (originalInt >= 13) {
				_originalMagicHit = 1;
			}
			else {
				_originalMagicHit = 0;
			}
		}
	}

	public void resetOriginalMagicCritical() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
			_originalMagicCritical = 0;
		}
		else if (isKnight()) {
			_originalMagicCritical = 0;
		}
		else if (isElf()) {
			if ((originalInt == 14) || (originalInt == 15)) {
				_originalMagicCritical = 2;
			}
			else if (originalInt >= 16) {
				_originalMagicCritical = 4;
			}
			else {
				_originalMagicCritical = 0;
			}
		}
		else if (isDarkelf()) {
			_originalMagicCritical = 0;
		}
		else if (isWizard()) {
			if (originalInt == 15) {
				_originalMagicCritical = 2;
			}
			else if (originalInt == 16) {
				_originalMagicCritical = 4;
			}
			else if (originalInt == 17) {
				_originalMagicCritical = 6;
			}
			else if (originalInt == 18) {
				_originalMagicCritical = 8;
			}
			else {
				_originalMagicCritical = 0;
			}
		}
		else if (isDragonKnight()) {
			_originalMagicCritical = 0;
		}
		else if (isIllusionist()) {
			_originalMagicCritical = 0;
		}
	}

	public void resetOriginalMagicConsumeReduction() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
			if ((originalInt == 11) || (originalInt == 12)) {
				_originalMagicConsumeReduction = 1;
			}
			else if (originalInt >= 13) {
				_originalMagicConsumeReduction = 2;
			}
			else {
				_originalMagicConsumeReduction = 0;
			}
		}
		else if (isKnight()) {
			if ((originalInt == 9) || (originalInt == 10)) {
				_originalMagicConsumeReduction = 1;
			}
			else if (originalInt >= 11) {
				_originalMagicConsumeReduction = 2;
			}
			else {
				_originalMagicConsumeReduction = 0;
			}
		}
		else if (isElf()) {
			_originalMagicConsumeReduction = 0;
		}
		else if (isDarkelf()) {
			if ((originalInt == 13) || (originalInt == 14)) {
				_originalMagicConsumeReduction = 1;
			}
			else if (originalInt >= 15) {
				_originalMagicConsumeReduction = 2;
			}
			else {
				_originalMagicConsumeReduction = 0;
			}
		}
		else if (isWizard()) {
			_originalMagicConsumeReduction = 0;
		}
		else if (isDragonKnight()) {
			_originalMagicConsumeReduction = 0;
		}
		else if (isIllusionist()) {
			if (originalInt == 14) {
				_originalMagicConsumeReduction = 1;
			}
			else if (originalInt >= 15) {
				_originalMagicConsumeReduction = 2;
			}
			else {
				_originalMagicConsumeReduction = 0;
			}
		}
	}

	public void resetOriginalMagicDamage() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
			_originalMagicDamage = 0;
		}
		else if (isKnight()) {
			_originalMagicDamage = 0;
		}
		else if (isElf()) {
			_originalMagicDamage = 0;
		}
		else if (isDarkelf()) {
			_originalMagicDamage = 0;
		}
		else if (isWizard()) {
			if (originalInt >= 13) {
				_originalMagicDamage = 1;
			}
			else {
				_originalMagicDamage = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalInt == 13) || (originalInt == 14)) {
				_originalMagicDamage = 1;
			}
			else if ((originalInt == 15) || (originalInt == 16)) {
				_originalMagicDamage = 2;
			}
			else if (originalInt == 17) {
				_originalMagicDamage = 3;
			}
			else {
				_originalMagicDamage = 0;
			}
		}
		else if (isIllusionist()) {
			if (originalInt == 16) {
				_originalMagicDamage = 1;
			}
			else if (originalInt == 17) {
				_originalMagicDamage = 2;
			}
			else {
				_originalMagicDamage = 0;
			}
		}
	}

	public void resetOriginalAc() {
		int originalDex = getOriginalDex();
		if (isCrown()) {
			if ((originalDex >= 12) && (originalDex <= 14)) {
				_originalAc = 1;
			}
			else if ((originalDex == 15) || (originalDex == 16)) {
				_originalAc = 2;
			}
			else if (originalDex >= 17) {
				_originalAc = 3;
			}
			else {
				_originalAc = 0;
			}
		}
		else if (isKnight()) {
			if ((originalDex == 13) || (originalDex == 14)) {
				_originalAc = 1;
			}
			else if (originalDex >= 15) {
				_originalAc = 3;
			}
			else {
				_originalAc = 0;
			}
		}
		else if (isElf()) {
			if ((originalDex >= 15) && (originalDex <= 17)) {
				_originalAc = 1;
			}
			else if (originalDex == 18) {
				_originalAc = 2;
			}
			else {
				_originalAc = 0;
			}
		}
		else if (isDarkelf()) {
			if (originalDex >= 17) {
				_originalAc = 1;
			}
			else {
				_originalAc = 0;
			}
		}
		else if (isWizard()) {
			if ((originalDex == 8) || (originalDex == 9)) {
				_originalAc = 1;
			}
			else if (originalDex >= 10) {
				_originalAc = 2;
			}
			else {
				_originalAc = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalDex == 12) || (originalDex == 13)) {
				_originalAc = 1;
			}
			else if (originalDex >= 14) {
				_originalAc = 2;
			}
			else {
				_originalAc = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalDex == 11) || (originalDex == 12)) {
				_originalAc = 1;
			}
			else if (originalDex >= 13) {
				_originalAc = 2;
			}
			else {
				_originalAc = 0;
			}
		}

		addAc(0 - _originalAc);
	}

	public void resetOriginalEr() {
		int originalDex = getOriginalDex();
		if (isCrown()) {
			if ((originalDex == 14) || (originalDex == 15)) {
				_originalEr = 1;
			}
			else if ((originalDex == 16) || (originalDex == 17)) {
				_originalEr = 2;
			}
			else if (originalDex == 18) {
				_originalEr = 3;
			}
			else {
				_originalEr = 0;
			}
		}
		else if (isKnight()) {
			if ((originalDex == 14) || (originalDex == 15)) {
				_originalEr = 1;
			}
			else if (originalDex == 16) {
				_originalEr = 3;
			}
			else {
				_originalEr = 0;
			}
		}
		else if (isElf()) {
			_originalEr = 0;
		}
		else if (isDarkelf()) {
			if (originalDex >= 16) {
				_originalEr = 2;
			}
			else {
				_originalEr = 0;
			}
		}
		else if (isWizard()) {
			if ((originalDex == 9) || (originalDex == 10)) {
				_originalEr = 1;
			}
			else if (originalDex == 11) {
				_originalEr = 2;
			}
			else {
				_originalEr = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalDex == 13) || (originalDex == 14)) {
				_originalEr = 1;
			}
			else if (originalDex >= 15) {
				_originalEr = 2;
			}
			else {
				_originalEr = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalDex == 12) || (originalDex == 13)) {
				_originalEr = 1;
			}
			else if (originalDex >= 14) {
				_originalEr = 2;
			}
			else {
				_originalEr = 0;
			}
		}
	}

	public void resetOriginalHpr() {
		int originalCon = getOriginalCon();
		if (isCrown()) {
			if ((originalCon == 13) || (originalCon == 14)) {
				_originalHpr = 1;
			}
			else if ((originalCon == 15) || (originalCon == 16)) {
				_originalHpr = 2;
			}
			else if (originalCon == 17) {
				_originalHpr = 3;
			}
			else if (originalCon == 18) {
				_originalHpr = 4;
			}
			else {
				_originalHpr = 0;
			}
		}
		else if (isKnight()) {
			if ((originalCon == 16) || (originalCon == 17)) {
				_originalHpr = 2;
			}
			else if (originalCon == 18) {
				_originalHpr = 4;
			}
			else {
				_originalHpr = 0;
			}
		}
		else if (isElf()) {
			if ((originalCon == 14) || (originalCon == 15)) {
				_originalHpr = 1;
			}
			else if (originalCon == 16) {
				_originalHpr = 2;
			}
			else if (originalCon >= 17) {
				_originalHpr = 3;
			}
			else {
				_originalHpr = 0;
			}
		}
		else if (isDarkelf()) {
			if ((originalCon == 11) || (originalCon == 12)) {
				_originalHpr = 1;
			}
			else if (originalCon >= 13) {
				_originalHpr = 2;
			}
			else {
				_originalHpr = 0;
			}
		}
		else if (isWizard()) {
			if (originalCon == 17) {
				_originalHpr = 1;
			}
			else if (originalCon == 18) {
				_originalHpr = 2;
			}
			else {
				_originalHpr = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalCon == 16) || (originalCon == 17)) {
				_originalHpr = 1;
			}
			else if (originalCon == 18) {
				_originalHpr = 3;
			}
			else {
				_originalHpr = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalCon == 14) || (originalCon == 15)) {
				_originalHpr = 1;
			}
			else if (originalCon >= 16) {
				_originalHpr = 2;
			}
			else {
				_originalHpr = 0;
			}
		}
	}

	public void resetOriginalMpr() {
		int originalWis = getOriginalWis();
		if (isCrown()) {
			if ((originalWis == 13) || (originalWis == 14)) {
				_originalMpr = 1;
			}
			else if (originalWis >= 15) {
				_originalMpr = 2;
			}
			else {
				_originalMpr = 0;
			}
		}
		else if (isKnight()) {
			if ((originalWis == 11) || (originalWis == 12)) {
				_originalMpr = 1;
			}
			else if (originalWis == 13) {
				_originalMpr = 2;
			}
			else {
				_originalMpr = 0;
			}
		}
		else if (isElf()) {
			if ((originalWis >= 15) && (originalWis <= 17)) {
				_originalMpr = 1;
			}
			else if (originalWis == 18) {
				_originalMpr = 2;
			}
			else {
				_originalMpr = 0;
			}
		}
		else if (isDarkelf()) {
			if (originalWis >= 13) {
				_originalMpr = 1;
			}
			else {
				_originalMpr = 0;
			}
		}
		else if (isWizard()) {
			if ((originalWis == 14) || (originalWis == 15)) {
				_originalMpr = 1;
			}
			else if ((originalWis == 16) || (originalWis == 17)) {
				_originalMpr = 2;
			}
			else if (originalWis == 18) {
				_originalMpr = 3;
			}
			else {
				_originalMpr = 0;
			}
		}
		else if (isDragonKnight()) {
			if ((originalWis == 15) || (originalWis == 16)) {
				_originalMpr = 1;
			}
			else if (originalWis >= 17) {
				_originalMpr = 2;
			}
			else {
				_originalMpr = 0;
			}
		}
		else if (isIllusionist()) {
			if ((originalWis >= 14) && (originalWis <= 16)) {
				_originalMpr = 1;
			}
			else if (originalWis >= 17) {
				_originalMpr = 2;
			}
			else {
				_originalMpr = 0;
			}
		}
	}

	public void refresh() {
		resetLevel();
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseMr();
		resetBaseAc();
		resetOriginalHpup();
		resetOriginalMpup();
		resetOriginalDmgup();
		resetOriginalBowDmgup();
		resetOriginalHitup();
		resetOriginalBowHitup();
		resetOriginalMr();
		resetOriginalMagicHit();
		resetOriginalMagicCritical();
		resetOriginalMagicConsumeReduction();
		resetOriginalMagicDamage();
		resetOriginalAc();
		resetOriginalEr();
		resetOriginalHpr();
		resetOriginalMpr();
		resetOriginalStrWeightReduction();
		resetOriginalConWeightReduction();
	}

	public void startRefreshParty() {// 組隊更新 3.3C

		final int INTERVAL = 25000;

		if (!_rpActive) {

			_rp = new L1PartyRefresh(this);

			_regenTimer.scheduleAtFixedRate(_rp, INTERVAL, INTERVAL);

			_rpActive = true;

		}

	}

	public void stopRefreshParty() {// 組隊暫停更新 3.3C

		if (_rpActive) {

			_rp.cancel();

			_rp = null;

			_rpActive = false;

		}
	}

	private final L1ExcludingList _excludingList = new L1ExcludingList();

	public L1ExcludingList getExcludingList() {
		return _excludingList;
	}

	// -- 加速器検知機能 --
	private final AcceleratorChecker _acceleratorChecker = new AcceleratorChecker(this);

	public AcceleratorChecker getAcceleratorChecker() {
		return _acceleratorChecker;
	}

	// 使用屠宰者判斷
	private boolean _FoeSlayer = false;

	public void setFoeSlayer(boolean FoeSlayer) {
		_FoeSlayer = FoeSlayer;
	}

	public boolean isFoeSlayer() {
		return _FoeSlayer;
	}

	/**
	 * テレポート先の座標
	 */
	private int _teleportX = 0;

	public int getTeleportX() {
		return _teleportX;
	}

	public void setTeleportX(int i) {
		_teleportX = i;
	}

	private int _teleportY = 0;

	public int getTeleportY() {
		return _teleportY;
	}

	public void setTeleportY(int i) {
		_teleportY = i;
	}

	private short _teleportMapId = 0;

	public short getTeleportMapId() {
		return _teleportMapId;
	}

	public void setTeleportMapId(short i) {
		_teleportMapId = i;
	}

	private int _teleportHeading = 0;

	public int getTeleportHeading() {
		return _teleportHeading;
	}

	public void setTeleportHeading(int i) {
		_teleportHeading = i;
	}

	private int _tempCharGfxAtDead;

	public int getTempCharGfxAtDead() {
		return _tempCharGfxAtDead;
	}

	public void setTempCharGfxAtDead(int i) {
		_tempCharGfxAtDead = i;
	}

	private boolean _isCanWhisper = true;

	public boolean isCanWhisper() {
		return _isCanWhisper;
	}

	public void setCanWhisper(boolean flag) {
		_isCanWhisper = flag;
	}

	
	private boolean _showTradeChat = true;
	public boolean showTradeChat() { return _showTradeChat; }
	
	private boolean _showWorldChat = true;
	public boolean showWorldChat() { return _showWorldChat; }
	
	
	private boolean _isShowTradeChat = true;

	public boolean isShowTradeChat() {
		return _isShowTradeChat;
	}

	public void setShowTradeChat(boolean flag) {
		_isShowTradeChat = flag;
	}

	// 血盟
	private boolean _isShowClanChat = true;

	public boolean isShowClanChat() {
		return _isShowClanChat;
	}

	public void setShowClanChat(boolean flag) {
		_isShowClanChat = flag;
	}

	// 組隊
	private boolean _isShowPartyChat = true;

	public boolean isShowPartyChat() {
		return _isShowPartyChat;
	}

	public void setShowPartyChat(boolean flag) {
		_isShowPartyChat = flag;
	}

	private boolean _isShowWorldChat = true;

	public boolean isShowWorldChat() {
		return _isShowWorldChat;
	}

	public void setShowWorldChat(boolean flag) {
		_isShowWorldChat = flag;
	}

	private int _fightId;

	public int getFightId() {
		return _fightId;
	}

	public void setFightId(int i) {
		_fightId = i;
	}

	// 釣魚點
	private int _fishX;

	public int getFishX() {
		return _fishX;
	}

	public void setFishX(int i) {
		_fishX = i;
	}
	private int _fishY;

	public int getFishY() {
		return _fishY;
	}

	public void setFishY(int i) {
		_fishY = i;
	}

	private byte _chatCount = 0;

	private long _oldChatTimeInMillis = 0L;

	public void checkChatInterval() {
		long nowChatTimeInMillis = System.currentTimeMillis();
		if (_chatCount == 0) {
			_chatCount++;
			_oldChatTimeInMillis = nowChatTimeInMillis;
			return;
		}

		long chatInterval = nowChatTimeInMillis - _oldChatTimeInMillis;
		if (chatInterval > 2000) {
			_chatCount = 0;
			_oldChatTimeInMillis = 0;
		}
		else {
			if (_chatCount >= 3) {
				setSkillEffect(STATUS_CHAT_PROHIBITED, 120 * 1000);
				sendPackets(new S_SkillIconGFX(36, 120));
				sendPackets(new S_ServerMessage(153)); // \f3迷惑なチャット流しをしたので、今後2分間チャットを行うことはできません。
				_chatCount = 0;
				_oldChatTimeInMillis = 0;
			}
			_chatCount++;
		}
	}

	private int _callClanId;

	public int getCallClanId() {
		return _callClanId;
	}

	public void setCallClanId(int i) {
		_callClanId = i;
	}

	private int _callClanHeading;

	public int getCallClanHeading() {
		return _callClanHeading;
	}

	public void setCallClanHeading(int i) {
		_callClanHeading = i;
	}

	private boolean _isInCharReset = false;

	public boolean isInCharReset() {
		return _isInCharReset;
	}

	public void setInCharReset(boolean flag) {
		_isInCharReset = flag;
	}

	private int _tempLevel = 1;

	public int getTempLevel() {
		return _tempLevel;
	}

	public void setTempLevel(int i) {
		_tempLevel = i;
	}

	private int _tempMaxLevel = 1;

	public int getTempMaxLevel() {
		return _tempMaxLevel;
	}

	public void setTempMaxLevel(int i) {
		_tempMaxLevel = i;
	}

	private int _awakeSkillId = 0;

	public int getAwakeSkillId() {
		return _awakeSkillId;
	}

	public void setAwakeSkillId(int i) {
		_awakeSkillId = i;
	}

	private boolean _isSummonMonster = false;

	public void setSummonMonster(boolean SummonMonster) {
		_isSummonMonster = SummonMonster;
	}

	public boolean isSummonMonster() {
		return _isSummonMonster;
	}

	private boolean _isShapeChange = false;

	public void setShapeChange(boolean isShapeChange) {
		_isShapeChange = isShapeChange;
	}

	public boolean isShapeChange() {
		return _isShapeChange;
	}

	public void setPartyType(int type) {
		_partyType = type;
	}

	public int getPartyType() {
		return _partyType;
	}

	
	//----------------------------Player Commands---------------------------------
	private boolean _dropMessages = true;
	public void setDropMessages(final boolean dropMessages) {
		_dropMessages = dropMessages;
	}
	public boolean getDropMessages() { return _dropMessages; }
	
	private boolean _partyDropMessages = true;
	public void setPartyDropMessages(final boolean partyDropMessages) {
		_partyDropMessages = partyDropMessages;
	}
	public boolean getPartyDropMessages() { return _partyDropMessages; }
	
	private boolean _dmgMessages = false;
	public boolean getDmgMessages() { return _dmgMessages; }
	public void setDmgMessages(final boolean dmgMessages) {
		_dmgMessages = dmgMessages;
	}
	
	private boolean _potionMessages = true;
	public boolean getPotionMessages() { return _potionMessages; }
	public void setPotionMessages(final boolean potionMessages) {
		_potionMessages = potionMessages;
	}
	//----------------------------Player Commands---------------------------------
	
    private TheCryOfSurvival _CryOfSurvival;
    /** 生存的呐喊(开关). */
    private boolean _CryOfSurvivalActive;
    /** 生存的呐喊时间(分). */
    private int _CryTime = 0;
    
    public int getCryTime() {
        return _CryTime;
    }    
    
    public void setCryTime(final int time) {
        _CryTime = time;
    }
    
    /**
     * 生存的呐喊(开始).
     */
    public void startCryOfSurvival() {
        final int INTERVAL = 60000;
        if (!_CryOfSurvivalActive) {
            _CryOfSurvival = new TheCryOfSurvival(this);
            _regenTimer.scheduleAtFixedRate(_CryOfSurvival, INTERVAL,
                    INTERVAL);
            _CryOfSurvivalActive = true;
        }
    }
    
    /**
     * 生存的呐喊(停止).
     */
    public void stopCryOfSurvival() {
        if (_CryOfSurvivalActive) {
            _CryOfSurvival.cancel();
            _CryOfSurvival = null;
            _CryOfSurvivalActive = false;
        }
    }
    
	/**
	 * 最後離線時間.
	 */
	private Timestamp _lastActive;

	/**
	 * 殷海薩的祝福的等級.
	 */
	private boolean _isEinLevel;

	/**
	 * 殷海薩的祝福 -%數.
	 */
	private int _einPoint;

	/**
	 * 殷海薩的祝福 -取回經驗點數的累計.
	 */
	private int _ein_getExp;

	/**
	 * 殷海薩的祝福 -最高百分比.
	 */
	private static final int einMaxPercent = Config_Einhasad.EIN_MAX_PERCENT;
	
	/**
	 * 增加殷海薩的祝福點數
	 * 
	 * @param i
	 */
	public void addEinPoint(final int i) {
		_einPoint += i;
		if (_einPoint < 1) {
			_einPoint = 0;
		} else {
			if (_einPoint > einMaxPercent) {
				_einPoint = einMaxPercent;
			}
		}
		sendPackets(new S_SkillIconEinhasad(_einPoint));
	}
	
	/**
	 * 確認殷海薩的祝福
	 */
	public void checkEinhasad() {
		if (Config_Einhasad.EINHASAD_IS_ACTIVE) {
			setEinLevel();
			if (isMatchEinResult()) { // 49級以上 殷海薩的祝福紀錄
				addEinPoint(0);
			}
		}
	}
	
	/**
	 * 設定殷海薩祝福的等級
	 */
	public void setEinLevel() {
		_isEinLevel = (getLevel() >= 49) ? true : false;
	}
	
	/**
	 * 是否滿足殷海薩祝福的條件
	 * 
	 * @return
	 */
	public boolean isMatchEinResult() {
		if (_isEinLevel) {
			return isEinZone();
		} else {
			return false;
		}
	}
	
	/**
	 * 是否在安全區域
	 * 
	 * @return
	 */
	public boolean isEinZone() {
		return getMap().isSafetyZone(getLocation());
	}
	
	/**
	 * 經驗值累積交換
	 * 
	 * @param i
	 */
	public void CalcExpCostEin(final int i) {
		if (_einPoint < 1) {
			_ein_getExp = 0;
		} else {
			_ein_getExp += i;
			if (_ein_getExp > 5000) {
				addEinPoint(-_ein_getExp / 5000);
				_ein_getExp %= 5000;
			}
		}
	}
	
	public Timestamp getLastActive() {
		return _lastActive;
	}
	
	public void setLastActive() {
		_lastActive = new Timestamp(System.currentTimeMillis());
	}
	
	public void setLastActive(final Timestamp time) {
		_lastActive = time;
	}
	
	public int getEinPoint() {
		return _einPoint;
	}
	
	public boolean isEinLevel() {
		return _isEinLevel;
	}
	
	private int _kill = 0;
	private int _death = 0;
	
	public void setKill(int kill) {
		_kill = kill;
	}
	
	public int getKill() {
		return _kill;
	}
	
	public void setDeath(int death) {
		_death = death;
	}
	
	public int getDeath() {
		return _death;
	}
	
	
	
	/****************************** 戰鬥特化系統 ******************************/
	// 改變戰鬥特化狀態
	public void changeFightType(int oldType, int newType) {
		// 消除既有的戰鬥特化狀態
		switch (oldType) {
			case 1:
				addAc(2);
				addMr(-3);
				sendPackets(new S_Fight(S_Fight.TYPE_JUSTICE_LEVEL1, S_Fight.FLAG_OFF));
				break;

			case 2:
				addAc(4);
				addMr(-6);
				sendPackets(new S_Fight(S_Fight.TYPE_JUSTICE_LEVEL2, S_Fight.FLAG_OFF));
				break;

			case 3:
				addAc(6);
				addMr(-9);
				sendPackets(new S_Fight(S_Fight.TYPE_JUSTICE_LEVEL3, S_Fight.FLAG_OFF));
				break;

			case -1:
				addDmgup(-1);
				addBowDmgup(-1);
				addSp(-1);
				sendPackets(new S_Fight(S_Fight.TYPE_EVIL_LEVEL1, S_Fight.FLAG_OFF));
				break;

			case -2:
				addDmgup(-3);
				addBowDmgup(-3);
				addSp(-2);
				sendPackets(new S_Fight(S_Fight.TYPE_EVIL_LEVEL2, S_Fight.FLAG_OFF));
				break;

			case -3:
				addDmgup(-5);
				addBowDmgup(-5);
				addSp(-3);
				sendPackets(new S_Fight(S_Fight.TYPE_EVIL_LEVEL3, S_Fight.FLAG_OFF));
				break;
		}

		// 增加新的戰鬥特化狀態
		switch (newType) {
			case 1:
				addAc(-2);
				addMr(3);
				sendPackets(new S_Fight(S_Fight.TYPE_JUSTICE_LEVEL1, S_Fight.FLAG_ON));
				break;

			case 2:
				addAc(-4);
				addMr(6);
				sendPackets(new S_Fight(S_Fight.TYPE_JUSTICE_LEVEL2, S_Fight.FLAG_ON));
				break;

			case 3:
				addAc(-6);
				addMr(9);
				sendPackets(new S_Fight(S_Fight.TYPE_JUSTICE_LEVEL3, S_Fight.FLAG_ON));
				break;

			case -1:
				addDmgup(1);
				addBowDmgup(1);
				addSp(1);
				sendPackets(new S_Fight(S_Fight.TYPE_EVIL_LEVEL1, S_Fight.FLAG_ON));
				break;

			case -2:
				addDmgup(3);
				addBowDmgup(3);
				addSp(2);
				sendPackets(new S_Fight(S_Fight.TYPE_EVIL_LEVEL2, S_Fight.FLAG_ON));
				break;

			case -3:
				addDmgup(5);
				addBowDmgup(5);
				addSp(3);
				sendPackets(new S_Fight(S_Fight.TYPE_EVIL_LEVEL3, S_Fight.FLAG_ON));
				break;
		}
	}

	// 確認是否屬於新手, 並設定相關狀態
	public void checkNoviceType() {
		// 判斷是否啟動新手保護系統(遭遇的守護)
		if (!Config.NOVICE_PROTECTION_IS_ACTIVE) {
			return;
		}

		// 判斷目前等級是否已超過新手階段
		if (getLevel() > Config.NOVICE_MAX_LEVEL) {
			// 判斷之前是否具有新手保護狀態
			if (hasSkillEffect(L1SkillId.STATUS_NOVICE)) {
				// 移除新手保護狀態
				removeSkillEffect(L1SkillId.STATUS_NOVICE);

				// 關閉遭遇的守護圖示
				sendPackets(new S_Fight(S_Fight.TYPE_ENCOUNTER, S_Fight.FLAG_OFF));
			}
		}
		else {
			// 判斷是否未具有新手保護狀態
			if (!hasSkillEffect(L1SkillId.STATUS_NOVICE)) {
				// 增加新手保護狀態
				setSkillEffect(L1SkillId.STATUS_NOVICE, 0);

				// 開啟遭遇的守護圖示
				sendPackets(new S_Fight(S_Fight.TYPE_ENCOUNTER, S_Fight.FLAG_ON));
			}
		}
	}
}
