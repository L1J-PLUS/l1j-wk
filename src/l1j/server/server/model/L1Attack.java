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
import l1j.server.server.model.gametime.L1GameTimeClock;
import l1j.server.server.model.npc.action.L1NpcDefaultAction;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.model.poison.L1ParalysisPoison;
import l1j.server.server.model.poison.L1SilencePoison;
import l1j.server.server.serverpackets.S_AttackMissPacket;
import l1j.server.server.serverpackets.S_AttackPacket;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_EffectLocation;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_UseArrowSkill;
import l1j.server.server.serverpackets.S_UseAttackSkill;
import l1j.server.server.templates.L1MagicDoll;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.types.Point;
import l1j.server.server.utils.Random;
import java.util.Arrays;

import static l1j.server.server.model.skill.L1SkillId.*;

public class L1Attack {
	private L1PcInstance _pc = null;

	private L1Character _target = null;

	private L1PcInstance _targetPc = null;

	private L1NpcInstance _npc = null;

	private L1NpcInstance _targetNpc = null;

	private final int _targetId;

	private int _targetX;

	private int _targetY;

	private int _statusDamage = 0;

	private int _hitRate = 0;

	private int _calcType;

	private static final int PC_PC = 1;

	private static final int PC_NPC = 2;

	private static final int NPC_PC = 3;

	private static final int NPC_NPC = 4;

	private boolean _isHit = false;

	private int _damage = 0;

	private int _drainMana = 0;

	private int _drainHp = 0;

	private byte _effectId = 0;

	private int _attckGrfxId = 0;

	private int _attckActId = 0;

	// 攻撃者がプレイヤーの場合の武器情報
	private L1ItemInstance weapon = null;

	private int _weaponId = 0;

	private int _weaponType = 0;

	private int _weaponType2 = 0;

	private int _weaponAddHit = 0;

	private int _weaponAddDmg = 0;

	private int _weaponSmall = 0;

	private int _weaponLarge = 0;

	private int _weaponRange = 1;

	private int _weaponBless = 1;

	private int _weaponEnchant = 0;

	private int _weaponMaterial = 0;

	private int _weaponDoubleDmgChance = 0;

	private int _weaponAttrEnchantKind = 0;

	private int _weaponAttrEnchantLevel = 0;

	private L1ItemInstance _arrow = null;

	private L1ItemInstance _sting = null;

	private int _leverage = 10; // 1/10倍で表現する。

	private int _skillId;

	@SuppressWarnings("unused")
	private double _skillDamage = 0;

	public void setLeverage(int i) {
		_leverage = i;
	}

	private int getLeverage() {
		return _leverage;
	}
	
	private static final int[] PREVENT_DAMAGE = { ABSOLUTE_BARRIER, ICE_LANCE,
		FREEZING_BLIZZARD, FREEZING_BREATH, EARTH_BIND };
	

	// 攻撃者がプレイヤーの場合のステータスによる補正
	// private static final int[] strHit = { -2, -2, -2, -2, -2, -2, -2, -2, -2,
	// -2, -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9,
	// 9, 10, 10, 11, 11, 12, 12, 13, 13, 14 };

	// private static final int[] dexHit = { -2, -2, -2, -2, -2, -2, -2, -2, -2,
	// -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8,
	// 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14 };

	/*
	 * private static final int[] strHit = { -2, -2, -2, -2, -2, -2, -2, //
	 * 0～7まで -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, //
	 * 8～26まで 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, //
	 * 27～44まで 13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 17}; //
	 * 45～59まで
	 * 
	 * private static final int[] dexHit = { -2, -2, -2, -2, -2, -2, -1, -1, 0,
	 * 0, // 1～10まで 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
	 * 15, 16, // 11～30まで 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
	 * 30, 31, // 31～45まで 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44,
	 * 45, 46 }; // 46～60まで
	 * 
	 * private static final int[] strDmg = new int[128];
	 * 
	 * static { // ＳＴＲダメージ補正 int dmg = -6; for (int str = 0; str <= 22; str++) {
	 * // ０～２２は２毎に＋１ if (str % 2 == 1) { dmg++; } strDmg[str] = dmg; } for (int
	 * str = 23; str <= 28; str++) { // ２３～２８は３毎に＋１ if (str % 3 == 2) { dmg++; }
	 * strDmg[str] = dmg; } for (int str = 29; str <= 32; str++) { //
	 * ２９～３２は２毎に＋１ if (str % 2 == 1) { dmg++; } strDmg[str] = dmg; } for (int
	 * str = 33; str <= 39; str++) { // ３３～３９は１毎に＋１ dmg++; strDmg[str] = dmg; }
	 * for (int str = 40; str <= 46; str++) { // ４０～４６は１毎に＋２ dmg += 2;
	 * strDmg[str] = dmg; } for (int str = 47; str <= 127; str++) { //
	 * ４７～１２７は１毎に＋１ dmg++; strDmg[str] = dmg; } }
	 * 
	 * private static final int[] dexDmg = new int[128];
	 * 
	 * static { // ＤＥＸダメージ補正 for (int dex = 0; dex <= 14; dex++) { // ０～１４は０
	 * dexDmg[dex] = 0; } dexDmg[15] = 1; dexDmg[16] = 2; dexDmg[17] = 3;
	 * dexDmg[18] = 4; dexDmg[19] = 4; dexDmg[20] = 4; dexDmg[21] = 5;
	 * dexDmg[22] = 5; dexDmg[23] = 5; int dmg = 5; for (int dex = 24; dex <=
	 * 127; dex++) { // ２４～１２７は１毎に＋１ dmg++; dexDmg[dex] = dmg; } }
	 */

	private static final int[] strHit = { -2, -2, -2, -2, -2, -2, -2, // 1～7まで
			-2, -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6, // 8～26まで
			7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, // 27～44まで
			13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 17 }; // 45～59まで

	private static final int[] dexHit = { -2, -2, -2, -2, -2, -2, -1, -1, 0, 0, // 1～10まで
			1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, // 11～30まで
			17, 18, 19, 19, 19, 20, 20, 20, 21, 21, 21, 22, 22, 22, 23, // 31～45まで
			23, 23, 24, 24, 24, 25, 25, 25, 26, 26, 26, 27, 27, 27, 28 }; // 46～60まで

	private static final int[] strDmg = new int[128];

	static {
		// STRダメージ補正
		int dmg = -6;
		for (int str = 0; str <= 22; str++) { // 0～22は2毎に+1
			if (str % 2 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 23; str <= 28; str++) { // 23～28は3毎に+1
			if (str % 3 == 2) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 29; str <= 32; str++) { // 29～32は2毎に+1
			if (str % 2 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 33; str <= 34; str++) { // 33～34は1毎に+1
			dmg++;
			strDmg[str] = dmg;
		}
		for (int str = 35; str <= 127; str++) { // 35～127は4毎に+1
			if (str % 4 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		Arrays.sort(PREVENT_DAMAGE);
	}

	private static final int[] dexDmg = new int[128];

	static {
		// DEXダメージ補正
		for (int dex = 0; dex <= 14; dex++) {
			// 0～14は0
			dexDmg[dex] = 0;
		}
		dexDmg[15] = 1;
		dexDmg[16] = 2;
		dexDmg[17] = 3;
		dexDmg[18] = 4;
		dexDmg[19] = 4;
		dexDmg[20] = 4;
		dexDmg[21] = 5;
		dexDmg[22] = 5;
		dexDmg[23] = 5;
		int dmg = 5;
		for (int dex = 24; dex <= 35; dex++) { // 24～35は3毎に+1
			if (dex % 3 == 1) {
				dmg++;
			}
			dexDmg[dex] = dmg;
		}
		for (int dex = 36; dex <= 127; dex++) { // 36～127は4毎に1
			if (dex % 4 == 1) {
				dmg++;
			}
			dexDmg[dex] = dmg;
		}
	}

	public void setActId(int actId) {
		_attckActId = actId;
	}

	public void setGfxId(int gfxId) {
		_attckGrfxId = gfxId;
	}

	public int getActId() {
		return _attckActId;
	}

	public int getGfxId() {
		return _attckGrfxId;
	}

	public L1Attack(L1Character attacker, L1Character target) {
		this(attacker, target, 0);
	}

	public L1Attack(L1Character attacker, L1Character target, int skillId) {
		_skillId = skillId;
		if (_skillId != 0) {
			L1Skills skills = SkillsTable.getInstance().getTemplate(_skillId);
			_skillDamage = skills.getDamageValue();
		}
		if (attacker instanceof L1PcInstance) {
			_pc = (L1PcInstance) attacker;
			if (target instanceof L1PcInstance) {
				_targetPc = (L1PcInstance) target;
				_calcType = PC_PC;
			} else if (target instanceof L1NpcInstance) {
				_targetNpc = (L1NpcInstance) target;
				_calcType = PC_NPC;
			}
			// 武器情報の取得
			weapon = _pc.getWeapon();
			if (weapon != null) {
				_weaponId = weapon.getItem().getItemId();
				_weaponType = weapon.getItem().getType1();
				_weaponType2 = weapon.getItem().getType();
				_weaponAddHit = weapon.getItem().getHitModifier()
						+ weapon.getHitByMagic();
				_weaponAddDmg = weapon.getItem().getDmgModifier()
						+ weapon.getDmgByMagic();
				_weaponSmall = weapon.getItem().getDmgSmall();
				_weaponLarge = weapon.getItem().getDmgLarge();
				_weaponRange = weapon.getItem().getRange();
				_weaponBless = weapon.getItem().getBless();
				_weaponEnchant = weapon.getEnchantLevel();
				_weaponMaterial = weapon.getItem().getMaterial();
				_statusDamage = dexDmg[_pc.getDex()]; // 傷害預設用敏捷補正

				if (_weaponType == 20) { // 弓箭
					_arrow = _pc.getInventory().getArrow();
					if (_arrow != null) {
						_weaponBless = _arrow.getItem().getBless();
						_weaponMaterial = _arrow.getItem().getMaterial();
					}
				} else if (_weaponType == 62) { // 鐵手甲
					_sting = _pc.getInventory().getSting();
					if (_sting != null) {
						_weaponBless = _sting.getItem().getBless();
						_weaponMaterial = _sting.getItem().getMaterial();
					}
				} else { // 近戰類武器
					_weaponEnchant = weapon.getEnchantLevel()
							- weapon.get_durability(); // 計算武器損傷
					_statusDamage = strDmg[_pc.getStr()]; // 傷害用力量補正
				}
				_weaponDoubleDmgChance = weapon.getItem().getDoubleDmgChance();
				_weaponAttrEnchantKind = weapon.getAttrEnchantKind();
				_weaponAttrEnchantLevel = weapon.getAttrEnchantLevel();
			}
		} else if (attacker instanceof L1NpcInstance) {
			_npc = (L1NpcInstance) attacker;
			if (target instanceof L1PcInstance) {
				_targetPc = (L1PcInstance) target;
				_calcType = NPC_PC;
			} else if (target instanceof L1NpcInstance) {
				_targetNpc = (L1NpcInstance) target;
				_calcType = NPC_NPC;
			}
		}
		_target = target;
		_targetId = target.getId();
		_targetX = target.getX();
		_targetY = target.getY();
	}

	/* ■■■■■■■■■■■■■■■■ 命中判定 ■■■■■■■■■■■■■■■■ */

	// 擁有這些狀態的, 不會受到傷害(無敵)
	private static final int[] INVINCIBLE = { ABSOLUTE_BARRIER, ICE_LANCE,
			FREEZING_BLIZZARD, FREEZING_BREATH, EARTH_BIND,
			ICE_LANCE_COCKATRICE, ICE_LANCE_BASILISK };

	public boolean calcHit() {
		// 檢查無敵狀態
		for (int skillId : INVINCIBLE) {
			if (_target.hasSkillEffect(skillId)) {
				_isHit = false;
				return _isHit;
			}
		}

		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			if (_weaponRange != -1) {
/*				if (_pc.getMapId() >= 181 && _pc.getMapId() <= 190 && _weaponRange == 1 && !_pc.glanceCheck(_targetX, _targetY)) {
					_isHit = false; // TOI 81~90f fix
					return _isHit;
				} */
				if (_pc.getLocation()
						.getTileLineDistance(_target.getLocation()) > _weaponRange + 1) { // BIGのモンスターに対応するため射程範囲+1
					_isHit = false; // 射程範囲外
					return _isHit;
				}
			} else {
				if (!_pc.getLocation().isInScreen(_target.getLocation())) {
					_isHit = false; // 射程範囲外
					return _isHit;
				}
			}
			if ((_weaponType == 20) && (_weaponId != 190) && (_arrow == null)) {
				_isHit = false; // 沒有箭
			} else if ((_weaponType == 62) && (_sting == null)) {
				_isHit = false; // 沒有飛刀
			} else if (!_pc.glanceCheck(_targetX, _targetY) && !(_target instanceof L1DoorInstance)) {
				_isHit = false; // 兩格以上武器 直線距離上有障礙物
			} else if ((_weaponId == 247) || (_weaponId == 248)
					|| (_weaponId == 249)) {
				_isHit = false; // 試練の剣B～C 攻撃無効
			} else if (_calcType == PC_PC) {
				_isHit = calcPcPcHit();
			} else if (_calcType == PC_NPC) {
				_isHit = calcPcNpcHit();
			}
		} else if (_calcType == NPC_PC) {
			_isHit = calcNpcPcHit();
		} else if (_calcType == NPC_NPC) {
			_isHit = calcNpcNpcHit();
		}
		return _isHit;
	}

	private int calShortRageHit(int hitRate) {
		int shortHit = hitRate + _pc.getHitup() + _pc.getOriginalHitup();
		// 防具增加命中
		shortHit += _pc.getHitModifierByArmor();

		if (_pc.hasSkillEffect(COOKING_2_0_N) // 料理追加命中
				|| _pc.hasSkillEffect(COOKING_2_0_S))
			shortHit += 1;
		if (_pc.hasSkillEffect(COOKING_3_2_N) // 料理追加命中
				|| _pc.hasSkillEffect(COOKING_3_2_S))
			shortHit += 2;
		return shortHit;
	}

	private int calLongRageHit(int hitRate) {
		int longHit = hitRate + _pc.getBowHitup() + _pc.getOriginalBowHitup();
		// 防具增加命中
		longHit += _pc.getBowHitModifierByArmor();

		if (_pc.hasSkillEffect(COOKING_2_3_N) // 料理追加命中
				|| _pc.hasSkillEffect(COOKING_2_3_S)
				|| _pc.hasSkillEffect(COOKING_3_0_N)
				|| _pc.hasSkillEffect(COOKING_3_0_S))
			longHit += 1;
		return longHit;
	}

	// ●●●● プレイヤー から プレイヤー への命中判定 ●●●●
	/*
	 * ＰＣへの命中率 ＝（PCのLv＋クラス補正＋STR補正＋DEX補正＋武器補正＋DAIの枚数/2＋魔法補正）×0.68－10
	 * これで算出された数値は自分が最大命中(95%)を与える事のできる相手側PCのAC そこから相手側PCのACが1良くなる毎に自命中率から1引いていく
	 * 最小命中率5% 最大命中率95%
	 */
	private boolean calcPcPcHit() {
		_hitRate = _pc.getLevel();

		if (_pc.getStr() > 59) {
			_hitRate += strHit[58];
		} else {
			_hitRate += strHit[_pc.getStr() - 1];
		}

		if (_pc.getDex() > 60) {
			_hitRate += dexHit[59];
		} else {
			_hitRate += dexHit[_pc.getDex() - 1];
		}

		// 命中計算 與魔法、食物buff
		_hitRate += _weaponAddHit + (_weaponEnchant / 2);
		if (_weaponType == 20 || _weaponType == 62)
			_hitRate = calLongRageHit(_hitRate);
		else
			_hitRate = calShortRageHit(_hitRate);

		if ((80 < _pc.getInventory().getWeight242() // 重量による命中補正
				)
				&& (121 >= _pc.getInventory().getWeight242())) {
			_hitRate -= 1;
		} else if ((122 <= _pc.getInventory().getWeight242())
				&& (160 >= _pc.getInventory().getWeight242())) {
			_hitRate -= 3;
		} else if ((161 <= _pc.getInventory().getWeight242())
				&& (200 >= _pc.getInventory().getWeight242())) {
			_hitRate -= 5;
		}

		int attackerDice = Random.nextInt(20) + 1 + _hitRate - 10;

		// 閃避率
		attackerDice -= _targetPc.getDodge();
		attackerDice += _targetPc.getNdodge();

		int defenderDice = 0;

		int defenderValue = (int) (_targetPc.getAc() * 1.5) * -1;

		if (_targetPc.getAc() >= 0) {
			defenderDice = 10 - _targetPc.getAc();
		} else if (_targetPc.getAc() < 0) {
			defenderDice = 10 + Random.nextInt(defenderValue) + 1;
		}

		int fumble = _hitRate - 9;
		int critical = _hitRate + 10;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}

		if (_weaponType2 == 17 || _weaponType2 == 19) {
			_hitRate = 100; // 奇古獸命中率100%
		}

		// TODO 魔法娃娃效果 - 傷害迴避
		else if (L1MagicDoll.getDamageEvasionByDoll(_targetPc) > 0) {
			_hitRate = 0;
		}

		int rnd = Random.nextInt(100) + 1;
		if ((_weaponType == 20) && (_hitRate > rnd)) { // 弓の場合、ヒットした場合でもERでの回避を再度行う。
			return calcErEvasion();
		}

		return _hitRate >= rnd;

		/*
		 * final int MIN_HITRATE = 5;
		 * 
		 * _hitRate = _pc.getLevel();
		 * 
		 * if (_pc.getStr() > 39) { _hitRate += strHit[39]; } else { _hitRate +=
		 * strHit[_pc.getStr()]; }
		 * 
		 * if (_pc.getDex() > 39) { _hitRate += dexHit[39]; } else { _hitRate +=
		 * dexHit[_pc.getDex()]; }
		 * 
		 * if (_weaponType != 20 && _weaponType != 62) { _hitRate +=
		 * _weaponAddHit + _pc.getHitup() + _pc.getOriginalHitup() +
		 * (_weaponEnchant / 2); } else { _hitRate += _weaponAddHit +
		 * _pc.getBowHitup() + _pc .getOriginalBowHitup() + (_weaponEnchant /
		 * 2); }
		 * 
		 * if (_weaponType != 20 && _weaponType != 62) { // 防具による追加命中 _hitRate
		 * += _pc.getHitModifierByArmor(); } else { _hitRate +=
		 * _pc.getBowHitModifierByArmor(); }
		 * 
		 * int hitAc = (int) (_hitRate * 0.68 - 10) * -1;
		 * 
		 * if (hitAc <= _targetPc.getAc()) { _hitRate = 95; } else { _hitRate =
		 * 95 - (hitAc - _targetPc.getAc()); }
		 * 
		 * if (_targetPc.hasSkillEffect(UNCANNY_DODGE)) { _hitRate -= 20; }
		 * 
		 * if (_targetPc.hasSkillEffect(MIRROR_IMAGE)) { _hitRate -= 20; }
		 * 
		 * if (_pc.hasSkillEffect(COOKING_2_0_N) // 料理による追加命中 ||
		 * _pc.hasSkillEffect(COOKING_2_0_S)) { if (_weaponType != 20 &&
		 * _weaponType != 62) { _hitRate += 1; } } if
		 * (_pc.hasSkillEffect(COOKING_3_2_N) // 料理による追加命中 ||
		 * _pc.hasSkillEffect(COOKING_3_2_S)) { if (_weaponType != 20 &&
		 * _weaponType != 62) { _hitRate += 2; } } if
		 * (_pc.hasSkillEffect(COOKING_2_3_N) // 料理による追加命中 ||
		 * _pc.hasSkillEffect(COOKING_2_3_S) ||
		 * _pc.hasSkillEffect(COOKING_3_0_N) ||
		 * _pc.hasSkillEffect(COOKING_3_0_S)) { if (_weaponType == 20 ||
		 * _weaponType == 62) { _hitRate += 1; } }
		 * 
		 * if (_hitRate < MIN_HITRATE) { _hitRate = MIN_HITRATE; }
		 * 
		 * if (_weaponType2 == 17) { _hitRate = 100; // キーリンクの命中率は100% }
		 * 
		 * if (_targetPc.hasSkillEffect(ABSOLUTE_BARRIER)) { _hitRate = 0; } if
		 * (_targetPc.hasSkillEffect(ICE_LANCE)) { _hitRate = 0; } if
		 * (_targetPc.hasSkillEffect(FREEZING_BLIZZARD)) { _hitRate = 0; } if
		 * (_targetPc.hasSkillEffect(FREEZING_BREATH)) { _hitRate = 0; } if
		 * (_targetPc.hasSkillEffect(EARTH_BIND)) { _hitRate = 0; } int rnd =
		 * Random.nextInt(100) + 1; if (_weaponType == 20 && _hitRate > rnd) {
		 * // 弓の場合、ヒットした場合でもERでの回避を再度行う。 return calcErEvasion(); }
		 * 
		 * return _hitRate >= rnd;
		 */
	}

	// ●●●● プレイヤー から ＮＰＣ への命中判定 ●●●●
	private boolean calcPcNpcHit() {
		// ＮＰＣへの命中率
		// ＝（PCのLv＋クラス補正＋STR補正＋DEX補正＋武器補正＋DAIの枚数/2＋魔法補正）×5－{NPCのAC×（-5）}
		_hitRate = _pc.getLevel();

		if (_pc.getStr() > 59) {
			_hitRate += strHit[58];
		} else {
			_hitRate += strHit[_pc.getStr() - 1];
		}

		if (_pc.getDex() > 60) {
			_hitRate += dexHit[59];
		} else {
			_hitRate += dexHit[_pc.getDex() - 1];
		}

		// 命中計算 與魔法、食物buff
		_hitRate += _weaponAddHit + (_weaponEnchant / 2);
		if (_weaponType == 20 || _weaponType == 62)
			_hitRate = calLongRageHit(_hitRate);
		else
			_hitRate = calShortRageHit(_hitRate);

		if ((80 < _pc.getInventory().getWeight242() // 重量による命中補正
				)
				&& (121 >= _pc.getInventory().getWeight242())) {
			_hitRate -= 1;
		} else if ((122 <= _pc.getInventory().getWeight242())
				&& (160 >= _pc.getInventory().getWeight242())) {
			_hitRate -= 3;
		} else if ((161 <= _pc.getInventory().getWeight242())
				&& (200 >= _pc.getInventory().getWeight242())) {
			_hitRate -= 5;
		}

		int attackerDice = Random.nextInt(20) + 1 + _hitRate - 10;

		// 閃避率
		attackerDice -= _targetNpc.getDodge();
		attackerDice += _targetNpc.getNdodge();

		int defenderDice = 10 - _targetNpc.getAc();

		int fumble = _hitRate - 9;
		int critical = _hitRate + 10;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}

		if (_weaponType2 == 17 || _weaponType2 == 19) {
			_hitRate = 100; // 奇古獸 命中率 100%
		}

		// 特定狀態下才可攻擊 NPC
		if (_pc.isAttackMiss(_pc, _targetNpc.getNpcTemplate().get_npcId())) {
			_hitRate = 0;
		}

		int rnd = Random.nextInt(100) + 1;

		return _hitRate >= rnd;
	}

	// ●●●● ＮＰＣ から プレイヤー への命中判定 ●●●●
	private boolean calcNpcPcHit() {

		_hitRate += _npc.getLevel();

		if (_npc instanceof L1PetInstance) { // ペットの武器による追加命中
			_hitRate += ((L1PetInstance) _npc).getHitByWeapon();
		}

		_hitRate += _npc.getHitup();

		int attackerDice = Random.nextInt(20) + 1 + _hitRate - 1;

		// 閃避率
		attackerDice -= _targetPc.getDodge();
		attackerDice += _targetPc.getNdodge();

		int defenderDice = 0;

		int defenderValue = (_targetPc.getAc()) * -1;

		if (_targetPc.getAc() >= 0) {
			defenderDice = 10 - _targetPc.getAc();
		} else if (_targetPc.getAc() < 0) {
			defenderDice = 10 + Random.nextInt(defenderValue) + 1;
		}

		int fumble = _hitRate;
		int critical = _hitRate + 19;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}

		if ((_npc instanceof L1PetInstance)
				|| (_npc instanceof L1SummonInstance)) {
			// 目標在安區、攻擊者在安區、NOPVP
			if ((_targetPc.getZoneType() == 1) || (_npc.getZoneType() == 1)
					|| (_targetPc.checkNonPvP(_targetPc, _npc))) {
				_hitRate = 0;
			}
		}
		// TODO 魔法娃娃效果 - 傷害迴避
		else if (L1MagicDoll.getDamageEvasionByDoll(_targetPc) > 0) {
			_hitRate = 0;
		}

		int rnd = Random.nextInt(100) + 1;

		// NPCの攻撃レンジが10以上の場合で、2以上離れている場合弓攻撃とみなす
		if ((_npc.getAtkRanged() >= 10)
				&& (_hitRate > rnd)
				&& (_npc.getLocation().getTileLineDistance(
						new Point(_targetX, _targetY)) >= 2)) {
			return calcErEvasion();
		}
		return _hitRate >= rnd;
	}

	// ●●●● ＮＰＣ から ＮＰＣ への命中判定 ●●●●
	private boolean calcNpcNpcHit() {

		_hitRate += _npc.getLevel();

		if (_npc instanceof L1PetInstance) { // ペットの武器による追加命中
			_hitRate += ((L1PetInstance) _npc).getHitByWeapon();
		}

		_hitRate += _npc.getHitup();

		int attackerDice = Random.nextInt(20) + 1 + _hitRate - 1;

		// 閃避率
		attackerDice -= _targetNpc.getDodge();
		attackerDice += _targetNpc.getNdodge();

		int defenderDice = 0;

		int defenderValue = (_targetNpc.getAc()) * -1;

		if (_targetNpc.getAc() >= 0) {
			defenderDice = 10 - _targetNpc.getAc();
		} else if (_targetNpc.getAc() < 0) {
			defenderDice = 10 + Random.nextInt(defenderValue) + 1;
		}

		int fumble = _hitRate;
		int critical = _hitRate + 19;

		if (attackerDice <= fumble) {
			_hitRate = 0;
		} else if (attackerDice >= critical) {
			_hitRate = 100;
		} else {
			if (attackerDice > defenderDice) {
				_hitRate = 100;
			} else if (attackerDice <= defenderDice) {
				_hitRate = 0;
			}
		}
		if (((_npc instanceof L1PetInstance) || (_npc instanceof L1SummonInstance))
				&& ((_targetNpc instanceof L1PetInstance) || (_targetNpc instanceof L1SummonInstance))) {
			// 目標在安區、攻擊者在安區、NOPVP
			if ((_targetNpc.getZoneType() == 1) || (_npc.getZoneType() == 1)) {
				_hitRate = 0;
			}
		}

		int rnd = Random.nextInt(100) + 1;
		return _hitRate >= rnd;
	}

	// ●●●● ＥＲによる回避判定 ●●●●
	private boolean calcErEvasion() {
		int er = _targetPc.getEr();

		int rnd = Random.nextInt(100) + 1;
		return er < rnd;
	}

	/* ■■■■■■■■■■■■■■■ ダメージ算出 ■■■■■■■■■■■■■■■ */

	public int calcDamage() {
		if (_calcType == PC_PC) {
			_damage = calcPcPcDamage();
		} else if (_calcType == PC_NPC) {
			_damage = calcPcNpcDamage();
			if (_pc.getDmgMessages()) {
				_pc.sendPackets(new S_SystemMessage("Damage Dealt:" + String.valueOf(_damage)));
			}
		} else if (_calcType == NPC_PC) {
			_damage = calcNpcPcDamage();
		} else if (_calcType == NPC_NPC) {
			_damage = calcNpcNpcDamage();
		}
		return _damage;
	}

	private int calcWeponDamage(int weaponMaxDamage) {
		int weaponDamage = Random.nextInt(weaponMaxDamage) + 1;
		// 判斷魔法輔助
		if (_pc.hasSkillEffect(SOUL_OF_FLAME))
			weaponDamage = weaponMaxDamage;

		// 判斷武器類型
		boolean darkElfWeapon = false ;
		if (_pc.isDarkelf() && (_weaponType == 58)) { // 鋼爪 (追加判斷持有者為黑妖，避免與幻術師奇谷獸相衝)
			darkElfWeapon = true ;
			if ((Random.nextInt(100) + 1) <= _weaponDoubleDmgChance) { // 出現最大值的機率
				weaponDamage = weaponMaxDamage;
			}
			if (weaponDamage == weaponMaxDamage) { // 出現最大值時 - 爪痕
				_effectId = 2;
			}
		} else if (_weaponType == 20 || _weaponType == 62) {// 弓、鐵手甲 不算武器傷害
			weaponDamage = 0;
		}

		weaponDamage +=  _weaponAddDmg + _weaponEnchant ; // 加上武器(額外點數+祝福魔法武器)跟武卷數

		if (_calcType == PC_NPC)
			weaponDamage += calcMaterialBlessDmg(); // 銀祝福武器加傷害
		if (_weaponType == 54) {
			darkElfWeapon = true ;
			if ((Random.nextInt(100) + 1) <= _weaponDoubleDmgChance) { // 雙刀雙擊
				weaponDamage *= 2;
				_effectId = 4;
			}
		}
		weaponDamage += calcAttrEnchantDmg(); // 属性強化傷害

		if (darkElfWeapon && _pc.hasSkillEffect(DOUBLE_BRAKE)) 
			if ((Random.nextInt(100) + 1) <= 33) 
				weaponDamage *= 2;

		return weaponDamage;
	}

	private double calLongRageDamage(double dmg) {
		double longdmg = dmg + _pc.getBowDmgup() + _pc.getOriginalBowDmgup();

		int add_dmg = 1;
		if (_weaponType == 20) { // 弓
			if (_arrow != null) {
				add_dmg = _arrow.getItem().getDmgSmall();
				if (_calcType == PC_NPC) {
					if (_targetNpc.getNpcTemplate().get_size()
							.equalsIgnoreCase("large"))
						add_dmg = _arrow.getItem().getDmgLarge();
					if (_targetNpc.getNpcTemplate().is_hard())
						add_dmg /= 2;
				}
			} else if (_weaponId == 190)  // 沙哈之弓
				add_dmg = 15;
		} else if (_weaponType == 62) { // 鐵手甲
			add_dmg = _sting.getItem().getDmgSmall();
			if (_calcType == PC_NPC)
				if (_targetNpc.getNpcTemplate().get_size()
						.equalsIgnoreCase("large"))
					add_dmg = _sting.getItem().getDmgLarge();
		}
		
		if ( add_dmg > 0) 
			longdmg += Random.nextInt(add_dmg) + 1;

		// 防具增傷
		longdmg += _pc.getBowDmgModifierByArmor();

		if (_pc.hasSkillEffect(COOKING_2_3_N) // 料理
				|| _pc.hasSkillEffect(COOKING_2_3_S)
				|| _pc.hasSkillEffect(COOKING_3_0_N)
				|| _pc.hasSkillEffect(COOKING_3_0_S))
			longdmg += 1;

		return longdmg;
	}

	private double calShortRageDamage(double dmg) {
		double shortdmg = dmg + _pc.getDmgup() + _pc.getOriginalDmgup();
		// 弱點曝光發動判斷
		WeaknessExposure();
		// 近戰魔法增傷
		shortdmg = calcBuffDamage(shortdmg);
		// 防具增傷
		shortdmg += _pc.getDmgModifierByArmor();

		if (_weaponType == 0) // 空手
			shortdmg = (Random.nextInt(5) + 4) / 4;
		else if (_weaponType2 == 17 || _weaponType2 == 19) // 奇古獸
			shortdmg = L1WeaponSkill.getKiringkuDamage(_pc, _target) + calcAttrEnchantDmg();

		if (_pc.hasSkillEffect(COOKING_2_0_N) // 料理
				|| _pc.hasSkillEffect(COOKING_2_0_S)
				|| _pc.hasSkillEffect(COOKING_3_2_N)
				|| _pc.hasSkillEffect(COOKING_3_2_S))
			shortdmg += 1;

		return shortdmg;
	}

	// ●●●● プレイヤー から プレイヤー へのダメージ算出 ●●●●
	public int calcPcPcDamage() {
		// 計算武器總傷害
		int weaponTotalDamage = calcWeponDamage(_weaponSmall);

		if (((_weaponId == 12)||(_weaponId == 262)) && (Random.nextInt(100) + 1 <= 50)) { // ディストラクション装備かつ成功確率(暫定)75%
			weaponTotalDamage += calcDestruction(weaponTotalDamage);
		}

		// 計算 遠程 或 近戰武器 傷害 與魔法、食物buff
		double dmg = weaponTotalDamage + _statusDamage;
		if (_weaponType == 20 || _weaponType == 62)
			dmg = calLongRageDamage(dmg);
		else
			dmg = calShortRageDamage(dmg);

		if ((_weaponId == 2 || _weaponId == 200002)) {
			dmg = L1WeaponSkill.getDiceDaggerDamage(_pc, _target, weapon);
		} else {
			dmg += L1WeaponSkill.getWeaponSkillDamage(
					_pc, _target, _weaponId);
		}
		
		if (_pc.hasSkillEffect(ARM_BREAKER)) { // ＰＣがアームブレイカ—中。
			dmg -= 5;
		}

		dmg -= _targetPc.getDamageReductionByArmor(); // 防具によるダメージ軽減

		// 魔法娃娃效果 - 傷害減免
		dmg -= L1MagicDoll.getDamageReductionByDoll(_targetPc);

		if (_targetPc.hasSkillEffect(COOKING_1_0_S) // 料理によるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_1_1_S)
				|| _targetPc.hasSkillEffect(COOKING_1_2_S)
				|| _targetPc.hasSkillEffect(COOKING_1_3_S)
				|| _targetPc.hasSkillEffect(COOKING_1_4_S)
				|| _targetPc.hasSkillEffect(COOKING_1_5_S)
				|| _targetPc.hasSkillEffect(COOKING_1_6_S)
				|| _targetPc.hasSkillEffect(COOKING_2_0_S)
				|| _targetPc.hasSkillEffect(COOKING_2_1_S)
				|| _targetPc.hasSkillEffect(COOKING_2_2_S)
				|| _targetPc.hasSkillEffect(COOKING_2_3_S)
				|| _targetPc.hasSkillEffect(COOKING_2_4_S)
				|| _targetPc.hasSkillEffect(COOKING_2_5_S)
				|| _targetPc.hasSkillEffect(COOKING_2_6_S)
				|| _targetPc.hasSkillEffect(COOKING_3_0_S)
				|| _targetPc.hasSkillEffect(COOKING_3_1_S)
				|| _targetPc.hasSkillEffect(COOKING_3_2_S)
				|| _targetPc.hasSkillEffect(COOKING_3_3_S)
				|| _targetPc.hasSkillEffect(COOKING_3_4_S)
				|| _targetPc.hasSkillEffect(COOKING_3_5_S)
				|| _targetPc.hasSkillEffect(COOKING_3_6_S)) {
			dmg -= 5;
		}
		if (_targetPc.hasSkillEffect(COOKING_1_7_S) // デザートによるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_2_7_S)
				|| _targetPc.hasSkillEffect(COOKING_3_7_S)) {
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
		if (_targetPc.hasSkillEffect(IMMUNE_TO_HARM)) {
			dmg /= 2;
		}
		// 使用暴擊增加15點傷害，而奇古獸固定15點傷害
		if (_skillId == SMASH) {
			dmg += 15;
			if (_weaponType2 == 17 || _weaponType2 == 19) {
				dmg = 15;
			}
		}
		// 使用骷髏毀壞增加10點傷害，而奇古獸固定10點傷害
		else if (_skillId == BONE_BREAK) {
			dmg += 10;
			if (_weaponType2 == 17 || _weaponType2 == 19) {
				dmg = 10;
			}
			// 再次發動判斷
/*			if (!_targetPc.hasSkillEffect(BONE_BREAK)) {
				int change = Random.nextInt(100) + 1;
				if (change < (30 + Random.nextInt(11))) { // 30 ~ 40%
					L1EffectSpawn.getInstance().spawnEffect(93001, 2000,
							_targetPc.getX(), _targetPc.getY(),
							_targetPc.getMapId());
//					_targetPc.setSkillEffect(BONE_BREAK, 2 * 1000); // 發動後再次發動間隔
																	// 2秒
					_targetPc.setSkillEffect(BONE_BREAK_START, 700);
//				}
			}*/
		}
		if (dmg <= 0) {
			_isHit = false;
			_drainHp = 0; // ダメージ無しの場合は吸収による回復はしない
		}

		return (int) dmg;
	}

	// ●●●● プレイヤー から ＮＰＣ へのダメージ算出 ●●●●
	private int calcPcNpcDamage() {
		int weaponMaxDamage = 0;
		if (_targetNpc.getNpcTemplate().get_size().equalsIgnoreCase("small")
				&& (_weaponSmall > 0)) {
			weaponMaxDamage = _weaponSmall;
		} else if (_targetNpc.getNpcTemplate().get_size()
				.equalsIgnoreCase("large")
				&& (_weaponLarge > 0)) {
			weaponMaxDamage = _weaponLarge;
		}

		// 計算武器總傷害
		int weaponTotalDamage = calcWeponDamage(weaponMaxDamage) ;
		
		if (((_weaponId == 12)||(_weaponId == 262))&& (Random.nextInt(100) + 1 <= 50)) { // ディストラクション装備かつ成功確率(暫定)75%
			weaponTotalDamage += calcDestruction(weaponTotalDamage);
		}

		// 計算傷害 遠程 或 近戰武器 及buff
		double dmg = weaponTotalDamage + _statusDamage;
		if (_weaponType == 20 || _weaponType == 62)
			dmg = calLongRageDamage(dmg);
		else
			dmg = calShortRageDamage(dmg);


		dmg += L1WeaponSkill.getWeaponSkillDamage(_pc, _target, _weaponId);


		if (_pc.hasSkillEffect(ARM_BREAKER)) { // ＰＣがアームブレイカ—中。
			dmg -= 5;
		}
		
		dmg -= calcNpcDamageReduction();

		// 使用暴擊增加15點傷害，而奇古獸固定15點傷害
		if (_skillId == SMASH) {
			dmg += 15;
			if (_weaponType2 == 17 || _weaponType2 == 19) {
				dmg = 15;
			}
		}
		// 使用骷髏毀壞增加10點傷害，而奇古獸固定10點傷害
		else if (_skillId == BONE_BREAK) {
			dmg += 10;
			if (_weaponType2 == 17 || _weaponType2 == 19) {
				dmg = 10;
			}
/*			// 再次發動判斷
			if (!_targetNpc.hasSkillEffect(BONE_BREAK)) {
				int change = Random.nextInt(100) + 1;
				if (change < (30 + Random.nextInt(11))) { // 30 ~ 40%
					L1EffectSpawn.getInstance().spawnEffect(93001, 2000,
							_targetNpc.getX(), _targetNpc.getY(),
							_targetNpc.getMapId());
					_targetNpc.setSkillEffect(BONE_BREAK, 2 * 1000); // 發動後再次發動間隔
																		// 2秒
					_targetNpc.setSkillEffect(BONE_BREAK_START, 700);
				}
			}*/
		}

		// 非攻城區域對寵物、召喚獸傷害減少
		boolean isNowWar = false;
		int castleId = L1CastleLocation.getCastleIdByArea(_targetNpc);
		if (castleId > 0) {
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		if (!isNowWar) {
			if (_targetNpc instanceof L1PetInstance)
				dmg /= 8;
			else if (_targetNpc instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) _targetNpc;
				if (summon.isExsistMaster())
					dmg /= 8;
			}
		}
		if (dmg <= 0) {
			_isHit = false;
			_drainHp = 0; // ダメージ無しの場合は吸収による回復はしない
		}

		return (int) dmg;
	}

	// ●●●● ＮＰＣ から プレイヤー へのダメージ算出 ●●●●
	private int calcNpcPcDamage() {
		int lvl = _npc.getLevel();
		double dmg = 0D;
		if (lvl < 10) {
			dmg = Random.nextInt(lvl) + 10D + _npc.getStr() / 2 + 1;
		} else {
			dmg = Random.nextInt(lvl) + _npc.getStr() / 2 + 1;
		}

		if (_npc instanceof L1PetInstance) {
			dmg += (lvl / 16); // ペットはLV16毎に追加打撃
			dmg += ((L1PetInstance) _npc).getDamageByWeapon();
		}

		dmg += _npc.getDmgup();

		if (isUndeadDamage()) {
			dmg *= 1.1;
		}

		dmg = dmg * getLeverage() / 10;

		dmg -= calcPcDefense();

		if (_npc.isWeaponBreaked()) { // ＮＰＣがウェポンブレイク中。
			dmg /= 2;
		}
		
		if (_npc.hasSkillEffect(ARM_BREAKER)) { // ＮＰＣがアームブレイカ—中。
			dmg -= 5;
		}

		dmg -= _targetPc.getDamageReductionByArmor(); // 防具によるダメージ軽減

		// 魔法娃娃效果 - 傷害減免
		dmg -= L1MagicDoll.getDamageReductionByDoll(_targetPc);

		if (_targetPc.hasSkillEffect(COOKING_1_0_S) // 料理によるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_1_1_S)
				|| _targetPc.hasSkillEffect(COOKING_1_2_S)
				|| _targetPc.hasSkillEffect(COOKING_1_3_S)
				|| _targetPc.hasSkillEffect(COOKING_1_4_S)
				|| _targetPc.hasSkillEffect(COOKING_1_5_S)
				|| _targetPc.hasSkillEffect(COOKING_1_6_S)
				|| _targetPc.hasSkillEffect(COOKING_2_0_S)
				|| _targetPc.hasSkillEffect(COOKING_2_1_S)
				|| _targetPc.hasSkillEffect(COOKING_2_2_S)
				|| _targetPc.hasSkillEffect(COOKING_2_3_S)
				|| _targetPc.hasSkillEffect(COOKING_2_4_S)
				|| _targetPc.hasSkillEffect(COOKING_2_5_S)
				|| _targetPc.hasSkillEffect(COOKING_2_6_S)
				|| _targetPc.hasSkillEffect(COOKING_3_0_S)
				|| _targetPc.hasSkillEffect(COOKING_3_1_S)
				|| _targetPc.hasSkillEffect(COOKING_3_2_S)
				|| _targetPc.hasSkillEffect(COOKING_3_3_S)
				|| _targetPc.hasSkillEffect(COOKING_3_4_S)
				|| _targetPc.hasSkillEffect(COOKING_3_5_S)
				|| _targetPc.hasSkillEffect(COOKING_3_6_S)) {
			dmg -= 5;
		}
		if (_targetPc.hasSkillEffect(COOKING_1_7_S) // デザートによるダメージ軽減
				|| _targetPc.hasSkillEffect(COOKING_2_7_S)
				|| _targetPc.hasSkillEffect(COOKING_3_7_S)) {
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
		if (_targetPc.hasSkillEffect(IMMUNE_TO_HARM)) {
			dmg /= 2;
		}
		// ペット、サモンからプレイヤーに攻撃
		boolean isNowWar = false;
		int castleId = L1CastleLocation.getCastleIdByArea(_targetPc);
		if (castleId > 0) {
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		if (!isNowWar) {
			if (_npc instanceof L1PetInstance) {
				dmg /= 8;
			} else if (_npc instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) _npc;
				if (summon.isExsistMaster()) {
					dmg /= 8;
				}
			}
		}

		if (dmg <= 0) {
			_isHit = false;
		}

		addNpcPoisonAttack(_npc, _targetPc);

		return (int) dmg;
	}

	// ●●●● ＮＰＣ から ＮＰＣ へのダメージ算出 ●●●●
	private int calcNpcNpcDamage() {
		int lvl = _npc.getLevel();
		double dmg = 0;

		if (_npc instanceof L1PetInstance) {
			dmg = Random.nextInt(_npc.getNpcTemplate().get_level())
					+ _npc.getStr() / 2 + 1;
			dmg += (lvl / 16); // ペットはLV16毎に追加打撃
			dmg += ((L1PetInstance) _npc).getDamageByWeapon();
		} else {
			dmg = Random.nextInt(lvl) + _npc.getStr() / 2 + 1;
		}

		if (isUndeadDamage()) {
			dmg *= 1.1;
		}

		dmg = dmg * getLeverage() / 10;
		
		if (_npc.hasSkillEffect(ARM_BREAKER)) { // ＮＰＣがアームブレイカ—中。
			dmg -= 5;
		}

		dmg -= calcNpcDamageReduction();

		if (_npc.isWeaponBreaked()) { // ＮＰＣがウェポンブレイク中。
			dmg /= 2;
		}

		addNpcPoisonAttack(_npc, _targetNpc);

		if (dmg <= 0) {
			_isHit = false;
		}

		return (int) dmg;
	}

	// ●●●● 強化魔法近戰用 ●●●●
	private double calcBuffDamage(double dmg) {
		// 火武器、バーサーカーのダメージは1.5倍しない
		if (_pc.hasSkillEffect(BURNING_SPIRIT)
				|| _pc.hasSkillEffect(ELEMENTAL_FIRE)) {
			if ((Random.nextInt(100) + 1) <= 33) {
				double tempDmg = dmg;
				if (_pc.hasSkillEffect(FIRE_WEAPON)) {
					tempDmg -= 4;
				} else if (_pc.hasSkillEffect(FIRE_BLESS)) {
					tempDmg -= 5;
				} else if (_pc.hasSkillEffect(BURNING_WEAPON)) {
					tempDmg -= 6;
				}
				if (_pc.hasSkillEffect(BERSERKERS)) {
					tempDmg -= 5;
				}
				double diffDmg = dmg - tempDmg;
				dmg = tempDmg * 1.5 + diffDmg;
			}
		}
/*		// 鎖鏈劍
		if (_weaponType2 == 18) {
			// 弱點曝光 - 傷害加成
			if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV3)) {
				dmg += 9;
			} else if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV2)) {
				dmg += 6;
			} else if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV1)) {
				dmg += 3;
			}
		} */
		// 屠宰者 & 弱點曝光LV3 - 傷害 *1.3
		if (_pc.isFoeSlayer()) {
			if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV3)) {
				dmg += 60;
			} else if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV2)) {
				dmg += 40;
			} else if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV1)) {
				dmg += 20;
			}
		}
			
		if (_pc.hasSkillEffect(BURNING_SLASH)) { // 燃燒擊砍
			dmg += 10;
			_pc.sendPackets(new S_EffectLocation(_targetX, _targetY, 6591));
			_pc.broadcastPacket(new S_EffectLocation(_targetX, _targetY, 6591));
			_pc.killSkillEffectTimer(BURNING_SLASH);
		}

		return dmg;
	}

	// ●●●● プレイヤーのＡＣによるダメージ軽減 ●●●●
	private int calcPcDefense() {
		int ac = Math.max(0, 10 - _targetPc.getAc());
		int acDefMax = _targetPc.getClassFeature().getAcDefenseMax(ac);
		return Random.nextInt(acDefMax + 1);
	}

	// ●●●● ＮＰＣのダメージリダクションによる軽減 ●●●●
	private int calcNpcDamageReduction() {
		return _targetNpc.getNpcTemplate().get_damagereduction();
	}

	// ●●●● 武器の材質と祝福による追加ダメージ算出 ●●●●
	private int calcMaterialBlessDmg() {
		int damage = 0;
		int undead = _targetNpc.getNpcTemplate().get_undead();
		if (((_weaponMaterial == 14) || (_weaponMaterial == 17) || (_weaponMaterial == 22))
				&& ((undead == 1) || (undead == 3) || (undead == 5))) { // 銀・ミスリル・オリハルコン、かつ、アンデッド系・アンデッド系ボス・銀特効モンスター
			damage += Random.nextInt(20) + 1;
		} else if (((_weaponMaterial == 17) || (_weaponMaterial == 22))
				&& (undead == 2)) { // ミスリル・オリハルコン、かつ、悪魔系
			damage += Random.nextInt(3) + 1;
		}
		if ((_weaponBless == 0)
				&& ((undead == 1) || (undead == 2) || (undead == 3))) { // 祝福武器、かつ、アンデッド系・悪魔系・アンデッド系ボス
			damage += Random.nextInt(4) + 1;
		}
		if ((_pc.getWeapon() != null) && (_weaponType != 20)
				&& (_weaponType != 62) && (weapon.getHolyDmgByMagic() != 0)
				&& ((undead == 1) || (undead == 3))) {
			damage += weapon.getHolyDmgByMagic();
		}
		return damage;
	}

	// ●●●● 武器の属性強化による追加ダメージ算出 ●●●●
	private int calcAttrEnchantDmg() {
		int damage = 0;
		// int weakAttr = _targetNpc.getNpcTemplate().get_weakAttr();
		// if ((weakAttr & 1) == 1 && _weaponAttrEnchantKind == 1 // 地
		// || (weakAttr & 2) == 2 && _weaponAttrEnchantKind == 2 // 火
		// || (weakAttr & 4) == 4 && _weaponAttrEnchantKind == 4 // 水
		// || (weakAttr & 8) == 8 && _weaponAttrEnchantKind == 8) { // 風
		// damage = _weaponAttrEnchantLevel;
		// }
		if (_weaponAttrEnchantLevel == 1) {
			damage = 1;
		} else if (_weaponAttrEnchantLevel == 2) {
			damage = 3;
		} else if (_weaponAttrEnchantLevel == 3) {
			damage = 5;
		}

		// XXX 耐性処理は本来、耐性合計値ではなく、各値を個別に処理して総和する。
		int resist = 0;
		if (_calcType == PC_PC) {
			if (_weaponAttrEnchantKind == 1) { // 地
				resist = _targetPc.getEarth();
			} else if (_weaponAttrEnchantKind == 2) { // 火
				resist = _targetPc.getFire();
			} else if (_weaponAttrEnchantKind == 4) { // 水
				resist = _targetPc.getWater();
			} else if (_weaponAttrEnchantKind == 8) { // 風
				resist = _targetPc.getWind();
			}
		} else if (_calcType == PC_NPC) {
			int weakAttr = _targetNpc.getNpcTemplate().get_weakAttr();
			if (((_weaponAttrEnchantKind == 1) && (weakAttr == 1)) // 地
					|| ((_weaponAttrEnchantKind == 2) && (weakAttr == 2)) // 火
					|| ((_weaponAttrEnchantKind == 4) && (weakAttr == 4)) // 水
					|| ((_weaponAttrEnchantKind == 8) && (weakAttr == 8))) { // 風
				resist = -50;
			}
		}

		int resistFloor = (int) (0.32 * Math.abs(resist));
		if (resist >= 0) {
			resistFloor *= 1;
		} else {
			resistFloor *= -1;
		}

		double attrDeffence = resistFloor / 32.0;
		double attrCoefficient = 1 - attrDeffence;

		damage *= attrCoefficient;

		return damage;
	}

	// ●●●● ＮＰＣのアンデッドの夜間攻撃力の変化 ●●●●
	private boolean isUndeadDamage() {
		boolean flag = false;
		int undead = _npc.getNpcTemplate().get_undead();
		boolean isNight = L1GameTimeClock.getInstance().currentTime().isNight();
		if (isNight && ((undead == 1) || (undead == 3) || (undead == 4))) { // 18～6時、かつ、アンデッド系・アンデッド系ボス・弱点無効のアンデッド系
			flag = true;
		}
		return flag;
	}

	// ●●●● ＮＰＣの毒攻撃を付加 ●●●●
	private void addNpcPoisonAttack(L1Character attacker, L1Character target) {
		if (_npc.getNpcTemplate().get_poisonatk() != 0) { // 毒攻撃あり
			if (15 >= Random.nextInt(100) + 1) { // 15%の確率で毒攻撃
				if (_npc.getNpcTemplate().get_poisonatk() == 1) { // 通常毒
					// 3秒周期でダメージ5
					L1DamagePoison.doInfection(attacker, target, 3000, 5);
				} else if (_npc.getNpcTemplate().get_poisonatk() == 2) { // 沈黙毒
					L1SilencePoison.doInfection(target);
				} else if (_npc.getNpcTemplate().get_poisonatk() == 4) { // 麻痺毒
					// 20秒後に45秒間麻痺
					L1ParalysisPoison.doInfection(target, 20000, 45000);
				}
			}
		} else if (_npc.getNpcTemplate().get_paralysisatk() != 0) { // 麻痺攻撃あり
		}
	}

	// ■■■■ マナスタッフ、鋼鉄のマナスタッフ、マナバーラードのMP吸収量算出 ■■■■
	public void calcStaffOfMana() {
		if ((_weaponId == 126) || (_weaponId == 127)) { // SOMまたは鋼鉄のSOM
			int som_lvl = _weaponEnchant + 3; // 最大MP吸収量を設定
			if (som_lvl < 0) {
				som_lvl = 0;
			}
			// MP吸収量をランダム取得
			_drainMana = Random.nextInt(som_lvl) + 1;
			// 最大MP吸収量を9に制限
			if (_drainMana > Config.MANA_DRAIN_LIMIT_PER_SOM_ATTACK) {
				_drainMana = Config.MANA_DRAIN_LIMIT_PER_SOM_ATTACK;
			}
		} else if (_weaponId == 259) { // マナバーラード
			if (_calcType == PC_PC) {
				if (_targetPc.getMr() <= Random.nextInt(100) + 1) { // 確率はターゲットのMRに依存
					_drainMana = 1; // 吸収量は1固定
				}
			} else if (_calcType == PC_NPC) {
				if (_targetNpc.getMr() <= Random.nextInt(100) + 1) { // 確率はターゲットのMRに依存
					_drainMana = 1; // 吸収量は1固定
				}
			}
		}
	}

	// ■■■■ ディストラクションのHP吸収量算出 ■■■■
	private int calcDestruction(int dmg) {
		_drainHp = (dmg / 8) + 1;
		return _drainHp > 0 ? _drainHp : 1;
	}

	// ■■■■ ＰＣの毒攻撃を付加 ■■■■
	public void addPcPoisonAttack(L1Character attacker, L1Character target) {
		int chance = Random.nextInt(100) + 1;
		if (((_weaponId == 13) || (_weaponId == 44 // FOD、古代のダークエルフソード
				) || ((_weaponId != 0) && _pc.hasSkillEffect(ENCHANT_VENOM))) // エンチャント
																				// ベノム中
				&& (chance <= 10)) {
			// 通常毒、3秒周期、ダメージHP-5
			L1DamagePoison.doInfection(attacker, target, 3000, 5);
		} else {
			// 魔法娃娃效果 - 中毒
			if (L1MagicDoll.getEffectByDoll(attacker, (byte) 1) == 1) {
				L1DamagePoison.doInfection(attacker, target, 3000, 5);
			}
		}
	}

	// ■■■■ 底比斯武器攻撃付加 ■■■■
	public void addChaserAttack() {
		if (5 > Random.nextInt(100) + 1) {
			if (_weaponId == 265 || _weaponId == 266 || _weaponId == 267
					|| _weaponId == 268 || _weaponId == 280 || _weaponId == 281) {
				L1Chaser chaser = new L1Chaser(_pc, _target,
						L1Skills.ATTR_EARTH, 7025);
				chaser.begin();
			} else if (_weaponId == 276 || _weaponId == 277) { 
				L1Chaser chaser = new L1Chaser(_pc, _target,
						L1Skills.ATTR_WATER, 7179);
				chaser.begin();
			} else if (_weaponId == 304 || _weaponId == 307 || _weaponId == 308) { 
				L1Chaser chaser = new L1Chaser(_pc, _target,
						L1Skills.ATTR_WATER, 8150);
				chaser.begin();
			} else if (_weaponId == 305 || _weaponId == 306 || _weaponId == 309) { 
				L1Chaser chaser = new L1Chaser(_pc, _target,
						L1Skills.ATTR_WATER, 8152);
				chaser.begin();
			}
		}
	}

	/* ■■■■■■■■■■■■■■ 攻撃モーション送信 ■■■■■■■■■■■■■■ */
	public void action() {
		if (_calcType == PC_PC || _calcType == PC_NPC) {
			actionPc();
		} else if (_calcType == NPC_PC || _calcType == NPC_NPC) {
			actionNpc();
		}
	}

	// ●●●● ＰＣ攻擊動作 ●●●●
	public void actionPc() {
		_attckActId = 1;
		boolean isFly = false;
		_pc.setHeading(_pc.targetDirection(_targetX, _targetY)); // 改變面向

		if (_weaponType == 20 && (_arrow != null || _weaponId == 190)) { // 弓 有箭或沙哈之弓
			if (_arrow != null) { // 弓 - 有箭
				_pc.getInventory().removeItem(_arrow, 1);
				_attckGrfxId = 66; // 箭
			} else if (_weaponId == 190)  // 沙哈 - 無箭
				_attckGrfxId = 2349; // 魔法箭
			
			if (_pc.getTempCharGfx() == 8719)  // 柑橘
				_attckGrfxId = 8721; // 橘子籽
			
			if (_pc.getTempCharGfx() == 8900)  // 海露拜
				_attckGrfxId = 8904; // 魔法箭

			if (_pc.getTempCharGfx() == 8913)  // 朱里安
				_attckGrfxId = 8916; // 魔法箭
			
			isFly = true;
		} else if ((_weaponType == 62) && (_sting != null)) { // 鐵手甲 - 有飛刀
			_pc.getInventory().removeItem(_sting, 1);
			_attckGrfxId = 2989; // 飛刀
			isFly = true;
		}

		if (!_isHit) { // Miss
			_damage = 0;
		}

		int[] data = null;

		if (isFly) { // 遠距離攻擊
			data = new int[] { _attckActId, _damage, _attckGrfxId };
			_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, _targetX,
					_targetY, data));
			_pc.broadcastPacket(new S_UseArrowSkill(_pc, _targetId, _targetX,
					_targetY, data));
		} else { // 近距離攻擊
			data = new int[] { _attckActId, _damage, _effectId };
			_pc.sendPackets(new S_AttackPacket(_pc, _targetId, data));
			_pc.broadcastPacket(new S_AttackPacket(_pc, _targetId, data));
		}

		if (_isHit) {
			_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(
					_targetId, ActionCodes.ACTION_Damage), _pc);
		}
	}

	// ●●●● ＮＰＣ攻擊動作 ●●●●
	private void actionNpc() {
		int bowActId = 0;
		int npcGfxid = _npc.getTempCharGfx();
		int actId = L1NpcDefaultAction.getInstance().getSpecialAttack(npcGfxid); // 特殊攻擊動作
		double dmg = _damage;
		int[] data = null;

		_npc.setHeading(_npc.targetDirection(_targetX, _targetY)); // 改變面向

		// 與目標距離2格以上
		boolean isLongRange = false;
		if (npcGfxid == 4521 || npcGfxid == 4550 || npcGfxid == 5062 || npcGfxid == 5317
				|| npcGfxid == 5324 || npcGfxid == 5331 || npcGfxid == 5338 || npcGfxid == 5412) {
			isLongRange = (_npc.getLocation().getTileLineDistance(
					new Point(_targetX, _targetY)) > 2);
		} else {
			isLongRange = (_npc.getLocation().getTileLineDistance(
					new Point(_targetX, _targetY)) > 1);
		}
		bowActId = _npc.getPolyArrowGfx(); // 被變身後的遠距圖像
		if (bowActId == 0) {
			bowActId = _npc.getNpcTemplate().getBowActId();
		}
		if (getActId() == 0) {
			if ((actId != 0) && ((Random.nextInt(100) + 1) <= 40)) {
				dmg *= 1.2;
			} else {
				if (!isLongRange || bowActId == 0) { // 近距離
					actId = L1NpcDefaultAction.getInstance().getDefaultAttack(npcGfxid);
					if (bowActId > 0) { // 遠距離怪物，近距離時攻擊力加成
						dmg *= 1.2;
					}
				} else { // 遠距離
					actId = L1NpcDefaultAction.getInstance().getRangedAttack(npcGfxid);
				}
			}
		} else {
			actId = getActId(); // 攻擊動作由 mobskill控制
		}
		_damage = (int) dmg;

		if (!_isHit) { // Miss
			_damage = 0;
		}

		// 距離2格以上攻使用 弓 攻擊
		if (isLongRange && (bowActId > 0)) {
			data = new int[] { actId, _damage, bowActId }; // data = {actid,
															// dmg, spellgfx}
			_npc.broadcastPacket(new S_UseArrowSkill(_npc, _targetId, _targetX,
					_targetY, data));
		} else {
			if (getGfxId() > 0) {
				data = new int[] { actId, _damage, getGfxId(), 6 }; // data =
																	// {actid,
																	// dmg,
																	// spellgfx,
																	// use_type}
				_npc.broadcastPacket(new S_UseAttackSkill(_npc, _targetId,
						_targetX, _targetY, data));
			} else {
				data = new int[] { actId, _damage, 0 }; // data = {actid, dmg,
														// effect}
				_npc.broadcastPacket(new S_AttackPacket(_npc, _targetId, data));
			}
		}
		if (_isHit) {
			_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(
					_targetId, ActionCodes.ACTION_Damage), _npc);
		}
	}

	/*
	 * // 飛び道具（矢、スティング）がミスだったときの軌道を計算 public void calcOrbit(int cx, int cy, int
	 * head) // 起点Ｘ 起点Ｙ 今向いてる方向 { float dis_x = Math.abs(cx - _targetX); //
	 * Ｘ方向のターゲットまでの距離 float dis_y = Math.abs(cy - _targetY); // Ｙ方向のターゲットまでの距離
	 * float dis = Math.max(dis_x, dis_y); // ターゲットまでの距離 float avg_x = 0; float
	 * avg_y = 0; if (dis == 0) { // 目標と同じ位置なら向いてる方向へ真っ直ぐ if (head == 1) { avg_x
	 * = 1; avg_y = -1; } else if (head == 2) { avg_x = 1; avg_y = 0; } else if
	 * (head == 3) { avg_x = 1; avg_y = 1; } else if (head == 4) { avg_x = 0;
	 * avg_y = 1; } else if (head == 5) { avg_x = -1; avg_y = 1; } else if (head
	 * == 6) { avg_x = -1; avg_y = 0; } else if (head == 7) { avg_x = -1; avg_y
	 * = -1; } else if (head == 0) { avg_x = 0; avg_y = -1; } } else { avg_x =
	 * dis_x / dis; avg_y = dis_y / dis; }
	 * 
	 * int add_x = (int) Math.floor((avg_x * 15) + 0.59f); // 上下左右がちょっと優先な丸め int
	 * add_y = (int) Math.floor((avg_y * 15) + 0.59f); // 上下左右がちょっと優先な丸め
	 * 
	 * if (cx > _targetX) { add_x *= -1; } if (cy > _targetY) { add_y *= -1; }
	 * 
	 * _targetX = _targetX + add_x; _targetY = _targetY + add_y; }
	 */

	/* ■■■■■■■■■■■■■■■ 計算結果反映 ■■■■■■■■■■■■■■■ */

	public void commit() {
		if (_isHit) {
			if ((_calcType == PC_PC) || (_calcType == NPC_PC)) {
				commitPc();
			} else if ((_calcType == PC_NPC) || (_calcType == NPC_NPC)) {
				commitNpc();
			}
		}

		// ダメージ値及び命中率確認用メッセージ
		if (!Config.ALT_ATKMSG) {
			return;
		}
		if (Config.ALT_ATKMSG) {
			if (((_calcType == PC_PC) || (_calcType == PC_NPC)) && !_pc.isGm()) {
				return;
			}
			if (((_calcType == PC_PC) || (_calcType == NPC_PC))
					&& !_targetPc.isGm()) {
				return;
			}
		}
		String msg0 = "";
		String msg1 = " 造成 ";
		String msg2 = "";
		String msg3 = "";
		String msg4 = "";
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) { // アタッカーがＰＣの場合
			msg0 = "物攻 對";
		} else if (_calcType == NPC_PC) { // アタッカーがＮＰＣの場合
			msg0 = _npc.getNameId() + "(物攻)：";
		}

		if ((_calcType == NPC_PC) || (_calcType == PC_PC)) { // ターゲットがＰＣの場合
			msg4 = _targetPc.getName();
			msg2 = "，剩餘 " + _targetPc.getCurrentHp() + "，命中	" + _hitRate + "%";
		} else if (_calcType == PC_NPC) { // ターゲットがＮＰＣの場合
			msg4 = _targetNpc.getNameId();
			msg2 = "，剩餘 " + _targetNpc.getCurrentHp() + "，命中 " + _hitRate + "%";
		}
		msg3 = _isHit ? _damage + " 傷害" : "  0 傷害";

		// 物攻 對 目標 造成 X 傷害，剩餘 Y，命中 Z %。
		if ((_calcType == PC_PC) || (_calcType == PC_NPC)) {
			_pc.sendPackets(new S_ServerMessage(166, msg0, msg1, msg2, msg3,
					msg4));
		}
		// 攻擊者(物攻)： X傷害，剩餘 Y，命中%。
		else if ((_calcType == NPC_PC)) {
			_targetPc.sendPackets(new S_ServerMessage(166, msg0, null, msg2,
					msg3, null));
		}
	}

	// ●●●● プレイヤーに計算結果を反映 ●●●●
	private void commitPc() {
		if (_calcType == PC_PC) {
			if ((_drainMana > 0) && (_targetPc.getCurrentMp() > 0)) {
				if (_drainMana > _targetPc.getCurrentMp()) {
					_drainMana = _targetPc.getCurrentMp();
				}
				short newMp = (short) (_targetPc.getCurrentMp() - _drainMana);
				_targetPc.setCurrentMp(newMp);
				newMp = (short) (_pc.getCurrentMp() + _drainMana);
				_pc.setCurrentMp(newMp);
			}
			if (_drainHp > 0) { // HP吸収による回復
				short newHp = (short) (_pc.getCurrentHp() + _drainHp);
				_pc.setCurrentHp(newHp);
			}
			damagePcWeaponDurability(); // 武器を損傷させる。
			_targetPc.receiveDamage(_pc, _damage, false);
		} else if (_calcType == NPC_PC) {
			_targetPc.receiveDamage(_npc, _damage, false);
		}
	}

	// ●●●● ＮＰＣに計算結果を反映 ●●●●
	private void commitNpc() {
		if (_calcType == PC_NPC) {
			if (_drainMana > 0) {
				int drainValue = _targetNpc.drainMana(_drainMana);
				int newMp = _pc.getCurrentMp() + drainValue;
				_pc.setCurrentMp(newMp);
				if (drainValue > 0) {
					int newMp2 = _targetNpc.getCurrentMp() - drainValue;
					_targetNpc.setCurrentMpDirect(newMp2);
				}
			}
			if (_drainHp > 0) { // HP吸収による回復
				short newHp = (short) (_pc.getCurrentHp() + _drainHp);
				_pc.setCurrentHp(newHp);
			}
			damageNpcWeaponDurability(); // 武器を損傷させる。
			_targetNpc.receiveDamage(_pc, _damage);
		} else if (_calcType == NPC_NPC) {
			_targetNpc.receiveDamage(_npc, _damage);
		}
	}

	/* ■■■■■■■■■■■■■■■ カウンターバリア ■■■■■■■■■■■■■■■ */

	// ■■■■ カウンターバリア時の攻撃モーション送信 ■■■■
	public void actionCounterBarrier() {
		if (_calcType == PC_PC) {
			_pc.setHeading(_pc.targetDirection(_targetX, _targetY)); // 向きのセット
			_pc.sendPackets(new S_AttackMissPacket(_pc, _targetId));
			_pc.broadcastPacket(new S_AttackMissPacket(_pc, _targetId));
			_pc.sendPackets(new S_DoActionGFX(_pc.getId(),
					ActionCodes.ACTION_Damage));
			_pc.broadcastPacket(new S_DoActionGFX(_pc.getId(),
					ActionCodes.ACTION_Damage));
		} else if (_calcType == NPC_PC) {
			int actId = 0;
			_npc.setHeading(_npc.targetDirection(_targetX, _targetY)); // 向きのセット
			if (getActId() > 0) {
				actId = getActId();
			} else {
				actId = ActionCodes.ACTION_Attack;
			}
			if (getGfxId() > 0) {
				int[] data = { actId, 0, getGfxId(), 6 }; // data = {actId, dmg, getGfxId(), use_type}
				_npc.broadcastPacket(new S_UseAttackSkill(_target,
						_npc.getId(), _targetX, _targetY, data));
			} else {
				_npc.broadcastPacket(new S_AttackMissPacket(_npc, _targetId,
						actId));
			}
			_npc.broadcastPacket(new S_DoActionGFX(_npc.getId(),
					ActionCodes.ACTION_Damage));
		}
	}

	// ■■■■ 相手の攻撃に対してカウンターバリアが有効かを判別 ■■■■
	public boolean isShortDistance() {
		boolean isShortDistance = true;
		if (_calcType == PC_PC) {
			if ((_weaponType == 20) || (_weaponType == 62) || _weaponType2 == 17 || _weaponType2 == 19) { // 弓かガントレット
				isShortDistance = false;
			}
		} else if (_calcType == NPC_PC) {
			boolean isLongRange = (_npc.getLocation().getTileLineDistance(
					new Point(_targetX, _targetY)) > 1);
			int bowActId = _npc.getPolyArrowGfx();
			if (bowActId == 0) {
				bowActId = _npc.getNpcTemplate().getBowActId();
			}
			// 距離が2以上、攻撃者の弓のアクションIDがある場合は遠攻撃
			if (isLongRange && (bowActId > 0)) {
				isShortDistance = false;
			}
		}
		return isShortDistance;
	}

	// ■■■■ カウンターバリアのダメージを反映 ■■■■
	public void commitCounterBarrier() {
		int damage = calcCounterBarrierDamage();
		if (damage == 0) {
			return;
		}
		if (_calcType == PC_PC) {
			_pc.receiveDamage(_targetPc, damage, false);
		} else if (_calcType == NPC_PC) {
			_npc.receiveDamage(_targetPc, damage);
		}
	}

	// ●●●● カウンターバリアのダメージを算出 ●●●●
	private int calcCounterBarrierDamage() {
		int damage = 0;
		L1ItemInstance weapon = null;
		weapon = _targetPc.getWeapon();
		if (weapon != null) {
			if (weapon.getItem().getType() == 3) { // 両手剣
				// (BIG最大ダメージ+強化数+追加ダメージ)*2
				damage = (weapon.getItem().getDmgLarge()
						+ weapon.getEnchantLevel() + weapon.getItem()
						.getDmgModifier()) * 2;
			}
		}
		return damage;
	}
	
	public static boolean isImmune(L1Character character) {
		for (int skillId : PREVENT_DAMAGE)
			if (character.hasSkillEffect(skillId))
				return true;
		return false;			
	}

	/*
	 * 武器を損傷させる。 対NPCの場合、損傷確率は10%とする。祝福武器は3%とする。
	 */
	private void damageNpcWeaponDurability() {
		int chance = 10;
		int bchance = 3;

		/*
		 * 損傷しないNPC、素手、損傷しない武器使用、SOF中の場合何もしない。
		 */
		if ((_calcType != PC_NPC)
				|| (_targetNpc.getNpcTemplate().is_hard() == false)
				|| (_weaponType == 0) || (weapon.getItem().get_canbedmg() == 0)
				|| _pc.hasSkillEffect(SOUL_OF_FLAME)) {
			return;
		}
		// 通常の武器・呪われた武器
		if (((_weaponBless == 1) || (_weaponBless == 2))
				&& ((Random.nextInt(100) + 1) < chance)) {
			// \f1あなたの%0が損傷しました。
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon);
		}
		// 祝福された武器
		if ((_weaponBless == 0) && ((Random.nextInt(100) + 1) < bchance)) {
			// \f1あなたの%0が損傷しました。
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon);
		}
	}

	/*
	 * バウンスアタックにより武器を損傷させる。 バウンスアタックの損傷確率は10%
	 */
	private void damagePcWeaponDurability() {
		// PvP以外、素手、弓、ガントトレット、相手がバウンスアタック未使用、SOF中の場合何もしない
		if ((_calcType != PC_PC) || (_weaponType == 0) || (_weaponType == 20)
				|| (_weaponType == 62)
				|| (_targetPc.hasSkillEffect(BOUNCE_ATTACK) == false)
				|| _pc.hasSkillEffect(SOUL_OF_FLAME)) {
			return;
		}

		if (Random.nextInt(100) + 1 <= 10) {
			// \f1あなたの%0が損傷しました。
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon);
		}
	}

	private static final S_SkillIconGFX Weakness1 = new S_SkillIconGFX(75, 1);
	private static final S_SkillIconGFX Weakness2 = new S_SkillIconGFX(75, 2);
	private static final S_SkillIconGFX Weakness3 = new S_SkillIconGFX(75, 3);
	
	/** 弱點曝光 */
	private void WeaknessExposure() {
		if (weapon != null) {
			int random = Random.nextInt(100) + 1;
			if (_weaponType2 == 18) { // 鎖鏈劍
				// 使用屠宰者...
				if (_pc.isFoeSlayer()) {
					return;
				}
				int weaponWeaknessExposureChance = 30;
				if (random <= weaponWeaknessExposureChance) {
					if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV3)) { 
						// Level 3 duration can not be overwritten
					} else if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV2)) { 
						_pc.killSkillEffectTimer(SPECIAL_EFFECT_WEAKNESS_LV2);
						_pc.setSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV3, 16000);
						_pc.sendPackets(Weakness3);
					} else if (_pc.hasSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV1)) { 
						_pc.killSkillEffectTimer(SPECIAL_EFFECT_WEAKNESS_LV1);
						_pc.setSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV2, 16000);
						_pc.sendPackets(Weakness2);
					} else {
						_pc.setSkillEffect(SPECIAL_EFFECT_WEAKNESS_LV1, 16000);
						_pc.sendPackets(Weakness1);
					}
				}
			}
		}
	}
}
