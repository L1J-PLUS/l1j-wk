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
package l1j.server.server.model.Instance;

import static l1j.server.server.model.skill.L1SkillId.GMSTATUS_SHOWTRAPS;

import java.util.List;

import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.trap.L1Trap;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_Trap;
import l1j.server.server.types.Point;
import l1j.server.server.utils.Random;
import l1j.server.server.utils.collections.Lists;

public class L1TrapInstance extends L1Object {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final L1Trap _trap;

	private final Point _baseLoc = new Point();

	private final Point _rndPt = new Point();

	private final int _span;

	private boolean _isEnable = true;

	private final String _nameForView;

	private List<L1PcInstance> _knownPlayers = Lists.newConcurrentList();

	public L1TrapInstance(int id, L1Trap trap, L1Location loc, Point rndPt, int span) {
		setId(id);
		_trap = trap;
		getLocation().set(loc);
		_baseLoc.set(loc);
		_rndPt.set(rndPt);
		_span = span;
		_nameForView = "trap";

		resetLocation();
	}

	public L1TrapInstance(int id, L1Location loc) {
		setId(id);
		_trap = L1Trap.newNull();
		getLocation().set(loc);
		_span = 0;
		_nameForView = "trap base";
	}

	public void resetLocation() {
		if ((_rndPt.getX() == 0) && (_rndPt.getY() == 0)) {
			return;
		}

		for (int i = 0; i < 50; i++) {
			int rndX = Random.nextInt(_rndPt.getX() + 1) * (Random.nextInt(2) == 1 ? 1 : -1); // 1/2の確率でマイナスにする
			int rndY = Random.nextInt(_rndPt.getY() + 1) * (Random.nextInt(2) == 1 ? 1 : -1);

			rndX += _baseLoc.getX();
			rndY += _baseLoc.getY();

			L1Map map = getLocation().getMap();
			if (map.isInMap(rndX, rndY) && map.isPassable(rndX, rndY)) {
				getLocation().set(rndX, rndY);
				break;
			}
		}
		// ループ内で位置が確定しない場合、前回と同じ位置になる。
	}

	public void enableTrap() {
		_isEnable = true;
	}

	public void disableTrap() {
		_isEnable = false;

		for (L1PcInstance pc : _knownPlayers) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		_knownPlayers.clear();
	}

	public boolean isEnable() {
		return _isEnable;
	}

	public int getSpan() {
		return _span;
	}

	public void onTrod(L1PcInstance trodFrom) {
		_trap.onTrod(trodFrom, this);
	}

	public void onDetection(L1PcInstance caster) {
		_trap.onDetection(caster, this);
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		if (perceivedFrom.hasSkillEffect(GMSTATUS_SHOWTRAPS)) {
			perceivedFrom.addKnownObject(this);
			perceivedFrom.sendPackets(new S_Trap(this, _nameForView));
			_knownPlayers.add(perceivedFrom);
		}
	}
}
