package tim.prune.undo;

import javax.swing.JFrame;

import tim.prune.App;

public class UndoManager extends UndoRedoManager
{
	/** Constructor, specifying true for undo */
	public UndoManager(App inApp, JFrame inFrame) {
		super(inApp, inFrame, true);
	}

	/** Call the App to do the actual undo */
	protected void undoRedoActions(App inApp, int inNumActions) {
		inApp.undoActions(inNumActions);
	}
}
