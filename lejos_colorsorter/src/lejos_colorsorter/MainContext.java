package lejos_colorsorter;

import log_manager.LogFileManager;

public class MainContext {

	public static void main(String[] args) {
		LogFileManager.start();
		WorkflowManager w = new WorkflowManager();
		w.start();
		LogFileManager.close();
	}

}
