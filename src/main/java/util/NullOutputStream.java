package util;

import java.io.OutputStream;

public final class NullOutputStream extends OutputStream {
	public static final OutputStream INSTANCE = new NullOutputStream();

	public static OutputStream getInstance() {
		return INSTANCE;
	}

	private NullOutputStream() {
	}

	/** Discards the specified byte. */
	@Override
	public void write(int b) {
	}

	/** Discards the specified byte array. */
	@Override
	public void write(byte[] b) {
	}

	/** Discards the specified byte array. */
	@Override
	public void write(byte[] b, int off, int len) {
	}
}
