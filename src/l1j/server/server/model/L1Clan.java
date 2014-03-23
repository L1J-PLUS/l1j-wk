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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.utils.collections.Lists;

public class L1Clan {

	public static final int CLAN_RANK_PROBATION = 1;

	public static final int CLAN_RANK_PUBLIC = 2;

	public static final int CLAN_RANK_GUARDIAN = 3;

	public static final int CLAN_RANK_PRINCE = 4;

	private static final Logger _log = Logger.getLogger(L1Clan.class.getName());

	private int _clanId;

	private String _clanName;

	private int _leaderId;

	private String _leaderName;

	private int _castleId;

	private int _houseId;

	private int _warehouse = 0;

	private final L1DwarfForClanInventory _dwarfForClan = new L1DwarfForClanInventory(this);

	private final List<String> membersNameList = Lists.newList();

	public int getClanId() {
		return _clanId;
	}

	public void setClanId(int clan_id) {
		_clanId = clan_id;
	}

	public String getClanName() {
		return _clanName;
	}

	public void setClanName(String clan_name) {
		_clanName = clan_name;
	}

	public int getLeaderId() {
		return _leaderId;
	}

	public void setLeaderId(int leader_id) {
		_leaderId = leader_id;
	}

	public String getLeaderName() {
		return _leaderName;
	}

	public void setLeaderName(String leader_name) {
		_leaderName = leader_name;
	}

	public int getCastleId() {
		return _castleId;
	}

	public void setCastleId(int hasCastle) {
		_castleId = hasCastle;
	}

	public int getHouseId() {
		return _houseId;
	}

	public void setHouseId(int hasHideout) {
		_houseId = hasHideout;
	}

	public void addMemberName(String member_name) {
		if (!membersNameList.contains(member_name)) {
			membersNameList.add(member_name);
		}
	}

	public void delMemberName(String member_name) {
		if (membersNameList.contains(member_name)) {
			membersNameList.remove(member_name);
		}
	}

	public L1PcInstance[] getOnlineClanMember() // オンライン中のクラン員のみ
	{
		List<L1PcInstance> onlineMembers = Lists.newList();
		for (String name : membersNameList) {
			L1PcInstance pc = L1World.getInstance().getPlayer(name);
			if ((pc != null) && !onlineMembers.contains(pc)) {
				onlineMembers.add(pc);
			}
		}
		return onlineMembers.toArray(new L1PcInstance[onlineMembers.size()]);
	}

	public String getOnlineMembersFP() { // FP means "For Pledge"
		String result = "";
		for (String name : membersNameList) {
			L1PcInstance pc = L1World.getInstance().getPlayer(name);
			if (pc != null) {
				result = result + name + " ";
			}
		}
		return result;
	}

	public String getAllMembersFP() {
		String result = "";
		for (String name : membersNameList) {
			result = result + name + " ";
		}
		return result;
	}

	public String getOnlineMembersFPWithRank() {
		String result = "";
		for (String name : membersNameList) {
			L1PcInstance pc = L1World.getInstance().getPlayer(name);
			if (pc != null) {
				result = result + name + getRankString(pc) + " ";
			}
		}
		return result;
	}

	public String getAllMembersFPWithRank() {
		String result = "";
		try {
			for (String name : membersNameList) {
				L1PcInstance pc = CharacterTable.getInstance().restoreCharacter(name);
				if (pc != null) {
					result = result + name + getRankString(pc) + " ";
				}
			}
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return result;
	}

	private String getRankString(L1PcInstance pc) {
		String rank = "";
		String[] msg = {
				"[Probation]", "[Public]", "[Guardian]", "[Royal]",
				"[Probation]", "[Public]", "[Guardian]", "[Royal]",
				"[Probation]", "[Public]", "[Guardian]", "[Royal]"
		};
		byte i = 0; // 預設：繁體
		if (Config.CLIENT_LANGUAGE == 5) { // 簡體
			i = 4;
		} else if (Config.CLIENT_LANGUAGE == 4) { // 日文
			i = 8;
		}
		if (pc != null) {
			if (pc.getClanRank() == CLAN_RANK_PROBATION) {
				rank = msg[0 + i];
			} else if (pc.getClanRank() == CLAN_RANK_PUBLIC) {
				rank = msg[1 + i];
			} else if (pc.getClanRank() == CLAN_RANK_GUARDIAN) {
				rank = msg[2 + i];
			} else if (pc.getClanRank() == CLAN_RANK_PRINCE) {
				rank = msg[3 + i];
			} else {
				rank = "";
			}
			
		}
		return rank;
	}

	public String[] getAllMembers() {
		return membersNameList.toArray(new String[membersNameList.size()]);
	}

	public L1DwarfForClanInventory getDwarfForClanInventory() {
		return _dwarfForClan;
	}

	public int getWarehouseUsingChar() {
		return _warehouse;
	}

	public void setWarehouseUsingChar(int objid) {
		_warehouse = objid;
	}
}
