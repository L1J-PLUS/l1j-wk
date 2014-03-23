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
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1BookMark;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

/**
 * è™•ç�†æ”¶åˆ°å®¢æˆ¶ç«¯å‚³ä¾†æ–°å¢žæ›¸ç±¤çš„å°�åŒ… 
 */
public class C_AddBookmark extends ClientBasePacket {

	private static final String C_ADD_BOOKMARK = "[C] C_AddBookmark";

	public C_AddBookmark(byte[] decrypt, ClientThread client) {
		super(decrypt);
		
		L1PcInstance pc = client.getActiveChar();
		if ((pc == null) || pc.isGhost()) {
			return;
		}

		String s = readS();

		if (pc.getMap().isMarkable() || pc.isGm()) {
			if ((L1CastleLocation.checkInAllWarArea(pc.getX(), pc.getY(),
					pc.getMapId()) || L1HouseLocation.isInHouse(pc.getX(),
					pc.getY(), pc.getMapId()))
					&& !pc.isGm()) {
				// \f1ã�“ã�“ã‚’è¨˜æ†¶ã�™ã‚‹ã�“ã�¨ã�Œã�§ã��ã�¾ã�›ã‚“ã€‚
				pc.sendPackets(new S_ServerMessage(214));
			} else {
				L1BookMark.addBookmark(pc, s);
			}
		} else {
			// \f1ã�“ã�“ã‚’è¨˜æ†¶ã�™ã‚‹ã�“ã�¨ã�Œã�§ã��ã�¾ã�›ã‚“ã€‚
			pc.sendPackets(new S_ServerMessage(214));
		}
	}

	@Override
	public String getType() {
		return C_ADD_BOOKMARK;
	}
}
