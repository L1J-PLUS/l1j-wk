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
package l1j.server.server.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import l1j.server.server.model.TimeInform;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;

/**
 * Log Recorder 紀錄存取
 * 
 * @author ibm
 */
public class LogRecorder {
	/* BufferedWriter */
	static BufferedWriter out;

	/** base */
	public static void writeLog(String messenge) {
		try {
			out = new BufferedWriter(new FileWriter("log\\Log.log", true));
			out.write(messenge + "\r\n");
			out.close();
		} catch (IOException e) {
			System.out.println("Error message: " + e.getMessage());
		}
	}

	/**
	 * Weapon Log
	 * 
	 * @param pc
	 *            玩家
	 */
	public static void writeWeaponLog(String messenge, L1PcInstance pc) {
		try {
			File WeaponLog = new File("log\\WeaponLog.log");
			if (WeaponLog.createNewFile()) {
				out = new BufferedWriter(new FileWriter("log\\WeaponLog.log",
						false));
				out.write("Log of enchanting weapon:" + "\r\n");
				out.close();
			}
			out = new BufferedWriter(new FileWriter("log\\WeaponLog.log", true));// true:從尾端寫起
			out.write("\r\n");// 每次填寫資料都控一行 // |false:從頭寫
			out.write("From account: " + pc.getAccountName() 
					+ ", from player: "+ pc.getName() + ", " + messenge + "." + "\r\n");
			out.close();
		} catch (IOException e) {
			System.out.println("Error message: " + e.getMessage());
		}
	}

	/**
	 * Bug Log
	 * 
	 * @param pc
	 *            玩家
	 */
	public static void writeBugLog(String messenge, L1PcInstance pc) {
		try {
			File BugLog = new File("log\\BugLog.log");
			if (BugLog.createNewFile()) {
				out = new BufferedWriter(new FileWriter("log\\BugLog.txt",
						false));
				out.write("Bug list" + "\r\n");
				out.write(messenge + "\r\n");
				out.close();
			}
			out = new BufferedWriter(new FileWriter("log\\BugLog.txt", true));
			out.write("\r\n");// 每次填寫資料都控一行
			out.write("From player: " + pc.getName() + ": " + messenge + "\r\n");
			out.close();

		} catch (IOException e) {
			System.out.println("Error message: " + e.getMessage());
		}
	}

	/**
	 * Armor Log
	 * 
	 * @param pc
	 *            玩家
	 */
	public static void writeArmorLog(String messenge, L1PcInstance pc) {
		try {
			File ArmorLog = new File("log\\ArmorLog.log");
			if (ArmorLog.createNewFile()) {
				out = new BufferedWriter(new FileWriter("log\\ArmorLog.log",
						false));
				out.write("Enchant armor log" + "\r\n");
				out.close();
			}
			out = new BufferedWriter(new FileWriter("log\\ArmorLog.log", true));
			out.write("\r\n");// 每次填寫資料都控一行
			out.write("From account: " + pc.getAccountName() + ", from player: "
					+ pc.getName() + ", " + messenge + "." + "\r\n");
			out.close();
		} catch (IOException e) {
			System.out.println("Error message: " + e.getMessage());
		}
	}

	/**
	 * Trade Log
	 * 
	 * @param sender
	 *            主動交易者
	 * @param receiver
	 *            被交易者
	 * @param l1iteminstance1
	 *            物品
	 * 
	 **/
	public static void writeTradeLog(L1PcInstance sender,
			L1PcInstance receiver, L1ItemInstance l1iteminstance1) {
		try {
			File TradeLog = new File("log\\TradeLog.log");
			if (TradeLog.createNewFile()) {
				out = new BufferedWriter(new FileWriter("log\\TradeLog.log",
						false));
				out.write("Trade log" + "\r\n");
				out.close();
			}
			out = new BufferedWriter(new FileWriter("log\\TradeLog.log", true));
			out.write("\r\n");// 每次填寫資料都控一行
			out.write("From account: " + sender.getAccountName() 
					+ ", from player: "+ sender.getName() 
					+ ",traded  +"+ l1iteminstance1.getEnchantLevel()+ l1iteminstance1.getName() 
					+ "("+ l1iteminstance1.getCount() + ")," 
					+ "to " + " account:"+ receiver.getAccountName() 
					+ " player:" + receiver.getName()+ "." + "\r\n");
			out.close();
		} catch (IOException e) {
			System.out.println("Error message: " + e.getMessage());
		}
	}

	/**
	 * Robots Log
	 * 
	 * @param player
	 *            使用外掛或加速器的玩家
	 */
	public static void writeRobotsLog(L1PcInstance player) {
		try {
			File RobotsLog = new File("log\\RobotsLog.log");
			if (RobotsLog.createNewFile()) {
				out = new BufferedWriter(new FileWriter("log\\RobotsLog.log",
						false));
				out.write("Bot and macro log" + "\r\n");
				out.close();
			}
			out = new BufferedWriter(new FileWriter("log\\RobotsLog.log", true));
			out.write("\r\n");// 每次填寫資料都控一行
			out.write("Speedhacking from account: " + player.getAccountName() 
					+ ", from player: "+ player.getName() 
					+ ", time:" + TimeInform.getNowTime(3, 0)
					+ "\r\n");
			out.close();
		} catch (IOException e) {
			System.out.println("Error message: " + e.getMessage());
		}
	}

	/**
	 * 丟棄物品紀錄
	 * @param player
	 */
	public static void writeDropLog(L1PcInstance player, L1ItemInstance item) {
		try {
			File DropLog = new File("log\\DropLog.log");
			if (DropLog.createNewFile()) {
				out = new BufferedWriter(new FileWriter("log\\DropLog.log",
						false));
				out.write("Drop item log:" + "\r\n");
				out.close();
			}
			out = new BufferedWriter(new FileWriter("log\\DropLog.log", true));
			out.write("\r\n");// 每次填寫資料都控一行
			out.write("From account: " + player.getAccountName() 
					+ "ip: " + player.getNetConnection().getIp()
					+ ",from player: "+ player.getName() 
					+ ",location: " + item.getLocation()
					+ ",time:" + TimeInform.getNowTime(3, 0)
					+ ",dropped: " + item.getCount() + " of " + item.getName()
					+ ",blessed: " + item.getBless() 
					+ ",enchant level: "+ item.getEnchantLevel() 
					+ ",element enchant type: "+item.getAttrEnchantKind()
					+ ",element enchant level: "+item.getAttrEnchantLevel()
					+ "\r\n");
			out.close();
		} catch (IOException e) {
			System.out.println("Error message: " + e.getMessage());
		}
	}
}
