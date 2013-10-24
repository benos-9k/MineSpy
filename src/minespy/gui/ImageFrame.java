package minespy.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private BufferedImage m_img;

	public ImageFrame(BufferedImage img_) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		m_img = img_;
		Panel p = new Panel();
		p.setPreferredSize(new Dimension(512, 512));
		add(p);
		pack();
	}

	public void setImage(BufferedImage img) {
		m_img = img;
	}

	private class Panel extends JPanel {

		private static final long serialVersionUID = 1L;

		public Panel() {

		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (m_img != null) {
				g.drawImage(m_img, 0, 0, getWidth(), getHeight(), null);
			}
		}

	}

	public static void main(String[] args) {
		ImageFrame f = new ImageFrame(null);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

}
