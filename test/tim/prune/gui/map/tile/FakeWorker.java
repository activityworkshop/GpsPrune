package tim.prune.gui.map.tile;

import java.util.ArrayList;


public class FakeWorker extends TileWorker
{
	public static class WorkLog {
		public final int ctr;
		public enum Action {BORN, STARTED, COMPLETED}
		public final Action action;
		public final TileDef tileDef;
		WorkLog(int inCtr, Action inAction, TileDef inDef) {
			ctr = inCtr; action = inAction; tileDef = inDef;
		}
	}

	private static int INST_COUNTER = -1;
	private static TileDef FINISHED_DEF = null;
	public static final ArrayList<WorkLog> logs = new ArrayList<>();
	private final int ctr;

	public FakeWorker(Coordinator inParent) {
		super(inParent);
		ctr = getCounter();
		logs.add(new WorkLog(ctr, WorkLog.Action.BORN, null));
	}

	public static synchronized int getCounter() {
		INST_COUNTER++;
		return INST_COUNTER;
	}

	public static void resetAll() {
		INST_COUNTER = -1;
		FINISHED_DEF = null;
		logs.clear();
	}

	public static void finish(TileDef defToFinish) {
		FINISHED_DEF = defToFinish;
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException ignored) {}
	}

	@Override
	protected TileBytes processTile(TileDef def) {
		logs.add(new WorkLog(ctr, WorkLog.Action.STARTED, def));
		while (def != FINISHED_DEF) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ignored) {}
		}
		logs.add(new WorkLog(ctr, WorkLog.Action.COMPLETED, def));
		return null;
	}
}
