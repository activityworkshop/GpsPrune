package tim.prune.undo;

import tim.prune.cmd.Command;
import tim.prune.data.TrackInfo;

public class UndoWrapper implements UndoOperation
{
	private final Command _command;

	public UndoWrapper(Command inCommand) {
		_command = inCommand;
	}

	@Override
	public String getDescription() {
		return _command.getDescription();
	}

	@Override
	public void performUndo(TrackInfo inTrackInfo) {
		_command.getInverse().execute(inTrackInfo);
	}
}
