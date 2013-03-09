package minespy.gui;

import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.*;

import minespy.Config;

public class BlockFilterDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final BlockListItem[] blocklistitems = new BlockListItem[4096];

	public BlockFilterDialog(Frame owner_, String blocks_) {
		super(owner_, true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		for (int i = 0; i < 4096; i++) {
			blocklistitems[i] = new BlockListItem(i);
		}

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		JList list_selblocks = new JList();
		JScrollPane scroll_selblocks = new JScrollPane(list_selblocks, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll_selblocks.setBorder(BorderFactory.createTitledBorder("Filtered Blocks"));
		// add(scroll_selblocks);

		Box box_centre = Box.createVerticalBox();

		JButton button_add = new JButton("Add");
		button_add.setMaximumSize(new Dimension(Integer.MAX_VALUE, button_add.getMaximumSize().height));
		JButton button_remove = new JButton("Remove");
		button_remove.setMaximumSize(new Dimension(Integer.MAX_VALUE, button_remove.getMaximumSize().height));
		box_centre.add(Box.createVerticalGlue());
		box_centre.add(button_add);
		box_centre.add(button_remove);
		box_centre.add(Box.createVerticalGlue());

		// add(box_centre);

		JList list_allblocks = new JList(blocklistitems);
		JScrollPane scroll_allblocks = new JScrollPane(list_allblocks, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll_allblocks.setBorder(BorderFactory.createTitledBorder("All Blocks"));
		// add(scroll_allblocks);

		scroll_selblocks.setPreferredSize(scroll_allblocks.getPreferredSize());

		layout.setVerticalGroup(layout
				.createParallelGroup()
				.addComponent(scroll_selblocks)
				.addComponent(box_centre)
				.addComponent(scroll_allblocks));

		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addComponent(scroll_selblocks)
				.addComponent(box_centre)
				.addComponent(scroll_allblocks));
		
		pack();
		setSize(getWidth(), getWidth());
		setLocationRelativeTo(owner_);
	}

	public String showDialog() {
		setVisible(true);
		return "";
	}

	private static class BlockListItem {

		private final int blockid;

		public BlockListItem(int blockid_) {
			blockid = blockid_;
		}

		@Override
		public String toString() {
			return "[" + blockid + "] " + Config.getConfig().getBlockName(blockid);
		}

	}

	public static void main(String[] args) {
		new BlockFilterDialog(null, "").showDialog();
	}

}
