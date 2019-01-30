package cwts.networkanalysis;

import java.util.LinkedHashSet;
import java.util.Set;

public class WorkerThread extends Thread {
	Set<Runnable> taskQueue;

	public WorkerThread (Set<Runnable> taskQueue) {
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
				long start = System.nanoTime();
				node.run();
				long end = System.nanoTime();
				System.out.println(Thread.currentThread().getId() + " Run time:\t\t" + (end-start));
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}