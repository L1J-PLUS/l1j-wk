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

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.WarTimeController;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1NamedSkill;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1MagicDoll;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.Random;

import static l1j.server.server.model.skill.L1SkillId.*;

public class L1Magic {
	private int _calcType;

	private final int PC_PC = 1;

	private final int PC_NPC = 2;

	private final int NPC_PC = 3;

	private final int NPC_NPC = 4;

	private L1Character _target = null;

	private L1PcInstance _pc = null;

	private L1PcInstance _targetPc = null;

	private L1NpcInstance _npc = null;

	private L1NpcInstance _targetNpc = null;

	private int _leverage = 10; // 1/10倍で表現する。

	public void setLeverage(int i) {
		_leverage = i;
	}

	private int getLeverage() {
		return _leverage;
	}

	public L1Magic(L1Character attacker, L1Character target) {
		_target = target;

		if (attacker instanceof L1PcInstance) {
			if (target instanceof L1PcInstance) {
				_calcType = PC_PC;
				_pc = (L1PcInstance) attacker;
				_targetPc = (L1PcInstance) target;
			}
			else {
				_calcType = PC_NPC;
				_pc = (L1PcInstance) attacker;
				_targetNpc = (L1NpcInstance) target;
			}
		}
		else {
			if (target instanceof L1PcInstance) {
				_calcType = NPC_PC;
				_npc = (L1NpcInstance) attacker;
				_targetPc = (L1PcInstance) target;
			}
			else {
				_calcType = NPC_NPC;
				_npc = (L1NpcInstance) attacker;
				_targetNpc = (L1NpcInstance) target;
			}
		}
	}

	private int getMagicLevel() {
		int magicLevel = 0;
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			magicLevel = _pc.getMagicLevel();
		}
		else if ((_calcType == NPC_PC) || (_calcType == NPC_NPC)) {
			magicLevel = _npc.getMagicLevel();
		}
		return magicLevel;
	}

	private int getMagicBonus() {
		int magicBonus = 0;
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			magicBonus = _pc.getMagicBonus();
		}
		else if ((_calcType == NPC_PC) || (_calcType == NPC_NPC)) {
			magicBonus = _npc.getMagicBonus();
		}
		return magicBonus;
	}

	private int getLawful() {
		int lawful = 0;
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			lawful = _pc.getLawful();
		}
		else if ((_calcType == NPC_PC) || (_calcType == NPC_NPC)) {
			lawful = _npc.getLawful();
		}
		return lawful;
	}

	private int getTargetMr() {
		int mr = 0;
		if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
			mr = _targetPc.getMr();
		}
		else {
			mr = _targetNpc.getMr();
		}
		return mr;
	}

	/* ■■■■■■■■■■■■■■ 成功判定 ■■■■■■■■■■■■■ */
	// ●●●● 確率系魔法の成功判定 ●●●●
	// 計算方法
	// 攻撃側ポイント：LV + ((MagicBonus * 3) * 魔法固有係数)
	// 防御側ポイント：((LV / 2) + (MR * 3)) / 2
	// 攻撃成功率：攻撃側ポイント - 防御側ポイント
	public boolean calcProbabilityMagic(int skillId) {
		int probability = 0;
		boolean isSuccess = false;

		// 攻撃者がGM権限の場合100%成功
		if ((_pc != null) && _pc.isGm()) {
			return true;
		}

		// 判斷特定狀態下才可攻擊 NPC
		if ((_calcType == PC_NPC) && (_targetNpc != null)) {
			if (_pc.isAttackMiss(_pc, _targetNpc.getNpcTemplate().get_npcId())) {
				return false;
			}
		}

		if (!checkZone(skillId)) {
			return false;
		}
		if (skillId == CANCELLATION) {
			if ((_calcType == PC_PC) && (_pc != null) && (_targetPc != null)) {
				// 自分自身の場合は100%成功
				if (_pc.getId() == _targetPc.getId()) {
					return true;
				}
				// 同じクランの場合は100%成功
				if ((_pc.getClanid() > 0) && (_pc.getClanid() == _targetPc.getClanid())) {
					return true;
				}
				// 同じパーティの場合は100%成功
				if (_pc.isInParty()) {
					if (_pc.getParty().isMember(_targetPc)) {
						return true;
					}
				}
				// それ以外の場合、セーフティゾーン内では無効
				if ((_pc.getZoneType() == 1) || (_targetPc.getZoneType() == 1)) {
					return false;
				}
			}
			// 対象がNPC、使用者がNPCの場合は100%成功
			if ((_calcType == PC_NPC) || (_calcType == NPC_PC) || (_calcType == NPC_NPC)) {
				return true;
			}
		}

		// アースバインド中はWB、キャンセレーション以外無効
		if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
			if (_targetPc.hasSkillEffect(EARTH_BIND)) {
				if ((skillId != WEAPON_BREAK) && (skillId != CANCELLATION)) {
					return false;
				}
			}
		}
		else {
			if (_targetNpc.hasSkillEffect(EARTH_BIND)) {
				if ((skillId != WEAPON_BREAK) && (skillId != CANCELLATION)) {
					return false;
				}
			}
		}

		probability = calcProbability(skillId);

		int rnd = Random.nextInt(100) + 1;
		if (probability > 90) {
			probability = 90; // 最高成功率を90%とする。
		}

		if (probability >= rnd) {
			isSuccess = true;
		}
		else {
			isSuccess = false;
		}

		// 確率系魔法メッセージ
		if (!Config.ALT_ATKMSG) {
			return isSuccess;
		}
		if (Config.ALT_ATKMSG) {
			if (((_calcType == PC_PC) || (_calcType == PC_NPC)) && !_pc.isGm()) {
				return isSuccess;
			}
			if (((_calcType == PC_PC) || (_calcType == NPC_PC)) && !_targetPc.isGm()) {
				return isSuccess;
			}
		}

		String msg0 = "";
		String msg1 = " 施放魔法 ";
		String msg2 = "";
		String msg3 = "";
		String msg4 = "";

		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) { // アタッカーがＰＣの場合
			msg0 = _pc.getName() + " 對";
		}
		else if (_calcType == NPC_PC) { // アタッカーがＮＰＣの場合
			msg0 = _npc.getName();
		}

		msg2 = "，機率：" + probability + "%";
		if ((_calcType == NPC_PC) || (_calcType == PC_PC)) { // ターゲットがＰＣの場合
			msg4 = _targetPc.getName();
		}
		else if (_calcType == PC_NPC) { // ターゲットがＮＰＣの場合
			msg4 = _targetNpc.getName();
		}
		if (isSuccess == true) {
			msg3 = "成功";
		}
		else {
			msg3 = "失敗";
		}
   
		// 0 4 1 3 2 攻擊者 對 目標 施放魔法 成功/失敗，機率：X%。
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			_pc.sendPackets(new S_ServerMessage(166, msg0, msg1, msg2, msg3, msg4));
		}
		// 攻擊者 施放魔法 成功/失敗，機率：X%。
		else if ((_calcType == NPC_PC)) {
			_targetPc.sendPackets(new S_ServerMessage(166, msg0, msg1, msg2, msg3, null));
		}

		return isSuccess;
	}

	private boolean checkZone(int skillId) {
		if ((_pc != null) && (_targetPc != null)) {
			if ((_pc.getZoneType() == 1) || (_targetPc.getZoneType() == 1)) { // セーフティーゾーン
				if ((skillId == WEAPON_BREAK) || (skillId == SLOW) || (skillId == CURSE_PARALYZE) || (skillId == MANA_DRAIN) || (skillId == DARKNESS)
						|| (skillId == WEAKNESS) || (skillId == DISEASE) || (skillId == DECAY_POTION) || (skillId == MASS_SLOW)
						|| (skillId == ENTANGLE) || (skillId == ERASE_MAGIC) || (skillId == EARTH_BIND) || (skillId == AREA_OF_SILENCE)
						|| (skillId == WIND_SHACKLE) || (skillId == STRIKER_GALE) || (skillId == SHOCK_STUN) || (skillId == FOG_OF_SLEEPING)
						|| (skillId == ICE_LANCE) || (skillId == FREEZING_BLIZZARD) || (skillId == FREEZING_BREATH) || (skillId == POLLUTE_WATER)
						|| (skillId == ELEMENTAL_FALL_DOWN) || (skillId == RETURN_TO_NATURE) 
						|| (skillId == ICE_LANCE_COCKATRICE) || (skillId == ICE_LANCE_BASILISK)) {
					return false;
				}
			}
		}
		return true;
	}

	private int calcProbability(int skillId) {
		L1Skills l1skills = SkillsTable.getInstance().getTemplate(skillId);
		int attackLevel = 0;
		int defenseLevel = 0;
		int probability = 0;

		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			attackLevel = _pc.getLevel();
		}
		else {
			attackLevel = _npc.getLevel();
		}

		if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
			defenseLevel = _targetPc.getLevel();
		}
		else {
			defenseLevel = _targetNpc.getLevel();
			if (skillId == RETURN_TO_NATURE) {
				if (_targetNpc instanceof L1SummonInstance) {
					L1SummonInstance summon = (L1SummonInstance) _targetNpc;
					defenseLevel = summon.getMaster().getLevel();
				}
			}
		}

		if ((skillId == ELEMENTAL_FALL_DOWN) || (skillId == RETURN_TO_NATURE) || (skillId == ENTANGLE) || (skillId == ERASE_MAGIC)
				|| (skillId == AREA_OF_SILENCE) || (skillId == WIND_SHACKLE) || (skillId == STRIKER_GALE) || (skillId == POLLUTE_WATER)
				|| (skillId == EARTH_BIND)) {
			// 成功確率は 魔法固有係数 × LV差 + 基本確率
			probability = (int) (((l1skills.getProbabilityDice()) / 10D) * (attackLevel - defenseLevel)) + l1skills.getProbabilityValue();

			// オリジナルINTによる魔法命中
			if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
				probability += 2 * _pc.getOriginalMagicHit();
			}
		}
		else if (skillId == SHOCK_STUN) {
			// 成功確率は 基本確率 + LV差1毎に+-2%
			probability = l1skills.getProbabilityValue() + (attackLevel - defenseLevel) * 2;

			// オリジナルINTによる魔法命中
			if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
				int difflvl = attackLevel - defenseLevel;
				if (difflvl >= 0) {
					probability = 50 + difflvl * 3;
				}
				else {
					probability = Math.max(50 + difflvl * 6, 5);
				}				
				probability += 2 * _pc.getOriginalMagicHit();
			}
		}
		else if (skillId == COUNTER_BARRIER) {
			int bonus = Math.max(0, (attackLevel - 60) / 4);

			probability = l1skills.getProbabilityValue() + bonus;

			if (_calcType == PC_PC || _calcType == PC_NPC) {
				probability += 2 * _pc.getOriginalMagicHit();
			}
		}
		else if (skillId == PHANTASM) {
			probability = l1skills.getProbabilityValue();
		}
/*		else if ((skillId == GUARD_BRAKE) || (skillId == RESIST_FEAR) || (skillId == HORROR_OF_DEATH)) {
			int dice = l1skills.getProbabilityDice();
			int value = l1skills.getProbabilityValue();
			int diceCount = 0;
			diceCount = getMagicBonus() + getMagicLevel();

			if (diceCount < 1) {
				diceCount = 1;
			}

			for (int i = 0; i < diceCount; i++) {
				probability += (Random.nextInt(dice) + 1 + value);
			}

			probability = probability * getLeverage() / 10;

			// オリジナルINTによる魔法命中
			if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
				probability += 2 * _pc.getOriginalMagicHit();
			}

			if (probability >= getTargetMr()) {
				probability = 100;
			}
			else {
				probability = 0;
			}
		}*/
		else if (skillId == GUARD_BRAKE || skillId == RESIST_FEAR
				|| skillId == HORROR_OF_DEATH) { 
			// probability is based on http://forum.gamer.com.tw/Co.php?bsn=00842&sn=5283670
			probability = 50 + (attackLevel - defenseLevel) * 3;
			if (skillId == GUARD_BRAKE) probability -= 3;	
			if (skillId == RESIST_FEAR) probability -= 5;

			if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
				probability += 2 * _pc.getOriginalMagicHit();
			}
		}
		else if (skillId == THUNDER_GRAB) { 
			// success rate is probability_value(50%) * (attackerlvl/ defenselvl) + random(0〜-20)
			probability = 50
					* (attackLevel / Math.max(1, defenseLevel))
					- Random.nextInt(21);

			if (_calcType == PC_PC || _calcType == PC_NPC) {
				probability += 2 * _pc.getOriginalMagicHit();
			}
		}
		else {
			int dice = l1skills.getProbabilityDice();
			int diceCount = 0;
			if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
				if (_pc.isWizard()) {
					diceCount = getMagicBonus() + getMagicLevel() + 1;
				}
				else if (_pc.isElf()) {
					diceCount = getMagicBonus() + getMagicLevel() - 1;
				}
				else {
					diceCount = getMagicBonus() + getMagicLevel() - 1;
				}
			}
			else {
				diceCount = getMagicBonus() + getMagicLevel();
			}
			if (diceCount < 1) {
				diceCount = 1;
			}

			for (int i = 0; i < diceCount; i++) {
				probability += (Random.nextInt(dice) + 1);
			}
			probability = probability * getLeverage() / 10;

			// オリジナルINTによる魔法命中
			if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
				probability += 2 * _pc.getOriginalMagicHit();
			}

			probability -= getTargetMr();

			if (skillId == TAMING_MONSTER) {
				double probabilityRevision = 1;
				if ((_targetNpc.getMaxHp() * 1 / 4) > _targetNpc.getCurrentHp()) {
					probabilityRevision = 1.3;
				}
				else if ((_targetNpc.getMaxHp() * 2 / 4) > _targetNpc.getCurrentHp()) {
					probabilityRevision = 1.2;
				}
				else if ((_targetNpc.getMaxHp() * 3 / 4) > _targetNpc.getCurrentHp()) {
					probabilityRevision = 1.1;
				}
				probability *= probabilityRevision;
			}
		}

		// 状態異常に対する耐性
		if (skillId == EARTH_BIND) {
			if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
				probability -= _targetPc.getRegistSustain();
			}
		}
		else if (skillId == SHOCK_STUN) {
			if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
				probability -= 2 * _targetPc.getRegistStun();
			}
		}
		else if (skillId == CURSE_PARALYZE) {
			if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
				probability -= _targetPc.getRegistStone();
			}
		}
		else if (skillId == FOG_OF_SLEEPING || skillId == PHANTASM) {
			if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
				probability -= _targetPc.getRegistSleep();
			}
		}
		else if ((skillId == ICE_LANCE) || (skillId == FREEZING_BLIZZARD) || (skillId == FREEZING_BREATH) 
			|| (skillId == ICE_LANCE_COCKATRICE) || (skillId == ICE_LANCE_BASILISK)) {
			if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
				probability -= _targetPc.getRegistFreeze();	
				// 檢查無敵狀態
				for (int skillid : INVINCIBLE) {
					if (_targetPc.hasSkillEffect(skillid)) {
						probability = 0;
						break;
					}
				}
			}
		}
		else if ((skillId == CURSE_BLIND) || (skillId == DARKNESS) || (skillId == DARK_BLIND)) {
			if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
				probability -= _targetPc.getRegistBlind();
			}
		}

		return probability;
	}

	// 擁有這些狀態的, 不會受到傷害(無敵)
	private static final int[] INVINCIBLE = {
		ABSOLUTE_BARRIER, ICE_LANCE, FREEZING_BLIZZARD, FREEZING_BREATH, EARTH_BIND, ICE_LANCE_COCKATRICE, ICE_LANCE_BASILISK
	};

	/* ■■■■■■■■■■■■■■ 魔法ダメージ算出 ■■■■■■■■■■■■■■ */

	public int calcMagicDamage(int skillId) {
		int damage = 0;
		
		// 檢查無敵狀態
		for (int skillid : INVINCIBLE) {
			if (_target.hasSkillEffect(skillid)) {
				return damage;
			}
		}

		if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
			damage = calcPcMagicDamage(skillId);
		}
		else if ((_calcType == PC_NPC) || (_calcType == NPC_NPC)) {
			damage = calcNpcMagicDamage(skillId);
		}

		if (skillId != JOY_OF_PAIN) { // 疼痛的歡愉無視魔免
			damage = calcMrDefense(damage);
		}
		
		if (_calcType == PC_NPC && _pc.getDmgMessages()) {
			_pc.sendPackets(new S_SystemMessage(L1NamedSkill.getName(skillId) +
					" Dealt:" + String.valueOf(damage)));
		}

		return damage;
	}

	// ●●●● プレイヤー へのファイアーウォールの魔法ダメージ算出 ●●●●
	public int calcPcFireWallDamage() {
		int dmg = 0;
		double attrDeffence = calcAttrResistance(L1Skills.ATTR_FIRE);
		L1Skills l1skills = SkillsTable.getInstance().getTemplate(FIRE_WALL);
		dmg = (int) ((1.0 - attrDeffence) * l1skills.getDamageValue());

		if (_targetPc.hasSkillEffect(ABSOLUTE_BARRIER)) {
			dmg = 0;
		}
		if (_targetPc.hasSkillEffect(ICE_LANCE)) {
			dmg = 0;
		}
		if (_targetPc.hasSkillEffect(FREEZING_BLIZZARD)) {
			dmg = 0;
		}
		if (_targetPc.hasSkillEffect(FREEZING_BREATH)) {
			dmg = 0;
		}
		if (_targetPc.hasSkillEffect(EARTH_BIND)) {
			dmg = 0;
		}
		if (_targetPc.hasSkillEffect(ICE_LANCE_COCKATRICE)) {
			dmg = 0;
		}
		if (_targetPc.hasSkillEffect(ICE_LANCE_BASILISK)) {
			dmg = 0;
		}

		if (dmg < 0) {
			dmg = 0;
		}

		return dmg;
	}

	// ●●●● ＮＰＣ へのファイアーウォールの魔法ダメージ算出 ●●●●
	public int calcNpcFireWallDamage() {
		int dmg = 0;
		double attrDeffence = calcAttrResistance(L1Skills.ATTR_FIRE);
		L1Skills l1skills = SkillsTable.getInstance().getTemplate(FIRE_WALL);
		dmg = (int) ((1.0 - attrDeffence) * l1skills.getDamageValue());

		if (_targetNpc.hasSkillEffect(ICE_LANCE)) {
			dmg = 0;
		}
		if (_targetNpc.hasSkillEffect(FREEZING_BLIZZARD)) {
			dmg = 0;
		}
		if (_targetNpc.hasSkillEffect(FREEZING_BREATH)) {
			dmg = 0;
		}
		if (_targetNpc.hasSkillEffect(EARTH_BIND)) {
			dmg = 0;
		}
		if (_targetNpc.hasSkillEffect(ICE_LANCE_COCKATRICE)) {
			dmg = 0;
		}
		if (_targetNpc.hasSkillEffect(ICE_LANCE_BASILISK)) {
			dmg = 0;
		}

		if (dmg < 0) {
			dmg = 0;
		}

		return dmg;
	}

	// ●●●● プレイヤー・ＮＰＣ から プレイヤー への魔法ダメージ算出 ●●●●
	private int calcPcMagicDamage(int skillId) {
		int dmg = 0;
		if (skillId == FINAL_BURN) {
			if (_calcType == PC_PC) {
				dmg = _pc.getCurrentMp();
			} else {
				dmg = _npc.getCurrentMp();
			}
		} else {
			dmg = calcMagicDiceDamage(skillId);
			dmg = (dmg * getLeverage()) / 10;
		}

		// 心靈破壞消耗目標5點MP造成5倍精神傷害
		if (skillId == MIND_BREAK) {
			if (_targetPc.getCurrentMp() >= 10) {
				_targetPc.setCurrentMp(_targetPc.getCurrentMp() - 10);
				if (_calcType == PC_PC) {
					dmg += _pc.getWis() * 5;
				} else if (_calcType == NPC_PC) {
					dmg += _npc.getWis() * 5;
				}
			}
		}

		dmg -= _targetPc.getDamageReductionByArmor(); // 防具によるダメージ軽減

		// 魔法娃娃效果 - 傷害減免
		dmg -= L1MagicDoll.getDamageReductionByDoll(_targetPc);

		if (_targetPc.hasSkillEffect(COOKING_1_0_S) // 料理によるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_1_1_S) || _targetPc.hasSkillEffect(COOKING_1_2_S)
				|| _targetPc.hasSkillEffect(COOKING_1_3_S)
				|| _targetPc.hasSkillEffect(COOKING_1_4_S) || _targetPc.hasSkillEffect(COOKING_1_5_S)
				|| _targetPc.hasSkillEffect(COOKING_1_6_S)
				|| _targetPc.hasSkillEffect(COOKING_2_0_S) || _targetPc.hasSkillEffect(COOKING_2_1_S)
				|| _targetPc.hasSkillEffect(COOKING_2_2_S)
				|| _targetPc.hasSkillEffect(COOKING_2_3_S) || _targetPc.hasSkillEffect(COOKING_2_4_S)
				|| _targetPc.hasSkillEffect(COOKING_2_5_S)
				|| _targetPc.hasSkillEffect(COOKING_2_6_S) || _targetPc.hasSkillEffect(COOKING_3_0_S)
				|| _targetPc.hasSkillEffect(COOKING_3_1_S)
				|| _targetPc.hasSkillEffect(COOKING_3_2_S) || _targetPc.hasSkillEffect(COOKING_3_3_S)
				|| _targetPc.hasSkillEffect(COOKING_3_4_S)
				|| _targetPc.hasSkillEffect(COOKING_3_5_S) || _targetPc.hasSkillEffect(COOKING_3_6_S)) {
			dmg -= 5;
		}
		if (_targetPc.hasSkillEffect(COOKING_1_7_S) // デザートによるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_2_7_S) || _targetPc.hasSkillEffect(COOKING_3_7_S)) {
			dmg -= 5;
		}

		if (_targetPc.hasSkillEffect(REDUCTION_ARMOR)) {
			int targetPcLvl = _targetPc.getLevel();
			if (targetPcLvl < 50) {
				targetPcLvl = 50;
			}
			dmg -= (targetPcLvl - 50) / 5 + 1;
		}
		if (_targetPc.hasSkillEffect(DRAGON_SKIN)) {
			dmg -= 5; 
		}

		if (_targetPc.hasSkillEffect(PATIENCE)) {
			dmg -= 2;
		}

		if (_calcType == NPC_PC) { // ペット、サモンからプレイヤーに攻撃
			boolean isNowWar = false;
			int castleId = L1CastleLocation.getCastleIdByArea(_targetPc);
			if (castleId > 0) {
				isNowWar = WarTimeController.getInstance().isNowWar(castleId);
			}
			if (!isNowWar) {
				if (_npc instanceof L1PetInstance) {
					dmg /= 16;
				}
				if (_npc instanceof L1SummonInstance) {
					L1SummonInstance summon = (L1SummonInstance) _npc;
					if (summon.isExsistMaster()) {
						dmg /= 16;
					}
				}
			}
		}

		if (_targetPc.hasSkillEffect(IMMUNE_TO_HARM)) {
			dmg /= 2;
		}
		// 疼痛的歡愉傷害：(最大血量 - 目前血量 /5)
		if (skillId == JOY_OF_PAIN) {
			int nowDamage = 0;
			if (_calcType == PC_PC) {
				nowDamage = _pc.getMaxHp() - _pc.getCurrentHp();
				if (nowDamage > 0) {
					dmg = nowDamage / 5;
				}
			} else if (_calcType == NPC_PC) {
				nowDamage = _npc.getMaxHp() - _npc.getCurrentHp();
				if (nowDamage > 0) {
					dmg = nowDamage / 5;
				}
			}
		}

		if (_targetPc.hasSkillEffect(ABSOLUTE_BARRIER)) {
			dmg = 0;
		} else if (_targetPc.hasSkillEffect(ICE_LANCE)) {
			dmg = 0;
		} else if (_targetPc.hasSkillEffect(FREEZING_BLIZZARD)) {
			dmg = 0;
		} else if (_targetPc.hasSkillEffect(FREEZING_BREATH)) {
			dmg = 0;
		} else if (_targetPc.hasSkillEffect(EARTH_BIND)) {
			dmg = 0;
		}
		
		// magic doll damage evasion
		if (L1MagicDoll.getDamageEvasionByDoll(_targetPc) > 0) {
			dmg = 0;
		}

		if (_calcType == NPC_PC) {
			if ((_npc instanceof L1PetInstance) || (_npc instanceof L1SummonInstance)) {
				// 目標在安區、攻擊者在安區、NOPVP
				if ((_targetPc.getZoneType() == 1) || (_npc.getZoneType() == 1)
						|| (_targetPc.checkNonPvP(_targetPc, _npc))) {
					dmg = 0;
				}
			}
		}

		if (_targetPc.hasSkillEffect(COUNTER_MIRROR)) {
			if (_calcType == PC_PC) {
				if (_targetPc.getWis() >= Random.nextInt(100)) {
					_pc.sendPackets(new S_DoActionGFX(_pc.getId(), ActionCodes.ACTION_Damage));
					_pc.broadcastPacket(new S_DoActionGFX(_pc.getId(), ActionCodes.ACTION_Damage));
					_targetPc.sendPackets(new S_SkillSound(_targetPc.getId(), 4395));
					_targetPc.broadcastPacket(new S_SkillSound(_targetPc.getId(), 4395));
					_pc.receiveDamage(_targetPc, dmg, false);
					dmg = 0;
					_targetPc.killSkillEffectTimer(COUNTER_MIRROR);
				}
			}
			else if (_calcType == NPC_PC) {
				int npcId = _npc.getNpcTemplate().get_npcId();
				if ((npcId == 45681) || (npcId == 45682) || (npcId == 45683) || (npcId == 45684)) {}
				else if (!_npc.getNpcTemplate().get_IsErase()) {}
				else {
					if (_targetPc.getWis() >= Random.nextInt(100)) {
						_npc.broadcastPacket(new S_DoActionGFX(_npc.getId(), ActionCodes.ACTION_Damage));
						_targetPc.sendPackets(new S_SkillSound(_targetPc.getId(), 4395));
						_targetPc.broadcastPacket(new S_SkillSound(_targetPc.getId(), 4395));
						_npc.receiveDamage(_targetPc, dmg);
						dmg = 0;
						_targetPc.killSkillEffectTimer(COUNTER_MIRROR);
					}
				}
			}
		}

		if (dmg < 0) {
			dmg = 0;
		}

		return dmg;
	}

	// ●●●● プレイヤー・ＮＰＣ から ＮＰＣ へのダメージ算出 ●●●●
	private int calcNpcMagicDamage(int skillId) {
		int dmg = 0;
		if (skillId == FINAL_BURN) {
			if (_calcType == PC_NPC) {
				dmg = _pc.getCurrentMp();
			} else {
				dmg = _npc.getCurrentMp();
			}
		}
		else {
			dmg = calcMagicDiceDamage(skillId);
			dmg = (dmg * getLeverage()) / 10;
		}

		// 心靈破壞消耗目標5點MP造成5倍精神傷害
		if (skillId == MIND_BREAK) {
			if (_targetNpc.getCurrentMp() >= 10) {
				_targetNpc.setCurrentMp(_targetNpc.getCurrentMp() - 10);
				if (_calcType == PC_NPC) {
					dmg += _pc.getWis() * 5;
				} else if (_calcType == NPC_NPC) {
					dmg += _npc.getWis() * 5;
				}
			}
		}

		// 疼痛的歡愉傷害：(最大血量 - 目前血量 /5)
		if (skillId == JOY_OF_PAIN) {
			int nowDamage = 0;
			if (_calcType == PC_NPC) {
				nowDamage = _pc.getMaxHp() - _pc.getCurrentHp();
				if (nowDamage > 0) {
					dmg = nowDamage / 5;
				}
			} else if (_calcType == NPC_NPC) {
				nowDamage = _npc.getMaxHp() - _npc.getCurrentHp();
				if (nowDamage > 0) {
					dmg = nowDamage / 5;
				}
			}
		}

		if (_calcType == PC_NPC) { // プレイヤーからペット、サモンに攻撃
			boolean isNowWar = false;
			int castleId = L1CastleLocation.getCastleIdByArea(_targetNpc);
			if (castleId > 0) {
				isNowWar = WarTimeController.getInstance().isNowWar(castleId);
			}
			if (!isNowWar) {
				if (_targetNpc instanceof L1PetInstance) {
					dmg /= 8;
				}
				if (_targetNpc instanceof L1SummonInstance) {
					L1SummonInstance summon = (L1SummonInstance) _targetNpc;
					if (summon.isExsistMaster()) {
						dmg /= 8;
					}
				}
			}
		}

		if (_targetNpc.hasSkillEffect(ICE_LANCE)) {
			dmg = 0;
		} else if (_targetNpc.hasSkillEffect(FREEZING_BLIZZARD)) {
			dmg = 0;
		} else if (_targetNpc.hasSkillEffect(FREEZING_BREATH)) {
			dmg = 0;
		} else if (_targetNpc.hasSkillEffect(EARTH_BIND)) {
			dmg = 0;
		}

		// 判斷特定狀態下才可攻擊 NPC
		if ((_calcType == PC_NPC) && (_targetNpc != null)) {
			if (_pc.isAttackMiss(_pc, _targetNpc.getNpcTemplate().get_npcId())) {
				dmg = 0;
			}
		}
		if (_calcType == NPC_NPC) {
			if (((_npc instanceof L1PetInstance) || (_npc instanceof L1SummonInstance))
					&& ((_targetNpc instanceof L1PetInstance) || (_targetNpc instanceof L1SummonInstance))) {
				// 目標在安區、攻擊者在安區
				if ((_targetNpc.getZoneType() == 1) || (_npc.getZoneType() == 1)) {
					dmg = 0;
				}
				if (_targetNpc.getMaster() == _npc.getMaster()) {
					dmg = 0;
				}
			}
		}

		return dmg;
	}

	// ●●●● damage_dice、damage_dice_count、damage_value、SPから魔法ダメージを算出 ●●●●
	private int calcMagicDiceDamage(int skillId) {
		L1Skills l1skills = SkillsTable.getInstance().getTemplate(skillId);
		int dice = l1skills.getDamageDice();
		int diceCount = l1skills.getDamageDiceCount();
		int value = l1skills.getDamageValue();
		int magicDamage = 0;
		int charaIntelligence = 0;

		for (int i = 0; i < diceCount; i++) {
			magicDamage += (Random.nextInt(dice) + 1);
		}
		magicDamage += value;

		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			int weaponAddDmg = 0; // 武器による追加ダメージ
			L1ItemInstance weapon = _pc.getWeapon();
			if (weapon != null) {
				weaponAddDmg = weapon.getItem().getMagicDmgModifier();
			}
			magicDamage += weaponAddDmg;
		}

		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			int spByItem = _pc.getSp() - _pc.getTrueSp(); // アイテムによるSP変動
			charaIntelligence = _pc.getInt() + spByItem - 12;
		}
		else if ((_calcType == NPC_PC) || (_calcType == NPC_NPC)) {
			int spByItem = _npc.getSp() - _npc.getTrueSp(); // アイテムによるSP変動
			charaIntelligence = _npc.getInt() + spByItem - 12;
		}
		if (charaIntelligence < 1) {
			charaIntelligence = 1;
		}

		double attrDeffence = calcAttrResistance(l1skills.getAttr());

		double coefficient = (1.0 - attrDeffence + charaIntelligence * 3.0 / 32.0);
		if (coefficient < 0) {
			coefficient = 0;
		}

		magicDamage *= coefficient;

		double criticalCoefficient = 1.5; // 魔法クリティカル
		int rnd = Random.nextInt(100) + 1;
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			if (l1skills.getSkillLevel() <= 6) {
				if (rnd <= (10 + _pc.getOriginalMagicCritical())) {
					magicDamage *= criticalCoefficient;
				}
			}
		}

		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) { // オリジナルINTによる魔法ダメージ
			magicDamage += _pc.getOriginalMagicDamage();
		}
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) { // アバターによる追加ダメージ
			if (_pc.hasSkillEffect(ILLUSION_AVATAR)) {
				magicDamage += 10;
			}
		}

		return magicDamage;
	}

	// ●●●● ヒール回復量（対アンデッドにはダメージ）を算出 ●●●●
	public int calcHealing(int skillId) {
		L1Skills l1skills = SkillsTable.getInstance().getTemplate(skillId);
		int dice = l1skills.getDamageDice();
		int value = l1skills.getDamageValue();
		int magicDamage = 0;

		int magicBonus = getMagicBonus();
		if (magicBonus > 10) {
			magicBonus = 10;
		}

		int diceCount = value + magicBonus;
		for (int i = 0; i < diceCount; i++) {
			magicDamage += (Random.nextInt(dice) + 1);
		}

		double alignmentRevision = 1.0;
		if (getLawful() > 0) {
			alignmentRevision += (getLawful() / 32768.0);
		}

		magicDamage *= alignmentRevision;

		magicDamage = (magicDamage * getLeverage()) / 10;

		return magicDamage;
	}

	// ●●●● ＭＲによるダメージ軽減 ●●●●
	private int calcMrDefense(int dmg) {
		int mr = getTargetMr();

		double mrFloor = 0;
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			if (mr <= 100) {
				mrFloor = Math.floor((mr - _pc.getOriginalMagicHit()) / 2);
			}
			else if (mr >= 100) {
				mrFloor = Math.floor((mr - _pc.getOriginalMagicHit()) / 10);
			}
			double mrCoefficient = 0;
			if (mr <= 100) {
				mrCoefficient = 1 - 0.01 * mrFloor;
			}
			else if (mr >= 100) {
				mrCoefficient = 0.6 - 0.01 * mrFloor;
			}
			dmg *= mrCoefficient;
		}
		else if ((_calcType == NPC_PC) || (_calcType == NPC_NPC)) {
			int rnd = Random.nextInt(100) + 1;
			if (mr >= rnd) {
				dmg /= 2;
			}
		}

		return dmg;
	}

	// ●●●● 属性によるダメージ軽減 ●●●●
	// attr:0.無属性魔法,1.地魔法,2.火魔法,4.水魔法,8.風魔法(,16.光魔法)
	private double calcAttrResistance(int attr) {
		int resist = 0;
		if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
			if (attr == L1Skills.ATTR_EARTH) {
				resist = _targetPc.getEarth();
			}
			else if (attr == L1Skills.ATTR_FIRE) {
				resist = _targetPc.getFire();
			}
			else if (attr == L1Skills.ATTR_WATER) {
				resist = _targetPc.getWater();
			}
			else if (attr == L1Skills.ATTR_WIND) {
				resist = _targetPc.getWind();
			}
		}
		else if ((_calcType == PC_NPC) || (_calcType == NPC_NPC)) {}

		int resistFloor = (int) (0.32 * Math.abs(resist));
		if (resist >= 0) {
			resistFloor *= 1;
		}
		else {
			resistFloor *= -1;
		}

		double attrDeffence = resistFloor / 32.0;

		return attrDeffence;
	}

	/* ■■■■■■■■■■■■■■■ 計算結果反映 ■■■■■■■■■■■■■■■ */

	public void commit(int damage, int drainMana) {
		if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
			commitPc(damage, drainMana);
		}
		else if ((_calcType == PC_NPC) || (_calcType == NPC_NPC)) {
			commitNpc(damage, drainMana);
		}

		// ダメージ値及び命中率確認用メッセージ
		if (!Config.ALT_ATKMSG) {
			return;
		}
		if (Config.ALT_ATKMSG) {
			if (((_calcType == PC_PC) || (_calcType == PC_NPC)) && !_pc.isGm()) {
				return;
			}
			if ((_calcType == NPC_PC) && !_targetPc.isGm()) {
				return;
			}
		}

		String msg0 = "";
		String msg1 = " 造成 ";
		String msg2 = "";
		String msg3 = "";
		String msg4 = "";

		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {// アタッカーがＰＣの場合
			msg0 = "魔攻 對";
		}
		else if (_calcType == NPC_PC) { // アタッカーがＮＰＣの場合
			msg0 = _npc.getName() + "(魔攻)：";
		}

		if ((_calcType == NPC_PC) || (_calcType == PC_PC)) { // ターゲットがＰＣの場合
			msg4 = _targetPc.getName();
			msg2 = "，剩餘 " + _targetPc.getCurrentHp();
		}
		else if (_calcType == PC_NPC) { // ターゲットがＮＰＣの場合
			msg4 = _targetNpc.getName();
			msg2 = "，剩餘 " + _targetNpc.getCurrentHp();
		}

		msg3 = damage  + " 傷害";

		// 魔攻 對 目標 造成 X 傷害，剩餘 Y。
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) { // アタッカーがＰＣの場合
			_pc.sendPackets(new S_ServerMessage(166, msg0, msg1, msg2, msg3, msg4)); // \f1%0が%4%1%3
																						// %2
		}
		// 攻擊者(魔攻)： X傷害，剩餘 Y。
		else if ((_calcType == NPC_PC)) { // ターゲットがＰＣの場合
			_targetPc.sendPackets(new S_ServerMessage(166, msg0, null, msg2, msg3, null)); // \f1%0が%4%1%3
																							// %2
		}
	}

	// ●●●● プレイヤーに計算結果を反映 ●●●●
	private void commitPc(int damage, int drainMana) {
		if (_calcType == PC_PC) {
			if ((drainMana > 0) && (_targetPc.getCurrentMp() > 0)) {
				if (drainMana > _targetPc.getCurrentMp()) {
					drainMana = _targetPc.getCurrentMp();
				}
				int newMp = _pc.getCurrentMp() + drainMana;
				_pc.setCurrentMp(newMp);
			}
			_targetPc.receiveManaDamage(_pc, drainMana);
			_targetPc.receiveDamage(_pc, damage, true);
		}
		else if (_calcType == NPC_PC) {
			_targetPc.receiveDamage(_npc, damage, true);
		}
	}

	// ●●●● ＮＰＣに計算結果を反映 ●●●●
	private void commitNpc(int damage, int drainMana) {
		if (_calcType == PC_NPC) {
			if (drainMana > 0) {
				int drainValue = _targetNpc.drainMana(drainMana);
				int newMp = _pc.getCurrentMp() + drainValue;
				_pc.setCurrentMp(newMp);
			}
			_targetNpc.ReceiveManaDamage(_pc, drainMana);
			_targetNpc.receiveDamage(_pc, damage);
		}
		else if (_calcType == NPC_NPC) {
			_targetNpc.receiveDamage(_npc, damage);
		}
	}
}
