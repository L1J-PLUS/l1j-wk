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
package l1j.server.server.command.executor;

import java.util.StringTokenizer;

import l1j.server.server.datatables.IpTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

/**
 * GM指令：禁止登入
 */
public class L1BanIp implements L1CommandExecutor {
	private L1BanIp() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1BanIp();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer stringtokenizer = new StringTokenizer(arg);
			// IPを指定
			String s1 = stringtokenizer.nextToken();

			// add/delを指定(しなくてもOK)
			String s2 = null;
			try {
				s2 = stringtokenizer.nextToken();
			}
			catch (Exception e) {}

			IpTable iptable = IpTable.getInstance();
			boolean isBanned = iptable.isBannedIp(s1);

			for (L1PcInstance tg : L1World.getInstance().getAllPlayers()) {
				if (s1.equals(tg.getNetConnection().getIp())) {
					String msg = new StringBuilder().append("IP:").append(s1).append(" player name:").append(tg.getName()).toString();
					pc.sendPackets(new S_SystemMessage(msg));
				}
			}

			if ("add".equalsIgnoreCase(s2) && !isBanned) {
				iptable.banIp(s1); // BANリストへIPを加える
				String msg = new StringBuilder().append("IP:").append(s1).append(" added to banlist.").toString();
				pc.sendPackets(new S_SystemMessage(msg));
			}
			else if ("del".equalsIgnoreCase(s2) && isBanned) {
				if (iptable.liftBanIp(s1)) { // BANリストからIPを削除する
					String msg = new StringBuilder().append("IP:").append(s1).append(" has been removed from banlist.").toString();
					pc.sendPackets(new S_SystemMessage(msg));
				}
			}
			else {
				// BANの確認
				if (isBanned) {
					String msg = new StringBuilder().append("IP:").append(s1).append(" recorded into the banlist.").toString();
					pc.sendPackets(new S_SystemMessage(msg));
				}
				else {
					String msg = new StringBuilder().append("IP:").append(s1).append(" not in the banlist.").toString();
					pc.sendPackets(new S_SystemMessage(msg));
				}
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("Please enter " + cmdName + " IP [ add | del ]。"));
		}
	}
}
