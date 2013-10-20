package cli;

import util.NullOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simulates writing lines to an {@link OutputStream}.
 * <p/>
 * Internally, the lines written to the underlying {@link OutputStream} are buffered and can be retrieved on demand for
 * verification purposes.
 */
public class TestOutputStream extends OutputStream {
	private final Queue<String> lines = new LinkedBlockingQueue<String>();
	private volatile StringBuilder line = new StringBuilder();
	private OutputStream delegate;

	/**
	 * Creates a new {@code TestOutputStream} instance writing to an {@link NullOutputStream}.
	 */
	public TestOutputStream() {
		this(NullOutputStream.getInstance());
	}

	/**
	 * Creates a new {@code TestOutputStream} instance writing to the provided {@link OutputStream}.
	 *
	 * @param delegate the stream to write to
	 */
	public TestOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
		if (b == '\r') {
			// Do nothing
		} else if (b == '\n') {
			addLine();
		} else {
			line.append((char) b);
		}
	}

	/**
	 * Returns a copy of the lines written to the {@link OutputStream} so far.
	 *
	 * @return the written lines
	 */
	public List<String> getLines() {
		synchronized (lines) {
			if (line.length() > 0) {
				addLine();
			}
			return new ArrayList<String>(lines);
		}
	}

	/**
	 * Returns a copy of the lines written to the {@link OutputStream} so far and clears the buffer.
	 *
	 * @return the written lines
	 * @see #getLines()
	 * @see #clear()
	 */
	public List<String> reset() {
		synchronized (lines) {
			List<String> lines = getLines();
			clear();
			return lines;
		}
	}

	/**
	 * Clears the buffer holding the lines written to the {@link OutputStream} so far.
	 */
	private void clear() {
		synchronized (lines) {
			lines.clear();
			line = new StringBuilder();
		}
	}

	/**
	 * Appends the current line to the buffer.
	 */
	private void addLine() {
		synchronized (lines) {
			lines.add(line.toString());
			line = new StringBuilder();
		}
	}
}
