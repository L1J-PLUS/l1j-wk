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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1Spawn;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.NumberUtil;
import l1j.server.server.utils.PerformanceTimer;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.utils.collections.Maps;

public class SpawnTable {
	private static Logger _log = Logger.getLogger(SpawnTable.class.getName());

	private static SpawnTable _instance;

	private Map<Integer, L1Spawn> _spawntable = Maps.newMap();

	private int _highestId;

	public static SpawnTable getInstance() {
		if (_instance == null) {
			_instance = new SpawnTable();
		}
		return _instance;
	}

	private SpawnTable() {
		PerformanceTimer timer = new PerformanceTimer();
		System.out.print("spawning mob...");
		fillSpawnTable();
		_log.config("モンスター配置リスト " + _spawntable.size() + "件ロード");
		System.out.println("OK! " + timer.get() + " ms");
	}

	private void fillSpawnTable() {

		int spawnCount = 0;
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist");
			rs = pstm.executeQuery();

			L1Spawn spawnDat;
			L1Npc template1;
			while (rs.next()) {
				if (Config.ALT_HALLOWEENIVENT == false) {
					int npcid = rs.getInt("id");
					if ((npcid >= 26656) && (npcid <= 26734)) {
						continue;
					}
				}
				int npcTemplateId = rs.getInt("npc_templateid");
				template1 = NpcTable.getInstance().getTemplate(npcTemplateId);
				int count;

				if (template1 == null) {
					_log.warning("mob data for id:" + npcTemplateId + " missing in npc table");
					spawnDat = null;
				}
				else {
					if (rs.getInt("count") == 0) {
						continue;
					}
					double amount_rate = MapsTable.getInstance().getMonsterAmount(rs.getShort("mapid"));
					count = calcCount(template1, rs.getInt("count"), amount_rate);
					if (count == 0) {
						continue;
					}

					spawnDat = new L1Spawn(template1);
					spawnDat.setId(rs.getInt("id"));
					spawnDat.setAmount(count);
					spawnDat.setGroupId(rs.getInt("group_id"));
					spawnDat.setLocX(rs.getInt("locx"));
					spawnDat.setLocY(rs.getInt("locy"));
					spawnDat.setRandomx(rs.getInt("randomx"));
					spawnDat.setRandomy(rs.getInt("randomy"));
					spawnDat.setLocX1(rs.getInt("locx1"));
					spawnDat.setLocY1(rs.getInt("locy1"));
					spawnDat.setLocX2(rs.getInt("locx2"));
					spawnDat.setLocY2(rs.getInt("locy2"));
					spawnDat.setHeading(rs.getInt("heading"));
					spawnDat.setMinRespawnDelay(rs.getInt("min_respawn_delay"));
					spawnDat.setMaxRespawnDelay(rs.getInt("max_respawn_delay"));
					spawnDat.setMapId(rs.getShort("mapid"));
					spawnDat.setRespawnScreen(rs.getBoolean("respawn_screen"));
					spawnDat.setMovementDistance(rs.getInt("movement_distance"));
					spawnDat.setRest(rs.getBoolean("rest"));
					spawnDat.setSpawnType(rs.getInt("near_spawn"));
					// our version of truly randomized spawn, currently not working with l1j-tw
//					spawnDat.setSpawnHome(rs.getInt("spawn_home"));
//					spawnDat.setSpawnHomeRange(rs.getInt("spawn_home_range"));
	//				spawnDat.setSpawnHomeCount(rs.getInt("spawn_home_count"));
	//				spawnDat.setSpawnHomeDelay(rs.getInt("spawn_home_delay"));

					spawnDat.setName(template1.get_name());

					if ((count > 1) && (spawnDat.getLocX1() == 0)) {
						// 複数かつ固定spawnの場合は、個体数 * 6 の範囲spawnに変える。
						// ただし範囲が30を超えないようにする
						int range = Math.min(count * 6, 30);
						spawnDat.setLocX1(spawnDat.getLocX() - range);
						spawnDat.setLocY1(spawnDat.getLocY() - range);
						spawnDat.setLocX2(spawnDat.getLocX() + range);
						spawnDat.setLocY2(spawnDat.getLocY() + range);
					}

					// start the spawning
					spawnDat.init();
					spawnCount += spawnDat.getAmount();
				}

				_spawntable.put(new Integer(spawnDat.getId()), spawnDat);
				if (spawnDat.getId() > _highestId) {
					_highestId = spawnDat.getId();
				}
			}

		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		_log.fine("総モンスター数 " + spawnCount + "匹");
	}

	public L1Spawn getTemplate(int Id) {
		return _spawntable.get(new Integer(Id));
	}

	public void addNewSpawn(L1Spawn spawn) {
		_highestId++;
		spawn.setId(_highestId);
		_spawntable.put(new Integer(spawn.getId()), spawn);
	}

	public static void storeSpawn(L1PcInstance pc, L1Npc npc) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			int count = 1;
			int randomXY = 12;
			int minRespawnDelay = 60;
			int maxRespawnDelay = 120;
			String note = npc.get_name();

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("INSERT INTO spawnlist SET location=?,count=?,npc_templateid=?,group_id=?,locx=?,locy=?,randomx=?,randomy=?,heading=?,min_respawn_delay=?,max_respawn_delay=?,mapid=?");
			pstm.setString(1, note);
			pstm.setInt(2, count);
			pstm.setInt(3, npc.get_npcId());
			pstm.setInt(4, 0);
			pstm.setInt(5, pc.getX());
			pstm.setInt(6, pc.getY());
			pstm.setInt(7, randomXY);
			pstm.setInt(8, randomXY);
			pstm.setInt(9, pc.getHeading());
			pstm.setInt(10, minRespawnDelay);
			pstm.setInt(11, maxRespawnDelay);
			pstm.setInt(12, pc.getMapId());
			pstm.execute();

		}
		catch (Exception e) {
			NpcTable._log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private static int calcCount(L1Npc npc, int count, double rate) {
		if (rate == 0) {
			return 0;
		}
		if ((rate == 1) || npc.isAmountFixed()) {
			return count;
		}
		else {
			return NumberUtil.randomRound((count * rate));
		}

	}
}
