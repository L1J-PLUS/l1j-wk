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
package l1j.server.server.model.trap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.IdFactory;
import l1j.server.server.datatables.TrapTable;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1TrapInstance;
import l1j.server.server.types.Point;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.utils.collections.Lists;

public class L1WorldTraps {
	private static Logger _log = Logger.getLogger(L1WorldTraps.class.getName());

	private List<L1TrapInstance> _allTraps = Lists.newList();

	private List<L1TrapInstance> _allBases = Lists.newList();

	private Timer _timer = new Timer();

	private static L1WorldTraps _instance;

	private L1WorldTraps() {
		initialize();
	}

	public static L1WorldTraps getInstance() {
		if (_instance == null) {
			_instance = new L1WorldTraps();
		}
		return _instance;
	}

	private void initialize() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();

			pstm = con.prepareStatement("SELECT * FROM spawnlist_trap");

			rs = pstm.executeQuery();

			while (rs.next()) {
				int trapId = rs.getInt("trapId");
				L1Trap trapTemp = TrapTable.getInstance().getTemplate(trapId);
				L1Location loc = new L1Location();
				loc.setMap(rs.getInt("mapId"));
				loc.setX(rs.getInt("locX"));
				loc.setY(rs.getInt("locY"));
				Point rndPt = new Point();
				rndPt.setX(rs.getInt("locRndX"));
				rndPt.setY(rs.getInt("locRndY"));
				int count = rs.getInt("count");
				int span = rs.getInt("span");

				for (int i = 0; i < count; i++) {
					L1TrapInstance trap = new L1TrapInstance(IdFactory.getInstance().nextId(), trapTemp, loc, rndPt, span);
					L1World.getInstance().addVisibleObject(trap);
					_allTraps.add(trap);
				}
				L1TrapInstance base = new L1TrapInstance(IdFactory.getInstance().nextId(), loc);
				L1World.getInstance().addVisibleObject(base);
				_allBases.add(base);
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
	}

	public static void reloadTraps() {
		TrapTable.reload();
		L1WorldTraps oldInstance = _instance;
		_instance = new L1WorldTraps();
		oldInstance.resetTimer();
		removeTraps(oldInstance._allTraps);
		removeTraps(oldInstance._allBases);
	}

	private static void removeTraps(List<L1TrapInstance> traps) {
		for (L1TrapInstance trap : traps) {
			trap.disableTrap();
			L1World.getInstance().removeVisibleObject(trap);
		}
	}

	private void resetTimer() {
		synchronized (this) {
			_timer.cancel();
			_timer = new Timer();
		}
	}

	private void disableTrap(L1TrapInstance trap) {
		trap.disableTrap();

		synchronized (this) {
			_timer.schedule(new TrapSpawnTimer(trap), trap.getSpan());
		}
	}

	public void resetAllTraps() {
		for (L1TrapInstance trap : _allTraps) {
			trap.resetLocation();
			trap.enableTrap();
		}
	}

	public void onPlayerMoved(L1PcInstance player) {
		L1Location loc = player.getLocation();

		for (L1TrapInstance trap : _allTraps) {
			if (trap.isEnable() && loc.equals(trap.getLocation())) {
				trap.onTrod(player);
				disableTrap(trap);
			}
		}
	}

	public void onDetection(L1PcInstance caster) {
		L1Location loc = caster.getLocation();

		for (L1TrapInstance trap : _allTraps) {
			if (trap.isEnable() && loc.isInScreen(trap.getLocation())) {
				trap.onDetection(caster);
				disableTrap(trap);
			}
		}
	}

	private class TrapSpawnTimer extends TimerTask {
		private final L1TrapInstance _targetTrap;

		public TrapSpawnTimer(L1TrapInstance trap) {
			_targetTrap = trap;
		}

		@Override
		public void run() {
			_targetTrap.resetLocation();
			_targetTrap.enableTrap();
		}
	}
}
