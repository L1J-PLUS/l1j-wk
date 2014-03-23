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

import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.types.Point;
import l1j.server.server.utils.Random;

public class L1Location extends Point {
	protected L1Map _map = L1Map.newNull();

	public L1Location() {
		super();
	}

	public L1Location(L1Location loc) {
		this(loc._x, loc._y, loc._map);
	}

	public L1Location(int x, int y, int mapId) {
		super(x, y);
		setMap(mapId);
	}

	public L1Location(int x, int y, L1Map map) {
		super(x, y);
		_map = map;
	}

	public L1Location(Point pt, int mapId) {
		super(pt);
		setMap(mapId);
	}

	public L1Location(Point pt, L1Map map) {
		super(pt);
		_map = map;
	}

	public void set(L1Location loc) {
		_map = loc._map;
		_x = loc._x;
		_y = loc._y;
	}

	public void set(int x, int y, int mapId) {
		set(x, y);
		setMap(mapId);
	}

	public void set(int x, int y, L1Map map) {
		set(x, y);
		_map = map;
	}

	public void set(Point pt, int mapId) {
		set(pt);
		setMap(mapId);
	}

	public void set(Point pt, L1Map map) {
		set(pt);
		_map = map;
	}

	public L1Map getMap() {
		return _map;
	}

	public int getMapId() {
		return _map.getId();
	}

	public void setMap(L1Map map) {
		_map = map;
	}

	public void setMap(int mapId) {
		_map = L1WorldMap.getInstance().getMap((short) mapId);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof L1Location)) {
			return false;
		}
		L1Location loc = (L1Location) obj;
		return (getMap() == loc.getMap()) && (getX() == loc.getX()) && (getY() == loc.getY());
	}

	@Override
	public int hashCode() {
		return 7 * _map.getId() + super.hashCode();
	}

	@Override
	public String toString() {
		return String.format("(%d, %d) on %d", _x, _y, _map.getId());
	}

	/**
	 * このLocationに対する、移動可能なランダム範囲のLocationを返す。
	 * ランダムテレポートの場合は、城エリア、アジト内のLocationは返却されない。
	 * 
	 * @param max
	 *            ランダム範囲の最大値
	 * @param isRandomTeleport
	 *            ランダムテレポートか
	 * @return 新しいLocation
	 */
	public L1Location randomLocation(int max, boolean isRandomTeleport) {
		return randomLocation(0, max, isRandomTeleport);
	}

	/**
	 * このLocationに対する、移動可能なランダム範囲のLocationを返す。
	 * ランダムテレポートの場合は、城エリア、アジト内のLocationは返却されない。
	 * 
	 * @param min
	 *            ランダム範囲の最小値(0で自身の座標を含む)
	 * @param max
	 *            ランダム範囲の最大値
	 * @param isRandomTeleport
	 *            ランダムテレポートか
	 * @return 新しいLocation
	 */
	public L1Location randomLocation(int min, int max, boolean isRandomTeleport) {
		return L1Location.randomLocation(this, min, max, isRandomTeleport);
	}

	/**
	 * 引数のLocationに対して、移動可能なランダム範囲のLocationを返す。
	 * ランダムテレポートの場合は、城エリア、アジト内のLocationは返却されない。
	 * 
	 * @param baseLocation
	 *            ランダム範囲の元になるLocation
	 * @param min
	 *            ランダム範囲の最小値(0で自身の座標を含む)
	 * @param max
	 *            ランダム範囲の最大値
	 * @param isRandomTeleport
	 *            ランダムテレポートか
	 * @return 新しいLocation
	 */
	public static L1Location randomLocation(L1Location baseLocation, int min, int max, boolean isRandomTeleport) {
		if (min > max) {
			throw new IllegalArgumentException("min > maxとなる引数は無効");
		}
		if (max <= 0) {
			return new L1Location(baseLocation);
		}
		if (min < 0) {
			min = 0;
		}

		L1Location newLocation = new L1Location();
		int newX = 0;
		int newY = 0;
		int locX = baseLocation.getX();
		int locY = baseLocation.getY();
		short mapId = (short) baseLocation.getMapId();
		L1Map map = baseLocation.getMap();

		newLocation.setMap(map);

		int locX1 = locX - max;
		int locX2 = locX + max;
		int locY1 = locY - max;
		int locY2 = locY + max;

		// map範囲
		int mapX1 = map.getX();
		int mapX2 = mapX1 + map.getWidth();
		int mapY1 = map.getY();
		int mapY2 = mapY1 + map.getHeight();

		// 最大でもマップの範囲内までに補正
		if (locX1 < mapX1) {
			locX1 = mapX1;
		}
		if (locX2 > mapX2) {
			locX2 = mapX2;
		}
		if (locY1 < mapY1) {
			locY1 = mapY1;
		}
		if (locY2 > mapY2) {
			locY2 = mapY2;
		}

		int diffX = locX2 - locX1; // x方向
		int diffY = locY2 - locY1; // y方向

		int trial = 0;
		// 試行回数を範囲最小値によってあげる為の計算
		int amax = (int) Math.pow(1 + (max * 2), 2);
		int amin = (min == 0) ? 0 : (int) Math.pow(1 + ((min - 1) * 2), 2);
		int trialLimit = 40 * amax / (amax - amin);

		while (true) {
			if (trial >= trialLimit) {
				newLocation.set(locX, locY);
				break;
			}
			trial++;

			newX = locX1 + Random.nextInt(diffX + 1);
			newY = locY1 + Random.nextInt(diffY + 1);

			newLocation.set(newX, newY);

			if (baseLocation.getTileLineDistance(newLocation) < min) {
				continue;

			}
			if (isRandomTeleport) { // ランダムテレポートの場合
				if (L1CastleLocation.checkInAllWarArea(newX, newY, mapId)) { // いずれかの城エリア
					continue;
				}

				// いずれかのアジト内
				if (L1HouseLocation.isInHouse(newX, newY, mapId)) {
					continue;
				}
			}

			if (map.isInMap(newX, newY) && map.isPassable(newX, newY)) {
				break;
			}
		}
		return newLocation;
	}
}
