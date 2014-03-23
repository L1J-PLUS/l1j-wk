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

import java.io.IOException;
import java.io.InputStream;

public class BinaryInputStream extends InputStream {
	InputStream _in;

	public BinaryInputStream(InputStream in) {
		_in = in;
	}

	@Override
	public long skip(long n) throws IOException {
		return _in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return _in.available();
	}

	@Override
	public void close() throws IOException {
		_in.close();
	}

	@Override
	public int read() throws IOException {
		return _in.read();
	}

	public int readByte() throws IOException {
		return _in.read();
	}

	public int readShort() throws IOException {
		return _in.read() | ((_in.read() << 8) & 0xFF00);
	}

	public int readInt() throws IOException {
		return readShort() | ((readShort() << 16) & 0xFFFF0000);
	}
}
