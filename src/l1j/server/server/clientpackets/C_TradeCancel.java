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
package l1j.server.server.clientpackets;

import l1j.server.server.ClientThread;
import l1j.server.server.model.L1Trade;
import l1j.server.server.model.Instance.L1PcInstance;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來交易取消的封包
 */
public class C_TradeCancel extends ClientBasePacket {

	private static final String C_TRADE_CANCEL = "[C] C_TradeCancel";

	public C_TradeCancel(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);

		L1PcInstance player = clientthread.getActiveChar();
		if (player == null) {
			return;
		}
		L1Trade trade = new L1Trade();
		trade.TradeCancel(player);
	}

	@Override
	public String getType() {
		return C_TRADE_CANCEL;
	}

}
