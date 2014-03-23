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
package l1j.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 殷海薩的祝福系統配置器
 * 
 * @author Psnnwe
 * 
 */
public final class Config_Einhasad {

	/**
	 * 提示信息
	 */
	private static final Logger _log = Logger.getLogger(Config_Einhasad.class
			.getName());

	/**
	 * 殷海薩的祝福設定檔路徑
	 */
	public static final String EINHASAD_SETTINGS_CONFIG_FILE = "./config/einhasad.properties";

	/**
	 * 是否啟動殷海薩的祝福系統
	 */
	public static boolean EINHASAD_IS_ACTIVE;

	/**
	 * 登入多久時間取得1%(單位:分鐘)
	 */
	public static byte EIN_TIME;

	/**
	 * 登出多久時間取得1%(單位:分鐘)
	 */
	public static byte EIN_OUTTIME;

	/**
	 * 最高百分比
	 */
	public static short EIN_MAX_PERCENT;

	/**
	 * 讀取設定檔中的設定
	 */
	public static void load() {
		_log.info("Reading Config for Einhasad...");
		final Properties set = new Properties();
		try {

			final InputStream is = new FileInputStream(new File(
					EINHASAD_SETTINGS_CONFIG_FILE));
			set.load(is);
			is.close();

			EINHASAD_IS_ACTIVE = Boolean.parseBoolean(set.getProperty(
					"EinhasadIsActive", "false"));
			EIN_TIME = Byte.parseByte(set.getProperty("EinTime", "15"));
			EIN_OUTTIME = Byte.parseByte(set.getProperty("EinOutTime", "15"));
			EIN_MAX_PERCENT = Short.parseShort(set.getProperty("EinMaxPercent",
					"200"));
		} catch (final Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new Error("Can't read config: " + EINHASAD_SETTINGS_CONFIG_FILE);
		} finally {
			set.clear();
		}
	}
}
