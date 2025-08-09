package tim.prune.function.compress;

import java.awt.Container;
import java.util.ArrayList;

import tim.prune.config.Config;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.methods.CompressionMethod;

public class PanelController implements MethodPanelContainer
{
	private final Track _track;
	private final CompressionDialog _parentDialog;
	private final ArrayList<MethodPanel> _panels = new ArrayList<>();
	private final Container _container;
	private ParameterValues _values = null;

	/** Constructor */
	public PanelController(Track inTrack, CompressionDialog inParent, Container inContainer)
	{
		_track = inTrack;
		_parentDialog = inParent;
		_container = inContainer;
	}

	/** @return the number of panels */
	public int getNumPanels() {
		return _panels.size();
	}

	/** Add a new method panel to the list, using the given config */
	public void addMethod(Config inConfig)
	{
		for (MethodPanel panel : _panels)
		{
			if (!panel.hasMethodSelected()) {
				return; // Refuse to create a new one because there's already a None
			}
		}
		MethodPanel panel = new MethodPanel(_panels.size(), this, _values, inConfig);
		addPanel(panel);
	}

	/** Add a new method panel to the list, using the given config */
	public void addMethod(CompressionMethod inMethod, Config inConfig)
	{
		MethodPanel panel = new MethodPanel(_panels.size(), this, _values, inConfig);
		panel.setMethod(inMethod);
		addPanel(panel);
	}

	/** a bit of a hack to reserve space in the gui for at least a few panels */
	public void addDummyPanels()
	{
		for (int i=0; i<5; i++) {
			addPanel(new MethodPanel(_panels.size(), this, _values, null));
		}
	}

	/** Called by a panel to move itself up */
	public void movePanelUp(int inIndex) {
		swapPanels(inIndex - 1, inIndex);
	}

	/** Called by a panel to move itself down */
	public void movePanelDown(int inIndex) {
		swapPanels(inIndex, inIndex + 1);
	}

	private void swapPanels(int inLowerIndex, int inHigherIndex)
	{
		if (inLowerIndex < 0 || inHigherIndex >= _panels.size()) {
			return; // move not allowed
		}
		MethodPanel.swapPanels(_panels.get(inLowerIndex), _panels.get(inHigherIndex));
		recalculateAll();
	}

	private void addPanel(MethodPanel inPanel)
	{
		_panels.add(inPanel);
		_container.add(inPanel);
		_container.revalidate();
		_container.repaint();
	}

	public void deletePanel(int inIndex)
	{
		if (inIndex < 0 || _panels.size() <= 1 || inIndex >= _panels.size()) {
			return;
		}
		for (int i=inIndex+1; i<_panels.size(); i++) {
			movePanelUp(i);
		}
		// Now the one to be deleted is at the end
		final MethodPanel lastPanel = _panels.get(_panels.size() - 1);
		_panels.remove(_panels.size() - 1);
		// Remove last one from scrollpanel too
		_container.remove(lastPanel);
		_container.revalidate();
		_container.repaint();
	}

	public void deleteAllPanels()
	{
		while (!_panels.isEmpty())
		{
			_container.remove(0);
			_panels.remove(0);
		}
		_container.revalidate();
		_container.repaint();
	}

	public void refresh()
	{
		for (MethodPanel panel : _panels) {
			panel.refresh();
		}
	}

	public MarkingData recalculateAll()
	{
		int totalDeleted = 0;
		MarkingData markings = new MarkingData(_track);
		for (MethodPanel panel : _panels)
		{
			CompressionMethod method = (panel == null ? null : panel.getCompressionMethod());
			if (method != null)
			{
				TrackDetails details = new TrackDetails(_track);
				int numDeletedByThisMethod = method.preview(_track, details, markings);
				panel.showNumDeleted(numDeletedByThisMethod);
				totalDeleted += numDeletedByThisMethod;
			}
		}
		_parentDialog.informNumPointsDeleted(totalDeleted);
		return markings;
	}

	public MethodList getMethodList()
	{
		MethodList methods = new MethodList();
		for (MethodPanel panel : _panels)
		{
			CompressionMethod method = (panel == null ? null : panel.getCompressionMethod());
			if (method != null){
				methods.add(method);
			}
		}
		return methods;
	}

	public void setParameterValues(ParameterValues inValues) {
		_values = inValues;
	}
}
