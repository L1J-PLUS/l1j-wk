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
package l1j.server.server.templates;

public class L1EtcItem extends L1Item {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public L1EtcItem() {
	}

	private boolean _stackable;

	private int _locx;

	private int _locy;

	private short _mapid;

	private int _delay_id;

	private int _delay_time;

	private int _delay_effect;

	private int _maxChargeCount;

	private boolean _isCanSeal; // ● 封印スクロールで封印可能

	@Override
	public boolean isStackable() {
		return _stackable;
	}

	public void set_stackable(boolean stackable) {
		_stackable = stackable;
	}

	public void set_locx(int locx) {
		_locx = locx;
	}

	@Override
	public int get_locx() {
		return _locx;
	}

	public void set_locy(int locy) {
		_locy = locy;
	}

	@Override
	public int get_locy() {
		return _locy;
	}

	public void set_mapid(short mapid) {
		_mapid = mapid;
	}

	@Override
	public short get_mapid() {
		return _mapid;
	}

	public void set_delayid(int delay_id) {
		_delay_id = delay_id;
	}

	@Override
	public int get_delayid() {
		return _delay_id;
	}

	public void set_delaytime(int delay_time) {
		_delay_time = delay_time;
	}

	@Override
	public int get_delaytime() {
		return _delay_time;
	}

	public void set_delayEffect(int delay_effect) {
		_delay_effect = delay_effect;
	}

	public int get_delayEffect() {
		return _delay_effect;
	}

	public void setMaxChargeCount(int i) {
		_maxChargeCount = i;
	}

	@Override
	public int getMaxChargeCount() {
		return _maxChargeCount;
	}

	@Override
	public boolean isCanSeal() {
		return _isCanSeal;
	}

	public void setCanSeal(boolean flag) {
		_isCanSeal = flag;
	}


}
