package tim.prune.cmd;

import tim.prune.data.TrackInfo;

/**
 * Superclass of all commands, which can be executed on the TrackInfo object
 */
public abstract class Command
{
	private Command _inverse;
	private final boolean _isUndo;
	// description is used for undo and redo
	private String _descriptionText = null;
	private String _confirmText = null;


	protected Command(Command inParent) {
		_inverse = inParent;
		_isUndo = (_inverse != null);
	}

	public Command getInverse() {
		return _inverse;
	}

	protected boolean isUndo() {
		return _isUndo;
	}

	public final boolean execute(TrackInfo inInfo)
	{
		if (_inverse == null)
		{
			_inverse = makeInverse(inInfo);
			assert _inverse == null || _inverse._inverse != null;
		}
		return executeCommand(inInfo);
	}

	protected void setInverse(Command inCommand) {
		_inverse = inCommand;
	}

	protected abstract boolean executeCommand(TrackInfo inInfo);

	protected abstract Command makeInverse(TrackInfo inInfo);

	public String getDescription() {
		return _descriptionText;
	}

	public String getConfirmText() {
		return _confirmText;
	}

	public void setDescription(String inText) {
		_descriptionText = inText;
	}

	public void setConfirmText(String inText) {
		_confirmText = inText;
	}

	public abstract int getUpdateFlags();
}
