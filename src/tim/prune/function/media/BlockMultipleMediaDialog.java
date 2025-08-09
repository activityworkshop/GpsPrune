package tim.prune.function.media;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import tim.prune.I18nManager;
import tim.prune.load.ItemToLoad;
import tim.prune.load.ItemToLoad.BlockStatus;

public class BlockMultipleMediaDialog extends JDialog
{
	private final ArrayList<String> _domains = new ArrayList<>();
	private final List<ItemToLoad> _itemsToLoad;


	public BlockMultipleMediaDialog(Frame inParent, Set<String> inDomains, List<ItemToLoad> inItems)
	{
		super(inParent, I18nManager.getText("dialog.loadlinkedmedia.title"), true);
		setLocationRelativeTo(inParent);
		_domains.addAll(inDomains);
		Collections.sort(_domains);
		_itemsToLoad = inItems;
		getContentPane().add(makeContents());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				buttonPressed(BlockStatus.ASKED);
			}
		});
		pack();
	}


	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(4, 4));
		mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		mainPanel.add(new JLabel(I18nManager.getText("dialog.loadlinkedmedia.blockdomains.desc") + ":"), BorderLayout.NORTH);
		// Scroll pane in the middle
		String[] domains = _domains.toArray(new String[0]);
		JList<String> domainList = new JList<>(domains);
		domainList.setEnabled(false);
		mainPanel.add(new JScrollPane(domainList), BorderLayout.CENTER);
		// Button panel at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		JButton allowButton = new JButton(I18nManager.getText("button.allowall"));
		allowButton.addActionListener(e -> buttonPressed(BlockStatus.ALLOW));
		buttonPanel.add(allowButton);
		JButton blockButton = new JButton(I18nManager.getText("button.blockall"));
		blockButton.addActionListener(e -> buttonPressed(BlockStatus.BLOCK));
		buttonPanel.add(blockButton);
		JButton chooseButton = new JButton(I18nManager.getText("button.chooseindividually"));
		chooseButton.addActionListener(e -> buttonPressed(BlockStatus.ASKED));
		buttonPanel.add(chooseButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}


	private void buttonPressed(BlockStatus inStatus)
	{
		for (ItemToLoad item : _itemsToLoad)
		{
			if (item.getBlockStatus() == BlockStatus.NOT_ASKED) {
				item.setBlockStatus(inStatus);
			}
		}
		dispose();
	}
}
