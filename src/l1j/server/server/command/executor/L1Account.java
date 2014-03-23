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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.SQLUtil;

public class L1Account implements L1CommandExecutor {
	private L1Account() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Account();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
               Connection con = null;
                PreparedStatement pstm = null;
                PreparedStatement pstm2 = null;
                PreparedStatement pstm3 = null;
                PreparedStatement pstm4 = null;

                ResultSet rs = null;
                ResultSet rs2 = null;
                ResultSet rs3 = null;
                ResultSet rs4 = null;

                try {
                		int storedadena = 0;
                		int totalheldadena = 0;
                        con = L1DatabaseFactory.getInstance().getConnection();
            			//String accountName=String.valueOf(arg).toString().trim();

                        pstm = con.prepareStatement("SELECT char_name,level,MaxHp,MaxMp,Class,objid FROM characters WHERE account_name=?");
                        pstm.setString(1, arg.trim());
                        rs = pstm.executeQuery();
                        
                        pstm2 = con.prepareStatement("SELECT count FROM character_warehouse WHERE account_name=? AND item_id = 40308");
                        pstm2.setString(1, arg.trim());
                        rs2 = pstm2.executeQuery();
                        
                        pstm3 = con.prepareStatement("SELECT count FROM character_elf_warehouse WHERE account_name=? AND item_id = 40308");
                        pstm3.setString(1, arg.trim());
                        rs3 = pstm3.executeQuery();
                        
                        while(rs2.next()) {
                        	storedadena = storedadena + rs2.getInt("count");
                        }
                        while(rs3.next()) {
                        	storedadena = storedadena + rs3.getInt("count");
                        }
                        
                        pc.sendPackets(new S_SystemMessage("Account ("+arg+"):"));
                        while(rs.next()) {
                        	int heldadena = 0;
                            pstm4 = con.prepareStatement("SELECT count FROM character_items WHERE char_id=? AND item_id = 40308");
                            pstm4.setInt(1, rs.getInt("objid"));
                            rs4 = pstm4.executeQuery();
                            while(rs4.next()) {
                            	heldadena = heldadena + rs4.getInt("count");
                            	totalheldadena = totalheldadena + rs4.getInt("count");
                            }
                            pstm4.close();
                            rs4.close();
                                pc.sendPackets(new S_SystemMessage((new StringBuilder())
                                                .append(rs.getString("char_name"))
                                                .append(": L").append(rs.getInt("level"))
                                                .append(" ").append(getSex(rs.getInt("Class")))
                                                .append(" ").append(getClass(rs.getInt("Class")))
                                                .append(" ").append(rs.getInt("MaxHp"))
                                                .append("/").append(rs.getInt("MaxMp") + " ")
                                                .append(" Gold: " + heldadena)
                                                .toString()));
                        }
                        pc.sendPackets(new S_SystemMessage("Stored Gold: " + storedadena));
                        pc.sendPackets(new S_SystemMessage("Total Held Gold: " + totalheldadena));
                        pc.sendPackets(new S_SystemMessage("Total Gold: " + (totalheldadena + storedadena) ));

                        rs.close();
                        rs2.close();
                        rs3.close();
                } catch (Exception e) {
                        pc.sendPackets(new S_SystemMessage(".account account_name"));
                        System.out.println(e);
                } finally {
                    SQLUtil.close(rs);
                    SQLUtil.close(pstm);
                    SQLUtil.close(con);
                    SQLUtil.close(rs2);
                    SQLUtil.close(pstm2);
                    SQLUtil.close(rs3);
                    SQLUtil.close(pstm3);
                }
        }
	private String getSex(int classID){
		if(classID == 0 || classID == 61 || classID == 138 || classID == 734 || classID == 2786){
			return "Male";
		} else if(classID == 1 || classID == 48 || classID == 37 || classID == 1186 || classID == 2796){
			return "Female";
		} else{
			return "error";
		}
	}
	private String getClass(int classID){
		if(classID == 0 || classID == 1){
			return "Royal";
		} else if(classID == 61 || classID == 48){
			return "Knight";
		} else if(classID == 138 || classID == 37){
			return "Elf";
		} else if(classID == 734 || classID == 1186){
			return "Mage";
		} else if(classID == 2786 || classID == 2796){
			return "Dark Elf";
		} else if (classID == 6658 || classID == 6661){
			return "Dragon Knight";
		} else if (classID == 6671 || classID == 6650){
			return "Illusionist";
		} else{
			return "error";
		}

	}

}
