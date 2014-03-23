/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.utils.SQLUtil;

public class LogShopBuy {
	private static Logger _log = Logger.getLogger(LogShopBuy.class.getName());

	public void storeLogShopBuy(L1PcInstance pc, L1ItemInstance item, int cnt, int adenabefore,
			int adenaafter, int itemprice) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("INSERT INTO LogShopBuy VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			Date time = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time.getTime());
			pstm.setString(1, fm);
			pstm.setString(2, pc.getNetConnection().getIp());
			pstm.setString(3, pc.getAccountName());
			pstm.setInt(4, pc.getId());
			pstm.setString(5, pc.getName());
			pstm.setInt(6, item.getId());
			pstm.setString(7, item.getItem().getName());
			pstm.setInt(8, item.getEnchantLevel());
			pstm.setInt(9, item.getCount());
			pstm.setInt(10, adenabefore);
			pstm.setInt(11, adenaafter);
			int adenadiff = adenabefore - adenaafter;
			if (adenadiff < 0) {
				adenadiff = -adenadiff;
			}
			pstm.setInt(12, adenadiff);
			pstm.setInt(13, itemprice);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
