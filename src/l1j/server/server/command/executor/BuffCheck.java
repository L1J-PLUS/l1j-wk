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
import l1j.server.server.model.skill.L1SkillTimer;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.model.skill.L1NamedSkill;

import java.util.Map;

public class BuffCheck implements L1CommandExecutor {

	private BuffCheck() {}

	public static L1CommandExecutor getInstance() {
		return new BuffCheck();
	}

	@Override
	public void execute(L1PcInstance user, String commandName, String target) {
		try {
			buffCheck(user, target);
		} catch (Exception e) {
			e.printStackTrace();
			user.sendPackets(new S_SystemMessage(".buffCheck <name>"));
		}
	}
	
	private void buffCheck(L1PcInstance gm, String name) {
		L1PcInstance target = L1World.getInstance().getPlayer(name.trim());
		if(target == null) {
			gm.sendPackets(new S_SystemMessage(
					".buffCheck doesn't work on offline players yet."));
			return;
		} else {
			Map<Integer, L1SkillTimer> buffs = target.getBuffs();
			StringBuilder buffList = new StringBuilder();
			for (Map.Entry<Integer, L1SkillTimer> buff : buffs.entrySet()) {
				String time = buff.getValue() != null 
					? ((Integer) buff.getValue().getRemainingTime()).toString()
					: "forever";
				buffList.append("| " + L1NamedSkill.getName(buff.getKey()) + " " + time);	
			}
//			gm.sendPackets(new S_SystemMessage(
//					name + " currently has: " + buffList.toString()));
			String bufflist = buffList.toString();

			gm.sendPackets(new S_SystemMessage(
					name + " currently has: " + bufflist.substring(0, bufflist.length()/2)));
			gm.sendPackets(new S_SystemMessage(bufflist.substring(bufflist.length()/2)));
		}
	}
}

