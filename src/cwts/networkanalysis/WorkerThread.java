package cwts.networkanalysis;

import java.util.LinkedHashSet;
import java.util.Set;

public class WorkerThread extends Thread {
	Runnable[] taskList;
	Set<Runnable> taskQueue;

	public WorkerThread (Runnable[] taskList, Set<Runnable> taskQueue) {
		this.taskList = taskList;
		this.taskQueue = taskQueue;
	}

	public void run() {
		Runnable node;
		while (true) {
			synchronized (taskQueue) {
				while (taskQueue.isEmpty()) {
					try {
						taskQueue.wait();
					} catch (InterruptedException ex) {
						System.out.println(ex);
					}
				}
				node = taskQueue.iterator().next();
				taskQueue.remove(node);
			}
			try {
				node.run();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}