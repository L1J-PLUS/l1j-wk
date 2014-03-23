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

import static l1j.server.server.model.skill.L1SkillId.COUNTER_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.SHOCK_STUN;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.ActionCodes;
import l1j.server.server.IdFactory;
import l1j.server.server.datatables.MobSkillTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.datatables.SprTable;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_NPCPack;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1MobSkill;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.Random;
import l1j.server.server.utils.collections.Lists;
import l1j.server.server.utils.collections.Maps;

public class L1MobSkillUse {
	private static Logger _log = Logger.getLogger(L1MobSkillUse.class.getName());

	private L1MobSkill _mobSkillTemplate = null;

	private L1NpcInstance _attacker = null;

	private L1Character _target = null;

	private int _sleepTime = 0;

	private int _skillUseCount[];

	public L1MobSkillUse(L1NpcInstance npc) {
		_sleepTime = 0;

		_mobSkillTemplate = MobSkillTable.getInstance().getTemplate(npc.getNpcTemplate().get_npcId());
		if (_mobSkillTemplate == null) {
			return;
		}
		_attacker = npc;
		_skillUseCount = new int[getMobSkillTemplate().getSkillSize()];
	}

	private int getSkillUseCount(int idx) {
		return _skillUseCount[idx];
	}

	private void skillUseCountUp(int idx) {
		_skillUseCount[idx]++;
	}

	public void resetAllSkillUseCount() {
		if (getMobSkillTemplate() == null) {
			return;
		}

		for (int i = 0; i < getMobSkillTemplate().getSkillSize(); i++) {
			_skillUseCount[i] = 0;
		}
	}

	public int getSleepTime() {
		return _sleepTime;
	}

	public void setSleepTime(int i) {
		_sleepTime = i;
	}

	public L1MobSkill getMobSkillTemplate() {
		return _mobSkillTemplate;
	}

	/*
	 * トリガーの条件のみチェック。
	 */
	public boolean isSkillTrigger(L1Character tg) {
		if (_mobSkillTemplate == null) {
			return false;
		}
		_target = tg;

		int type;
		type = getMobSkillTemplate().getType(0);

		if (type == L1MobSkill.TYPE_NONE) {
			return false;
		}

		for (int i = 0; (i < getMobSkillTemplate().getSkillSize()) && (getMobSkillTemplate().getType(i) != L1MobSkill.TYPE_NONE); i++) {
			if (isSkillUseble(i, false)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * スキル攻撃 スキル攻撃可能ならばtrueを返す。 攻撃できなければfalseを返す。
	 */
	public boolean skillUse(L1Character tg, boolean isTriRnd) {
		if (_mobSkillTemplate == null) {
			return false;
		}
		_target = tg;

		int type;
		type = getMobSkillTemplate().getType(0);

		if (type == L1MobSkill.TYPE_NONE) {
			return false;
		}

		int[] skills = null;
		int skillSizeCounter = 0;
		int skillSize = getMobSkillTemplate().getSkillSize();
		if (skillSize >= 0) {
			skills = new int[skillSize];
		}

		for (int i = 0; (i < getMobSkillTemplate().getSkillSize()) && (getMobSkillTemplate().getType(i) != L1MobSkill.TYPE_NONE); i++) {
			if (isSkillUseble(i, isTriRnd) == false) {
				continue;
			}
			else { // 条件にあうスキルが存在する
				skills[skillSizeCounter] = i;
				skillSizeCounter++;
			}
		}

		if (skillSizeCounter != 0) {
			int num = Random.nextInt(skillSizeCounter);
			if (useSkill(skills[num])) { // スキル使用
				return true;
			}
		}

		return false;
	}

	private boolean useSkill(int i) {
		// 對自身施法判斷
		int changeType = getMobSkillTemplate().getChangeTarget(i);
		if (changeType == 2) {
			_target = changeTarget(changeType, i);
		}

		boolean isUseSkill = false;
		int type = getMobSkillTemplate().getType(i);
		if (type == L1MobSkill.TYPE_PHYSICAL_ATTACK) { // 物理攻撃
			if (physicalAttack(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		else if (type == L1MobSkill.TYPE_MAGIC_ATTACK) { // 魔法攻撃
			if (magicAttack(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		else if (type == L1MobSkill.TYPE_SUMMON) { // サモンする
			if (summon(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		else if (type == L1MobSkill.TYPE_POLY) { // 強制変身させる
			if (poly(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		return isUseSkill;
	}

	private boolean summon(int idx) {
		int summonId = getMobSkillTemplate().getSummon(idx);
		int min = getMobSkillTemplate().getSummonMin(idx);
		int max = getMobSkillTemplate().getSummonMax(idx);
		int count = 0;
		int actId = getMobSkillTemplate().getActid(idx);
		int gfxId = getMobSkillTemplate().getGfxid(idx);

		if (summonId == 0) {
			return false;
		}

		// 施法動作
		if (actId > 0) {
			S_DoActionGFX gfx = new S_DoActionGFX(_attacker.getId(), actId);
			_attacker.broadcastPacket(gfx);
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);
		}
		// 魔方陣
		if (gfxId > 0) {
			_attacker.broadcastPacket(new S_SkillSound(_attacker.getId(), gfxId));
		}
		count = Random.nextInt(max) + min;
		mobspawn(summonId, count);
		return true;
	}

	/*
	 * 15セル以内で射線が通るPCを指定したモンスターに強制変身させる。 対PCしか使えない。
	 */
	private boolean poly(int idx) {
		int polyId = getMobSkillTemplate().getPolyId(idx);
		int actId = getMobSkillTemplate().getActid(idx);
		boolean usePoly = false;

		if (polyId == 0) {
			return false;
		}
		// 施法動作
		if (actId > 0) {
			S_DoActionGFX gfx = new S_DoActionGFX(_attacker.getId(), actId);
			_attacker.broadcastPacket(gfx);
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);
		}

		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(_attacker)) {
			if (pc.isDead()) { // 死亡
				continue;
			}
			if (pc.isGhost()) {
				continue;
			}
			if (pc.isGmInvis()) {
				continue;
			}
			if (_attacker.glanceCheck(pc.getX(), pc.getY()) == false) {
				continue; // 射線が通らない
			}

			switch (_attacker.getNpcTemplate().get_npcId()) {
				case 81082: // 火焰之影
					pc.getInventory().takeoffEquip(945); // 將目標裝備卸下。
					break;
				default:
					break;
			}
			_attacker.broadcastPacket(new S_SkillSound(pc.getId(), 230));
			L1PolyMorph.doPoly(pc, polyId, 1800, L1PolyMorph.MORPH_BY_NPC);
			usePoly = true;
		}
		return usePoly;
	}

	private boolean magicAttack(int idx) {
		L1SkillUse skillUse = new L1SkillUse();
		int skillid = getMobSkillTemplate().getSkillId(idx);
		int actId = getMobSkillTemplate().getActid(idx);
		int gfxId = getMobSkillTemplate().getGfxid(idx);
		int mpConsume = getMobSkillTemplate().getMpConsume(idx);
		boolean canUseSkill = false;

		if (skillid > 0) {
			skillUse.setSkillRanged(getMobSkillTemplate().getRange(idx)); // 變更技能施放距離
			skillUse.setSkillRanged(getMobSkillTemplate().getSkillArea(idx)); // 變更技能施放範圍
			canUseSkill = skillUse.checkUseSkill(null, skillid, _target.getId(), _target.getX(), _target.getY(), null, 0, L1SkillUse.TYPE_NORMAL,
					_attacker, actId, gfxId, mpConsume);
		}

		L1Skills skill = SkillsTable.getInstance().getTemplate(skillid);
		if (skill.getTarget().equals("buff") && _target.hasSkillEffect(skillid) && skillid!=SHOCK_STUN) {
			return false;
		}

		if (canUseSkill == true) {
			if (getMobSkillTemplate().getLeverage(idx) > 0) {
				skillUse.setLeverage(getMobSkillTemplate().getLeverage(idx));
			}
			skillUse.handleCommands(null, skillid, _target.getId(), _target.getX(), _target.getX(), null, 0, L1SkillUse.TYPE_NORMAL, _attacker);

			// 延遲時間判斷
			if (actId == 0) {
				actId = skill.getActionId();
			}
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);

			return true;
		}
		return false;
	}

	/*
	 * 物理攻撃
	 */
	private boolean physicalAttack(int idx) {
		Map<Integer, Integer> targetList = Maps.newConcurrentMap();
		int areaWidth = getMobSkillTemplate().getAreaWidth(idx);
		int areaHeight = getMobSkillTemplate().getAreaHeight(idx);
		int range = getMobSkillTemplate().getRange(idx);
		int actId = getMobSkillTemplate().getActid(idx);
		int gfxId = getMobSkillTemplate().getGfxid(idx);

		// レンジ外
		if (_attacker.getLocation().getTileLineDistance(_target.getLocation()) > range) {
			return false;
		}

		// 障害物がある場合攻撃不可能
		if (!_attacker.glanceCheck(_target.getX(), _target.getY())) {
			return false;
		}

		_attacker.setHeading(_attacker.targetDirection(_target.getX(), _target.getY())); // 向きのセット

		if (areaHeight > 0) {
			// 範囲攻撃
			List<L1Object> objs = L1World.getInstance().getVisibleBoxObjects(_attacker, _attacker.getHeading(), areaWidth, areaHeight);

			for (L1Object obj : objs) {
				if (!(obj instanceof L1Character)) { // ターゲットがキャラクター以外の場合何もしない。
					continue;
				}

				L1Character cha = (L1Character) obj;
				if (cha.isDead()) { // 死んでるキャラクターは対象外
					continue;
				}

				// ゴースト状態は対象外
				if (cha instanceof L1PcInstance) {
					if (((L1PcInstance) cha).isGhost()) {
						continue;
					}
				}

				// 障害物がある場合は対象外
				if (!_attacker.glanceCheck(cha.getX(), cha.getY())) {
					continue;
				}

				if ((_target instanceof L1PcInstance) || (_target instanceof L1SummonInstance) || (_target instanceof L1PetInstance)) {
					// 対PC
					if (((obj instanceof L1PcInstance) && !((L1PcInstance) obj).isGhost() && !((L1PcInstance) obj).isGmInvis())
							|| (obj instanceof L1SummonInstance) || (obj instanceof L1PetInstance)) {
						targetList.put(obj.getId(), 0);
					}
				}
				else {
					// 対NPC
					if (obj instanceof L1MonsterInstance) {
						targetList.put(obj.getId(), 0);
					}
				}
			}
		}
		else {
			// 単体攻撃
			targetList.put(_target.getId(), 0); // ターゲットのみ追加
		}

		if (targetList.isEmpty()) {
			return false;
		}

		Iterator<Integer> ite = targetList.keySet().iterator();
		while (ite.hasNext()) {
			int targetId = ite.next();
			L1Attack attack = new L1Attack(_attacker, (L1Character) L1World.getInstance().findObject(targetId));
			if (_target.hasSkillEffect(COUNTER_BARRIER)) {
				L1Magic magic = new L1Magic(_target, _attacker);
				if (magic.calcProbabilityMagic(COUNTER_BARRIER) &&
						_attacker.getLocation().getTileLineDistance(_target.getLocation()) <= 2 &&
						gfxId == 0) {
					attack.actionCounterBarrier();
					attack.commitCounterBarrier();
					continue;
				}
			}
			if (attack.calcHit()) {
				if (getMobSkillTemplate().getLeverage(idx) > 0) {
					attack.setLeverage(getMobSkillTemplate().getLeverage(idx));
				}
				attack.calcDamage();
			}
			if (actId > 0) {
				attack.setActId(actId);
			}
			// 攻撃モーションは実際のターゲットに対してのみ行う
			if (targetId == _target.getId()) {
				if (gfxId > 0) {
					_attacker.broadcastPacket(new S_SkillSound(_attacker.getId(), gfxId));
				}
				attack.action();
			}
			attack.commit();
		}

		if (actId > 0) {
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);
		} else {
			_sleepTime = _attacker.getAtkspeed();
		}
		return true;
	}

	/*
	 * トリガーの条件のみチェック
	 */
	private boolean isSkillUseble(int skillIdx, boolean isTriRnd) {
		boolean useble = false;
		int type = getMobSkillTemplate().getType(skillIdx);
		int chance = Random.nextInt(100) + 1;

		if (chance > getMobSkillTemplate().getTriggerRandom(skillIdx)) {
			return false;
		}

		if (isTriRnd || (type == L1MobSkill.TYPE_SUMMON) || (type == L1MobSkill.TYPE_POLY)) {
			/*if (getMobSkillTemplate().getTriggerRandom(skillIdx) > 0) {
				int chance = Random.nextInt(100) + 1;
				if (chance < getMobSkillTemplate().getTriggerRandom(skillIdx)) {
					useble = true;
				}
				else {
					return false;
				}
			}*/ // 確定此修改後的模式是仿正的，再移除此註解掉的程式碼
			useble = true;
		}

		if (getMobSkillTemplate().getTriggerHp(skillIdx) > 0) {
			int hpRatio = (_attacker.getCurrentHp() * 100) / _attacker.getMaxHp();
			if (hpRatio <= getMobSkillTemplate().getTriggerHp(skillIdx)) {
				useble = true;
			}
			else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerCompanionHp(skillIdx) > 0) {
			L1NpcInstance companionNpc = searchMinCompanionHp();
			if (companionNpc == null) {
				return false;
			}

			int hpRatio = (companionNpc.getCurrentHp() * 100) / companionNpc.getMaxHp();
			if (hpRatio <= getMobSkillTemplate().getTriggerCompanionHp(skillIdx)) {
				useble = true;
				_target = companionNpc; // ターゲットの入れ替え
			}
			else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerRange(skillIdx) != 0) {
			int distance = _attacker.getLocation().getTileLineDistance(_target.getLocation());

			if (getMobSkillTemplate().isTriggerDistance(skillIdx, distance)) {
				useble = true;
			}
			else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerCount(skillIdx) > 0) {
			if (getSkillUseCount(skillIdx) < getMobSkillTemplate().getTriggerCount(skillIdx)) {
				useble = true;
			}
			else {
				return false;
			}
		}
		return useble;
	}

	private L1NpcInstance searchMinCompanionHp() {
		L1NpcInstance npc;
		L1NpcInstance minHpNpc = null;
		int hpRatio = 100;
		int companionHpRatio;
		int family = _attacker.getNpcTemplate().get_family();

		for (L1Object object : L1World.getInstance().getVisibleObjects(_attacker)) {
			if (object instanceof L1NpcInstance) {
				npc = (L1NpcInstance) object;
				if (npc.getNpcTemplate().get_family() == family) {
					companionHpRatio = (npc.getCurrentHp() * 100) / npc.getMaxHp();
					if (companionHpRatio < hpRatio) {
						hpRatio = companionHpRatio;
						minHpNpc = npc;
					}
				}
			}
		}
		return minHpNpc;
	}

	private void mobspawn(int summonId, int count) {
		int i;

		for (i = 0; i < count; i++) {
			mobspawn(summonId);
		}
	}

	private void mobspawn(int summonId) {
		try {
			L1Npc spawnmonster = NpcTable.getInstance().getTemplate(summonId);
			if (spawnmonster != null) {
				L1NpcInstance mob = null;
				try {
					String implementationName = spawnmonster.getImpl();
					Constructor<?> _constructor = Class.forName(
							(new StringBuilder()).append("l1j.server.server.model.Instance.").append(implementationName).append("Instance")
									.toString()).getConstructors()[0];
					mob = (L1NpcInstance) _constructor.newInstance(new Object[]
					{ spawnmonster });
					mob.setId(IdFactory.getInstance().nextId());
					L1Location loc = _attacker.getLocation().randomLocation(8, false);
					int heading = Random.nextInt(8);
					mob.setX(loc.getX());
					mob.setY(loc.getY());
					mob.setHomeX(loc.getX());
					mob.setHomeY(loc.getY());
					short mapid = _attacker.getMapId();
					mob.setMap(mapid);
					mob.setHeading(heading);
					L1World.getInstance().storeObject(mob);
					L1World.getInstance().addVisibleObject(mob);
					L1Object object = L1World.getInstance().findObject(mob.getId());
					L1MonsterInstance newnpc = (L1MonsterInstance) object;
					newnpc.set_storeDroped(true); // 召喚怪不會掉落道具
					if (newnpc.getTempCharGfx() == 145) { // 史巴托
						newnpc.setStatus(11);
						newnpc.broadcastPacket(new S_NPCPack(newnpc));
						newnpc.broadcastPacket(new S_DoActionGFX(newnpc.getId(), ActionCodes.ACTION_Appear));
						newnpc.setStatus(0);
						newnpc.broadcastPacket(new S_CharVisualUpdate(newnpc, newnpc.getStatus()));
					} else if (newnpc.getTempCharGfx() == 7591) { // 泥龍(地)
						newnpc.broadcastPacket(new S_NPCPack(newnpc));
						newnpc.broadcastPacket(new S_DoActionGFX(newnpc.getId(), ActionCodes.ACTION_AxeWalk));
					}
					newnpc.onNpcAI();
					newnpc.turnOnOffLight();
					newnpc.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
				}
				catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// 現在ChangeTargetで有効な値は2,3のみ
	private L1Character changeTarget(int type, int idx) {
		L1Character target;

		switch (type) {
			case L1MobSkill.CHANGE_TARGET_ME:
				target = _attacker;
				break;
			case L1MobSkill.CHANGE_TARGET_RANDOM:
				// ターゲット候補の選定
				List<L1Character> targetList = Lists.newList();
				for (L1Object obj : L1World.getInstance().getVisibleObjects(_attacker)) {
					if ((obj instanceof L1PcInstance) || (obj instanceof L1PetInstance) || (obj instanceof L1SummonInstance)) {
						L1Character cha = (L1Character) obj;

						int distance = _attacker.getLocation().getTileLineDistance(cha.getLocation());

						// 発動範囲外のキャラクターは対象外
						if (!getMobSkillTemplate().isTriggerDistance(idx, distance)) {
							continue;
						}

						// 障害物がある場合は対象外
						if (!_attacker.glanceCheck(cha.getX(), cha.getY())) {
							continue;
						}

						if (!_attacker.getHateList().containsKey(cha)) { // ヘイトがない場合対象外
							continue;
						}

						if (cha.isDead()) { // 死んでるキャラクターは対象外
							continue;
						}

						// ゴースト状態は対象外
						if (cha instanceof L1PcInstance) {
							if (((L1PcInstance) cha).isGhost()) {
								continue;
							}
						}
						targetList.add((L1Character) obj);
					}
				}

				if (targetList.isEmpty()) {
					target = _target;
				}
				else {
					int randomSize = targetList.size() * 100;
					int targetIndex = Random.nextInt(randomSize) / 100;
					target = targetList.get(targetIndex);
				}
				break;

			default:
				target = _target;
				break;
		}
		return target;
	}
}
