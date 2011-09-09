package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.properties.DoubleProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class ConsumerGroup<T extends DoubleProperty> implements Iterable<Consumer<T>>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2484932094425645471L;
	private ArrayList<Consumer<T>> consumer;
	private String resourceType;

	public ConsumerGroup(String resourceType) {
		if(resourceType == null)
			throw new IllegalArgumentException();
		this.resourceType = resourceType;
		this.consumer = new ArrayList<Consumer<T>>();
	}

	/**
	 * @param consumer
	 */
	public void addConsumer(Consumer<T> consumer) {
		this.consumer.add(consumer);
	}

	/**
	 * @param result
	 */
	public void updateConsumerPriority(Result result) {
		if(result.isTie())
			return;

		int winnerIndex = -1;
		int looserIndex = -1;

		for (int i = 0; i < consumer.size(); i++) {
			if(consumer.get(i).getDefaultAgent() == result.getWinner())
				winnerIndex = i;
			else if (consumer.get(i).getDefaultAgent() == result.getLooser())
				looserIndex = i;
		}

		if (winnerIndex == -1 || looserIndex == -1) {
			throw new IllegalArgumentException("Result contains individuals not registred in this ConsumerGroup");
		}

		if(looserIndex < winnerIndex) {
			consumer.add(looserIndex, consumer.remove(winnerIndex));
		}
	}

	@Override
	public Iterator<Consumer<T>> iterator() {
		return consumer.iterator();
	}

	public void removeAllConsumer() {
		this.consumer.clear();
	}

	public String getResourceType() {
		return resourceType;
	}

	public int size() {
		return this.consumer.size();
	}
}
