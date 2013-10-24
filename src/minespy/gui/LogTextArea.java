package minespy.gui;

import java.awt.Graphics;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTextArea;
import javax.swing.text.Document;

public class LogTextArea extends JTextArea {

	private static final long serialVersionUID = 1L;

	private final BlockingQueue<String> logqueue = new LinkedBlockingQueue<String>();
	private final PrintWriter pw = new PrintWriter(new LogWriter(), true);

	public LogTextArea() {

	}

	public LogTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
	}

	public LogTextArea(Document doc) {
		super(doc);
	}

	public LogTextArea(int rows, int columns) {
		super(rows, columns);
	}

	public LogTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
	}

	public LogTextArea(String text) {
		super(text);
	}

	@Override
	public void paint(Graphics g) {
		for (int i = 0; i < 100 && !logqueue.isEmpty(); i++) {
			append(logqueue.poll());
		}
		super.paint(g);
	}

	public PrintWriter getWriter() {
		return pw;
	}

	private class LogWriter extends Writer {

		private StringBuilder sb = new StringBuilder();

		@Override
		public void close() throws IOException {
			// do nothing
		}

		@Override
		public synchronized void flush() throws IOException {
			logqueue.offer(sb.toString());
			sb = new StringBuilder();
			LogTextArea.this.repaint();
		}

		@Override
		public synchronized void write(char[] str, int offset, int len) throws IOException {
			sb.append(str, offset, len);
			// maybe flush if str contains a \n ?
		}

	}

}
