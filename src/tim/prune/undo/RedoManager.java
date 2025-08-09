package tim.prune.undo;

import javax.swing.JFrame;

import tim.prune.App;

public class RedoManager extends UndoRedoManager
{
	/** Constructor, specifying false for redo, not undo */
	public RedoManager(App inApp, JFrame inFrame) {
		super(inApp, inFrame, false);
	}

	protected void undoRedoActions(App inApp, int inNumActions) {
		inApp.redoActions(inNumActions);
	}
}
