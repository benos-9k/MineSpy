package minespy.gui;

import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import minespy.*;
import minespy.Renderer;
import minespy.mapshaders.*;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	static {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
	}

	private JPanel root;
	private JComboBox combo_saves;
	private JTextField text_custom;
	private JButton button_custom;
	private JRadioButton radio_overworld;
	private JRadioButton radio_nether;
	private JRadioButton radio_end;
	private JComboBox combo_shader;
	private JComboBox combo_skylight;
	private JCheckBox check_forcebrightness;
	private JCheckBox check_cavemode;
	private JCheckBox check_showmarkers;
	private JCheckBox check_filterblocks;
	private JRadioButton radio_filterblocks_include;
	private JRadioButton radio_filterblocks_exclude;
	private JTextField text_filterblocks;
	private JButton button_filterblocks;
	private JCheckBox check_filterlevels;
	private JRadioButton radio_filterlevels_include;
	private JRadioButton radio_filterlevels_exclude;
	private JTextField text_filterlevels;
	private JButton button_filterlevels;
	private JLabel label_world;
	private JTextField text_output;
	private JCheckBox check_autoname;
	private JButton button_render;
	private JComboBox combo_throttle;
	private JProgressBar progbar_render;
	private LogTextArea textarea_log;

	private Action action_render = new RenderAction();

	public MainFrame() {

		setTitle("MineSpy alpha");

		root = new JPanel();
		add(root);

		textarea_log = new LogTextArea(10, 0);
		textarea_log.setEditable(false);
		MineSpy.addLogOutput(textarea_log.getWriter());

		root.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		root.setLayout(new BoxLayout(root, BoxLayout.PAGE_AXIS));

		JLabel label_world = new JLabel("World:");
		label_world.setAlignmentX(0f);
		root.add(label_world);

		combo_saves = new JComboBox();
		for (File f : Config.getConfig().getMinecraftSaveDirs()) {
			combo_saves.addItem(f);
		}
		combo_saves.addItem("Custom...");
		combo_saves.setAlignmentX(0f);

		root.add(combo_saves);

		JLabel label_custom = new JLabel("Custom world path:");
		label_custom.setAlignmentX(0f);
		root.add(label_custom);

		Box box_custom = Box.createHorizontalBox();
		box_custom.setAlignmentX(0f);
		text_custom = new JTextField(30);
		box_custom.add(text_custom);
		button_custom = new JButton();
		button_custom.setAction(new WorldChooseAction());
		box_custom.add(button_custom);
		root.add(box_custom);

		ButtonGroup bg_dim = new ButtonGroup();

		radio_overworld = new JRadioButton(Dimension.OVERWORLD.getDisplayName());
		radio_nether = new JRadioButton(Dimension.NETHER.getDisplayName());
		radio_end = new JRadioButton(Dimension.END.getDisplayName());

		bg_dim.add(radio_overworld);
		bg_dim.add(radio_nether);
		bg_dim.add(radio_end);

		Box box_radio_dim = Box.createHorizontalBox();
		box_radio_dim.setAlignmentX(0f);
		box_radio_dim.add(radio_overworld);
		box_radio_dim.add(radio_nether);
		box_radio_dim.add(radio_end);
		root.add(box_radio_dim);

		radio_overworld.setSelected(true);

		Box box_shader = Box.createHorizontalBox();
		box_shader.setAlignmentX(0f);
		box_shader.add(new JLabel("Shader:  "));
		// TODO use a wrapper for the shader factories, ideally have the factories add themselves to some list
		combo_shader = new JComboBox(new Object[] { new BlockShader.Factory(), new PrettyShader.Factory(),
				new HeightColorShader.Factory(), new HeightGreyscaleShader.Factory(), new BiomeShader.Factory(),
				new BlocklightShader.Factory() });
		box_shader.add(combo_shader);
		root.add(box_shader);

		root.add(Box.createVerticalStrut(3));

		Box box_skylight = Box.createHorizontalBox();
		box_skylight.setAlignmentX(0f);
		box_skylight.add(new JLabel("Skylight level:  "));
		combo_skylight = new JComboBox(new String[] { "0", "1", "2", "3", "4 (Moonlight)", "5", "6", "7", "8", "9",
				"10", "11", "12", "13", "14", "15 (Sunlight)" });
		combo_skylight.setSelectedIndex(15);
		box_skylight.add(combo_skylight);
		root.add(box_skylight);

		check_forcebrightness = new JCheckBox("Force brightness");
		check_forcebrightness.setAlignmentX(0f);
		root.add(check_forcebrightness);

		check_cavemode = new JCheckBox("Cave mode");
		check_cavemode.setAlignmentX(0f);
		root.add(check_cavemode);

		check_showmarkers = new JCheckBox("Show markers");
		check_showmarkers.setAlignmentX(0f);
		check_showmarkers.setSelected(true);
		root.add(check_showmarkers);

		check_filterblocks = new JCheckBox("Filter blocks, eg: 1, 2, 3..23, 78");
		check_filterblocks.setAlignmentX(0f);
		root.add(check_filterblocks);

		ButtonGroup bg_filterblocks = new ButtonGroup();

		radio_filterblocks_include = new JRadioButton("Include blocks");
		radio_filterblocks_exclude = new JRadioButton("Exclude blocks");

		bg_filterblocks.add(radio_filterblocks_include);
		bg_filterblocks.add(radio_filterblocks_exclude);

		Box box_radio_filterblocks = Box.createHorizontalBox();
		box_radio_filterblocks.setAlignmentX(0f);
		box_radio_filterblocks.add(radio_filterblocks_include);
		box_radio_filterblocks.add(radio_filterblocks_exclude);
		root.add(box_radio_filterblocks);

		radio_filterblocks_exclude.setSelected(true);

		Box box_text_filterblocks = Box.createHorizontalBox();
		box_text_filterblocks.setAlignmentX(0f);
		text_filterblocks = new JTextField();
		box_text_filterblocks.add(text_filterblocks);
		button_filterblocks = new JButton("...");
		box_text_filterblocks.add(button_filterblocks);
		root.add(box_text_filterblocks);

		check_filterlevels = new JCheckBox("Filter levels, eg: 1, 2, 3..23, 78");
		check_filterlevels.setAlignmentX(0f);
		root.add(check_filterlevels);

		ButtonGroup bg_filterlevels = new ButtonGroup();

		radio_filterlevels_include = new JRadioButton("Include levels");
		radio_filterlevels_exclude = new JRadioButton("Exclude levels");

		bg_filterlevels.add(radio_filterlevels_include);
		bg_filterlevels.add(radio_filterlevels_exclude);

		Box box_radio_filterlevels = Box.createHorizontalBox();
		box_radio_filterlevels.setAlignmentX(0f);
		box_radio_filterlevels.add(radio_filterlevels_include);
		box_radio_filterlevels.add(radio_filterlevels_exclude);
		root.add(box_radio_filterlevels);

		radio_filterlevels_exclude.setSelected(true);

		Box box_text_filterlevels = Box.createHorizontalBox();
		box_text_filterlevels.setAlignmentX(0f);
		text_filterlevels = new JTextField();
		box_text_filterlevels.add(text_filterlevels);
		button_filterlevels = new JButton("...");
		box_text_filterlevels.add(button_filterlevels);
		root.add(box_text_filterlevels);

		JLabel label_output = new JLabel("Output:");
		label_output.setAlignmentX(0f);
		root.add(label_output);

		Box box_output = Box.createHorizontalBox();
		box_output.setAlignmentX(0f);
		label_world = new JLabel();
		box_output.add(label_world);
		text_output = new JTextField("output");
		box_output.add(text_output);
		check_autoname = new JCheckBox("Auto-name");
		check_autoname.setSelected(true);
		box_output.add(check_autoname);
		root.add(box_output);

		root.add(Box.createVerticalStrut(20));

		Box box_render = Box.createHorizontalBox();
		box_render.setAlignmentX(0f);
		button_render = new JButton();
		button_render.setAction(action_render);
		combo_throttle = new JComboBox(new Integer[] { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 });
		combo_throttle.setSelectedIndex(9);
		box_render.add(Box.createHorizontalGlue());
		box_render.add(new JLabel("Target CPU usage % "));
		box_render.add(combo_throttle);
		box_render.add(Box.createHorizontalGlue());
		box_render.add(button_render);
		box_render.add(Box.createHorizontalGlue());
		root.add(box_render);

		root.add(Box.createVerticalStrut(20));

		progbar_render = new JProgressBar();
		progbar_render.setStringPainted(true);
		progbar_render.setAlignmentX(0f);
		root.add(progbar_render);

		JScrollPane scroll_log = new JScrollPane(textarea_log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll_log.setAlignmentX(0f);
		root.add(scroll_log);

		pack();
		setResizable(false);
	}

	private class WorldChooseAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public WorldChooseAction() {
			super("Browse...");
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser(Config.getConfig().getMinecraftDir());
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				text_custom.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
		
	}
	
	private class RenderAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RenderAction() {
			super("Render");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// RenderAction.this.setEnabled(false);
			textarea_log.setText("");
			progbar_render.setValue(0);

			Preset p = new Preset();

			File worlddir = null;
			try {
				try {
					worlddir = (File) combo_saves.getSelectedItem();
				} catch (ClassCastException e) {
					worlddir = new File(text_custom.getText());
				}
			} catch (Exception e1) {
				// invalid world dir
				JOptionPane.showMessageDialog(MainFrame.this, "Invalid world directory.", "MineSpy",
						JOptionPane.ERROR_MESSAGE);
				// RenderAction.this.setEnabled(true);
				return;
			}

			// TODO polymorphism...
			Dimension dim = Dimension.OVERWORLD;
			if (radio_nether.isSelected()) {
				dim = Dimension.NETHER;
			} else if (radio_end.isSelected()) {
				dim = Dimension.END;
			}

			p.setForceBrightness(check_forcebrightness.isSelected());
			p.setCaveMode(check_cavemode.isSelected());
			p.setShowMarkers(check_showmarkers.isSelected());

			if (check_filterblocks.isSelected()) {
				try {
					int[] blocks = MineSpy.parseIntArray(text_filterblocks.getText());
					p.setBlockFilterEnabled(true);
					p.setBlockFilterInclusive(radio_filterblocks_include.isSelected());
					p.setBlockFilter(blocks);
				} catch (NumberFormatException e) {
					// invalid block filter
					JOptionPane.showMessageDialog(MainFrame.this, "Invalid block filter.", "MineSpy",
							JOptionPane.ERROR_MESSAGE);
					// RenderAction.this.setEnabled(true);
					return;
				}
			}

			if (check_filterlevels.isSelected()) {
				try {
					int[] levels = MineSpy.parseIntArray(text_filterlevels.getText());
					p.setLevelFilterEnabled(true);
					p.setLevelFilterInclusive(radio_filterlevels_include.isSelected());
					p.setLevelFilter(levels);
				} catch (NumberFormatException e) {
					// invalid level filter
					JOptionPane.showMessageDialog(MainFrame.this, "Invalid level filter.", "MineSpy",
							JOptionPane.ERROR_MESSAGE);
					// RenderAction.this.setEnabled(true);
					return;
				}
			}

			p.setMapShaderFactory((IMapShaderFactory) combo_shader.getSelectedItem());

			p.setSkylight(combo_skylight.getSelectedIndex());

			if (!check_autoname.isSelected()) {
				p.setFileName(text_output.getText());
			}

			p.setThrottle(((Integer) combo_throttle.getSelectedItem()) / 100d);

			final ImageFrame imgframe = new ImageFrame(null);
			imgframe.setLocationRelativeTo(MainFrame.this.getRootPane());
			imgframe.setVisible(true);

			final RenderListener rl = new RenderListener();
			final Timer timer_render = new Timer(200, null);
			timer_render.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int prog = rl.getProgress();
					int total = rl.getTotal();
					progbar_render.setMaximum(total);
					progbar_render.setValue(prog);
					Renderer r = rl.getRenderer();
					imgframe.setImage(r == null ? null : r.getImage());
					imgframe.setTitle(String.format("%d / %d chunks rendered [%d%%]", prog, total, prog * 100 / total));
					imgframe.repaint();
					if (rl.isDone()) {
						timer_render.stop();
						Exception te = rl.getTerminationException();
						if (te == null) {
							// normal termination

						} else {
							// abnormal termination

						}
					}
				}

			});
			timer_render.start();

			// this returns immediately
			MineSpy.renderPreset(p, worlddir, dim, rl);
		}

	}

	private static class RenderListener implements IRenderListener {

		private volatile int m_prog = 0;
		private volatile int m_total = Integer.MAX_VALUE;
		private volatile boolean m_done = false;
		private volatile Exception m_te = null;
		private volatile Renderer m_renderer = null;

		@Override
		public void notifyRenderer(Renderer r) {
			m_renderer = r;
		}

		@Override
		public void notifyProgress(int progress, int total) {
			m_prog = progress;
			m_total = total;
		}

		@Override
		public void notifyTermination(Exception te) {
			m_done = true;
			m_te = te;
		}

		public int getProgress() {
			return m_prog;
		}

		public int getTotal() {
			return m_total;
		}

		public boolean isDone() {
			return m_done;
		}

		public Exception getTerminationException() {
			return m_te;
		}

		public Renderer getRenderer() {
			return m_renderer;
		}

	}

}
