/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.command.executor;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;


public class CheckIP implements L1CommandExecutor {

	private CheckIP() {}

	public static L1CommandExecutor getInstance() {
		return new CheckIP();
	}

	@Override
	public void execute(L1PcInstance user, String commandName, String target) {
		try {
			ipCheck(user, target);
		} catch (Exception e) {
			e.printStackTrace();
			user.sendPackets(new S_SystemMessage(".ipCheck <name>"));
		}
	}
	
	private void ipCheck(L1PcInstance gm, String name) {
		L1PcInstance target = L1World.getInstance().getPlayer(name.trim());
		if(target == null) {
			gm.sendPackets(new S_SystemMessage(
					".ipCheck doesn't work on offline players."));
			return;
		} else {
			gm.sendPackets(new S_SystemMessage((new StringBuilder())
					.append(target.getName() + ": ")
					.append("IP: " + target.getNetConnection().getIp())
					.toString()));
		}
	}
}

