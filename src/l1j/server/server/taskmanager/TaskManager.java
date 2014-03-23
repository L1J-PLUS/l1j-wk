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
package l1j.server.server.taskmanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.taskmanager.tasks.TaskRestart;
import l1j.server.server.taskmanager.tasks.TaskShutdown;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.utils.collections.Lists;
import l1j.server.server.utils.collections.Maps;

/**
 * @author Layane
 * 
 */
public final class TaskManager {
	protected static final Logger _log = Logger.getLogger(TaskManager.class.getName());

	private static TaskManager _instance;

	protected static final String[] SQL_STATEMENTS =
	{ "SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks", "UPDATE global_tasks SET last_activation=? WHERE id=?",
			"SELECT id FROM global_tasks WHERE task=?",
			"INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)" };

	private final Map<Integer, Task> _tasks = Maps.newMap();

	protected final List<ExecutedTask> _currentTasks = Lists.newList();

	public class ExecutedTask implements Runnable {
		int _id;

		long _lastActivation;

		Task _task;

		TaskTypes _type;

		String[] _params;

		ScheduledFuture<?> _scheduled;

		public ExecutedTask(Task task, TaskTypes type, ResultSet rset) throws SQLException {
			_task = task;
			_type = type;
			_id = rset.getInt("id");
			_lastActivation = rset.getLong("last_activation");
			_params = new String[]
			{ rset.getString("param1"), rset.getString("param2"), rset.getString("param3") };
		}

		@Override
		public void run() {
			_task.onTimeElapsed(this);

			_lastActivation = System.currentTimeMillis();

			java.sql.Connection con = null;
			PreparedStatement pstm = null;
			try {
				con = L1DatabaseFactory.getInstance().getConnection();
				pstm = con.prepareStatement(SQL_STATEMENTS[1]);
				pstm.setLong(1, _lastActivation);
				pstm.setInt(2, _id);
				pstm.executeUpdate();
			}
			catch (SQLException e) {
				_log.warning("cannot updated the Global Task " + _id + ": " + e.getMessage());
			}
			finally {
				SQLUtil.close(pstm);
				SQLUtil.close(con);
			}

		}

		@Override
		public boolean equals(Object object) {
			return _id == ((ExecutedTask) object)._id;
		}

		public Task getTask() {
			return _task;
		}

		public TaskTypes getType() {
			return _type;
		}

		public int getId() {
			return _id;
		}

		public String[] getParams() {
			return _params;
		}

		public long getLastActivation() {
			return _lastActivation;
		}

		public void stopTask() {
			_task.onDestroy();

			if (_scheduled != null) {
				_scheduled.cancel(true);
			}

			_currentTasks.remove(this);
		}

	}

	public static TaskManager getInstance() {
		if (_instance == null) {
			_instance = new TaskManager();
		}
		return _instance;
	}

	public TaskManager() {
		initializate();
		startAllTasks();
	}

	private void initializate() {
		registerTask(new TaskRestart());
		registerTask(new TaskShutdown());
	}

	public void registerTask(Task task) {
		int key = task.getName().hashCode();
		if (!_tasks.containsKey(key)) {
			_tasks.put(key, task);
			task.initializate();
		}
	}

	private void startAllTasks() {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement(SQL_STATEMENTS[0]);
			rs = pstm.executeQuery();

			while (rs.next()) {
				Task task = _tasks.get(rs.getString("task").trim().toLowerCase().hashCode());

				if (task == null) {
					continue;
				}

			}

		}
		catch (Exception e) {
			_log.log(Level.SEVERE, "error while loading Global Task table", e);
		}
		finally {
			if (null != rs) {
				try {
					rs.close();
				}
				catch (SQLException ignore) {
					// ignore
				}
				rs = null;
			}

			if (null != pstm) {
				try {
					pstm.close();
				}
				catch (SQLException ignore) {
					// ignore
				}
				pstm = null;
			}

			if (null != con) {
				try {
					con.close();
				}
				catch (SQLException ignore) {
					// ignore
				}
				con = null;
			}
		}

	}

	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3) {
		return addUniqueTask(task, type, param1, param2, param3, 0);
	}

	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement(SQL_STATEMENTS[2]);
			pstm.setString(1, task);
			rs = pstm.executeQuery();

			if (!rs.next()) {
				pstm = con.prepareStatement(SQL_STATEMENTS[3]);
				pstm.setString(1, task);
				pstm.setString(2, type.toString());
				pstm.setLong(3, lastActivation);
				pstm.setString(4, param1);
				pstm.setString(5, param2);
				pstm.setString(6, param3);
				pstm.execute();
			}

			return true;
		}
		catch (SQLException e) {
			_log.warning("cannot add the unique task: " + e.getMessage());
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

		return false;
	}

	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3) {
		return addTask(task, type, param1, param2, param3, 0);
	}

	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement(SQL_STATEMENTS[3]);
			pstm.setString(1, task);
			pstm.setString(2, type.toString());
			pstm.setLong(3, lastActivation);
			pstm.setString(4, param1);
			pstm.setString(5, param2);
			pstm.setString(6, param3);
			pstm.execute();

			return true;
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, "cannot add the task", e);
		}
		finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

		return false;
	}

}
