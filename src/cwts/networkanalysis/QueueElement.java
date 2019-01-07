package cwts.networkanalysis;

public class QueueElement {
	public int queuePosition;
	public int nodeNumber;

	public QueueElement(int queuePosition, int nodeNumber) {
		this.queuePosition = queuePosition;
		this.nodeNumber = nodeNumber;
	}

	public int getQueuePosition () {
		return queuePosition;
	}

	public int getNodeNumber () {
		return nodeNumber;
	}
}