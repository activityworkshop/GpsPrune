package tim.prune.undo;

import java.util.Stack;

import tim.prune.cmd.Command;

/**
 * Class to hold a command together with a counter
 */
class CommandWithState
{
	public final Command _command;
	public final int _undoCounter;

	/** Constructor */
	public CommandWithState(Command inCommand, int inCounter)
	{
		_command = inCommand;
		_undoCounter = inCounter;
	}
}


/**
 * Stack of undo operations
 * which also remembers how many undos have been performed
 */
public class UndoStack extends Stack<CommandWithState>
{
	/** Number of undos (and clears) already performed */
	private int _numUndos = 0;

	@Override
	public synchronized void clear()
	{
		_numUndos++;
		super.clear();
	}

	/** Add a command to the stack */
	public synchronized boolean add(Command inCommand) {
		return super.add(new CommandWithState(inCommand, _numUndos));
	}

	/** Pop the latest command from the stack */
	public synchronized Command popCommand()
	{
		_numUndos++;
		return super.pop()._command;
	}

	/** Get the command at the given index */
	public Command getCommandAt(int inIndex) {
		return super.elementAt(inIndex)._command;
	}

	/** @return number of undos */
	public int getNumUndos()
	{
		if (isEmpty()) {return 0;}
		// Get the number of undos stored by the last operation on the stack
		return peek()._undoCounter;
	}
}
