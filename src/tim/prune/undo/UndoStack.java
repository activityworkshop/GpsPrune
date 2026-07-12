package tim.prune.undo;

import tim.prune.data.Stack;

import java.util.ArrayList;
import java.util.List;

import tim.prune.cmd.Command;

/**
 * Record to hold a command together with a counter
 */
record CommandWithState(Command command, int undoCounter) {
}


/**
 * Stack of undo operations
 * which also remembers how many undos have been performed
 */
public class UndoStack extends Stack<CommandWithState>
{
	/** Number of undos (and clears) already performed */
	private int _numUndos = 0;

	public synchronized void clear()
	{
		_numUndos++;
		super.clear();
	}

	/** Add a command to the stack */
	public synchronized void add(Command inCommand) {
		super.add(new CommandWithState(inCommand, _numUndos));
	}

	/** Pop the latest command from the stack */
	public synchronized Command popCommand()
	{
		_numUndos++;
		return super.pop().command();
	}

	/** Get the list of command descriptions, starting with the most recently added */
	public List<String> getDescriptions()
	{
		ArrayList<String> commands = new ArrayList<>();
		for (CommandWithState cws : asList()) {
			commands.add(cws.command().getDescription());
		}
		return commands;
	}

	/** @return number of undos */
	public int getNumUndos()
	{
		if (isEmpty()) {
			return 0;
		}
		// Get the number of undos stored by the last operation on the stack
		return peek().undoCounter();
	}
}
