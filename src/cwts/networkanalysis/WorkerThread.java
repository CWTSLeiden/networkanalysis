package cwts.networkanalysis;

import java.util.LinkedList;

public class WorkerThread extends Thread {
	Runnable[] taskList;
	LinkedList<Integer> taskQueue;

	public WorkerThread (Runnable[] taskList, LinkedList<Integer> taskQueue) {
		this.taskList = taskList;
		this.taskQueue = taskQueue;
	}

	public void run() {
		int node;
		while (true) {
			synchronized (taskQueue) {
				while (taskQueue.isEmpty()) {
					try {
						taskQueue.wait();
					} catch (InterruptedException ex) {
						System.out.println(ex);
					}
				}
				node = taskQueue.pop();
			}
			try {
				taskList[node].run();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}