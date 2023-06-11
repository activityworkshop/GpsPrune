package tim.prune.cmd;

import tim.prune.data.TrackInfo;

import java.util.ArrayList;

public class CompoundCommand extends Command
{
	private final ArrayList<Command> _commands = new ArrayList<>();
	private final int _updateFlags;

	public CompoundCommand() {
		this(0);
	}

	public CompoundCommand(int inUpdateFlags)
	{
		super(null);
		_updateFlags = inUpdateFlags;
	}

	protected CompoundCommand(CompoundCommand inParent)
	{
		super(inParent);
		_updateFlags = 0;
	}

	/**
	 * @param inCommand command to add to the list
	 */
	public CompoundCommand addCommand(Command inCommand)
	{
		if (inCommand != null) {
			_commands.add(inCommand);
		}
		return this;
	}

	@Override
	public int getUpdateFlags()
	{
		int flags = _updateFlags;
		for (Command command : _commands) {
			flags |= command.getUpdateFlags();
		}
		return flags;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		boolean success = true;
		for (Command command : _commands) {
			success &= command.executeCommand(inInfo);
		}
		return success;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		CompoundCommand undo = new CompoundCommand(this);
		// Undo in the opposite order
		for (int i=_commands.size()-1; i>= 0; i--)
		{
			Command command = _commands.get(i);
			Command opposite = command.makeInverse(inInfo);
			command.setInverse(opposite);
			undo.addCommand(opposite);
		}
		return undo;
	}

	/**
	 * Make the inverse command based on subcommands specified by subclass
	 * @param inInfo track info
	 * @param inInverses list of inverses supplied by subclass
	 * @return inverse of compound
	 */
	protected CompoundCommand makeInverse(TrackInfo inInfo, Command ... inInverses)
	{
		CompoundCommand inverse = new CompoundCommand(this);
		// Undo in the opposite order
		for (int i=_commands.size()-1; i>= 0; i--)
		{
			Command command = _commands.get(i);
			Command opposite = inInverses[i];
			if (opposite == null) {
				opposite = command.makeInverse(inInfo);
			}
			command.setInverse(opposite);
			inverse.addCommand(opposite);
		}
		return inverse;
	}

	/**
	 * Allow CompoundCommands to get their subcommands
	 */
	protected Command getCommand(int inIndex) {
		return _commands.get(inIndex);
	}
}
