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
package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.action.Effect;
import l1j.server.server.model.skill.L1BuffUtil;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.utils.SQLUtil;
import static l1j.server.server.model.skill.L1SkillId.*;

public class CharBuffTable {
	private CharBuffTable() {
	}

	private static Logger _log = Logger
			.getLogger(CharBuffTable.class.getName());

	private static final int[] buffSkill = { 2, 67,
		3, 99, 151, 159, 168,
		52, 101, 150,
		26, 42, 109, 110,
		114, 115, 117,
		148, 155, 163,
		149, 156, 166,
		DRESS_EVASION, // 迴避提升
		RESIST_FEAR, // 恐懼無助
		MIRROR_IMAGE, UNCANNY_DODGE, // 鏡像、暗影閃避
		43, 54, STATUS_HASTE, // 一段加速
		STATUS_BRAVE, STATUS_ELFBRAVE, STATUS_RIBRAVE, STATUS_BRAVE2, // 二段加速
		STATUS_THIRD_SPEED, // 三段加速
		STATUS_BLUE_POTION, STATUS_CHAT_PROHIBITED, // 藍水，禁言
		COOKING_1_0_N, COOKING_1_0_S, COOKING_1_1_N, COOKING_1_1_S, // 魔法料理
		COOKING_1_2_N, COOKING_1_2_S, COOKING_1_3_N, COOKING_1_3_S,
		COOKING_1_4_N, COOKING_1_4_S, COOKING_1_5_N, COOKING_1_5_S,
		COOKING_1_6_N, COOKING_1_6_S, COOKING_2_0_N, COOKING_2_0_S,
		COOKING_2_1_N, COOKING_2_1_S, COOKING_2_2_N, COOKING_2_2_S,
		COOKING_2_3_N, COOKING_2_3_S, COOKING_2_4_N, COOKING_2_4_S,
		COOKING_2_5_N, COOKING_2_5_S, COOKING_2_6_N, COOKING_2_6_S,
		COOKING_3_0_N, COOKING_3_0_S, COOKING_3_1_N, COOKING_3_1_S,
		COOKING_3_2_N, COOKING_3_2_S, COOKING_3_3_N, COOKING_3_3_S,
		COOKING_3_4_N, COOKING_3_4_S, COOKING_3_5_N, COOKING_3_5_S,
		COOKING_3_6_N, COOKING_3_6_S,
		EFFECT_POTION_OF_EXP_150, EFFECT_POTION_OF_EXP_175, // 神力藥水
		EFFECT_POTION_OF_EXP_200, EFFECT_POTION_OF_EXP_225,
		EFFECT_POTION_OF_EXP_250, EFFECT_POTION_OF_BATTLE, // 戰鬥藥水
		EFFECT_BLESS_OF_MAZU, // 媽祖的祝福
		EFFECT_ENCHANTING_BATTLE, EFFECT_STRENGTHENING_HP, EFFECT_STRENGTHENING_MP, // 強化戰鬥卷軸、體力增強卷軸、魔力增強卷軸
		COOKING_WONDER_DRUG, // 象牙塔妙藥
		EFFECT_BLOODSTAIN_OF_ANTHARAS, EFFECT_BLOODSTAIN_OF_FAFURION, // 龍之血痕
		EFFECT_MAGIC_STONE_A_1, EFFECT_MAGIC_STONE_A_2, EFFECT_MAGIC_STONE_A_3, // 附魔石
		EFFECT_MAGIC_STONE_A_4, EFFECT_MAGIC_STONE_A_5, EFFECT_MAGIC_STONE_A_6,
		EFFECT_MAGIC_STONE_A_7, EFFECT_MAGIC_STONE_A_8, EFFECT_MAGIC_STONE_A_9,
		EFFECT_MAGIC_STONE_B_1, EFFECT_MAGIC_STONE_B_2, EFFECT_MAGIC_STONE_B_3,
		EFFECT_MAGIC_STONE_B_4, EFFECT_MAGIC_STONE_B_5, EFFECT_MAGIC_STONE_B_6,
		EFFECT_MAGIC_STONE_B_7, EFFECT_MAGIC_STONE_B_8, EFFECT_MAGIC_STONE_B_9,
		EFFECT_MAGIC_STONE_C_1, EFFECT_MAGIC_STONE_C_2, EFFECT_MAGIC_STONE_C_3,
		EFFECT_MAGIC_STONE_C_4, EFFECT_MAGIC_STONE_C_5, EFFECT_MAGIC_STONE_C_6,
		EFFECT_MAGIC_STONE_C_7, EFFECT_MAGIC_STONE_C_8, EFFECT_MAGIC_STONE_C_9,
		EFFECT_MAGIC_STONE_D_1, EFFECT_MAGIC_STONE_D_2, EFFECT_MAGIC_STONE_D_3,
		EFFECT_MAGIC_STONE_D_4, EFFECT_MAGIC_STONE_D_5, EFFECT_MAGIC_STONE_D_6,
		EFFECT_MAGIC_STONE_D_7, EFFECT_MAGIC_STONE_D_8, EFFECT_MAGIC_STONE_D_9,
		EFFECT_MAGIC_EYE_OF_AHTHARTS, EFFECT_MAGIC_EYE_OF_FAFURION, EFFECT_MAGIC_EYE_OF_LINDVIOR, // 魔眼
		EFFECT_MAGIC_EYE_OF_VALAKAS, EFFECT_MAGIC_EYE_OF_BIRTH, EFFECT_MAGIC_EYE_OF_FIGURE,
		EFFECT_MAGIC_EYE_OF_LIFE,
		EFFECT_BLESS_OF_CRAY, EFFECT_BLESS_OF_SAELL // 卡瑞、莎爾的祝福
	};

	private static void StoreBuff(int objId, int skillId, int time, int polyId) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("INSERT INTO character_buff SET char_obj_id=?, skill_id=?, remaining_time=?, poly_id=?");
			pstm.setInt(1, objId);
			pstm.setInt(2, skillId);
			pstm.setInt(3, time);
			pstm.setInt(4, polyId);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public static void DeleteBuff(L1PcInstance pc) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("DELETE FROM character_buff WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);

		}
	}

	public static void SaveBuff(L1PcInstance pc) {
		for (int skillId : buffSkill) {
			int timeSec = pc.getSkillEffectTimeSec(skillId);
			if (0 < timeSec) {
				int polyId = 0;
				if (skillId == SHAPE_CHANGE) {
					polyId = pc.getTempCharGfx();
				}
				StoreBuff(pc.getId(), skillId, timeSec, polyId);
			}
		}
	}

	public static void buffRemainingTime(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM character_buff WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				int skillid = rs.getInt("skill_id");
				int remaining_time = rs.getInt("remaining_time");
				switch (skillid) {
					case STATUS_RIBRAVE: // 生命之樹果實
					case DRESS_EVASION: // 迴避提升
						remaining_time = remaining_time / 4;
						pc.setSkillEffect(skillid, remaining_time * 4 * 1000);
						break;
					case COOKING_WONDER_DRUG: // 象牙塔妙藥
						pc.addHpr(10);
						pc.addMpr(2);
						remaining_time = remaining_time / 4;
						pc.setSkillEffect(skillid, remaining_time * 4 * 1000);
						break;
					case EFFECT_BLESS_OF_MAZU: // 媽祖的祝福
					case EFFECT_ENCHANTING_BATTLE : // 強化戰鬥卷軸
					case EFFECT_STRENGTHENING_HP: // 體力增強卷軸
					case EFFECT_STRENGTHENING_MP: // 魔力增強卷軸
						remaining_time = remaining_time / 16;
						Effect.useEffect(pc, skillid, remaining_time * 16);
						break;
					case EFFECT_POTION_OF_BATTLE: // 戰鬥藥水
					case EFFECT_POTION_OF_EXP_150: // 神力藥水
					case EFFECT_POTION_OF_EXP_175:
					case EFFECT_POTION_OF_EXP_200:
					case EFFECT_POTION_OF_EXP_225:
					case EFFECT_POTION_OF_EXP_250:
						remaining_time = remaining_time / 16;
						pc.setSkillEffect(skillid, remaining_time * 16 * 1000);
						break;
					case EFFECT_MAGIC_EYE_OF_AHTHARTS: // 魔眼
					case EFFECT_MAGIC_EYE_OF_FAFURION:
					case EFFECT_MAGIC_EYE_OF_LINDVIOR:
					case EFFECT_MAGIC_EYE_OF_VALAKAS:
					case EFFECT_MAGIC_EYE_OF_BIRTH:
					case EFFECT_MAGIC_EYE_OF_FIGURE:
					case EFFECT_MAGIC_EYE_OF_LIFE:
						remaining_time = remaining_time / 32;
						Effect.useEffect(pc, skillid, remaining_time * 32);
						break;
					case RESIST_FEAR: // 恐懼無助
						remaining_time = remaining_time / 4;
						pc.addNdodge((byte) 5); // 閃避率 - 50%
						// 更新閃避率顯示
						pc.sendPackets(new S_PacketBox(101, pc.getNdodge()));
						pc.setSkillEffect(skillid, remaining_time * 4 * 1000);
						break;
					case EFFECT_BLESS_OF_CRAY: // 卡瑞、莎爾的祝福
					case EFFECT_BLESS_OF_SAELL:
						remaining_time = remaining_time / 32;
						L1BuffUtil.effectBlessOfDragonSlayer(pc, skillid, remaining_time * 32, 0);
						break;
					default:
						if (skillid >= EFFECT_MAGIC_STONE_A_1 && skillid <= EFFECT_MAGIC_STONE_D_9) { // 附魔石
							remaining_time = remaining_time / 32;
							Effect.magicStoneEffect(pc, skillid, remaining_time * 32);
							break;
						}
						break;
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

}
