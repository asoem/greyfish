package org.asoem.sico.core.simulation;

import java.util.concurrent.TimeUnit;

import org.asoem.sico.core.io.GreyfishLogger;

public class SimulationRunner implements Runnable {

	private int runs;
	private int ticks;
	private int initTicks;
	private Simulation simulation;
	private boolean infinite;
	private boolean running;
	private int sleepMillies; // 0 = infinite
	private long lastExecutionTimeMillis;

	public SimulationRunner() {
	}

	public SimulationRunner(Simulation scenario) {
		setScenario(scenario);
	}

	public void setScenario(Simulation scenario) {
		if (scenario == null)
			throw new IllegalArgumentException();
		stop();
		this.simulation = scenario;
	}

	@Override
	public void run() {
		try {
			if (simulation == null)
				throw new IllegalStateException();
			do {
				synchronized (this) {
					running = false;
					//					setChanged();
					//					notifyObservers();
					wait();
					running = true;
				}

				while (isInfinite() == false
						&& getRuns() > 0) {
					setTicks(initTicks);
					while (getTicks() > 0) {
						// do the tick
						synchronized (this) {
							execute();
							--ticks;
						}
					}
					synchronized (this) {
						if (runs > 1) {
							simulation.reset();
						}
						--runs;
					}
				}

				while (isInfinite())
					synchronized (this) {
						execute();
					}

			} while(true);
		} catch (InterruptedException e) {
			GreyfishLogger.debug("Tread interrupted!");
		}
	};

	private void execute() throws InterruptedException {
		if (sleepMillies > 0) {
			do {
				TimeUnit.MILLISECONDS.timedWait(this, 2);
			} while (System.currentTimeMillis() - lastExecutionTimeMillis < sleepMillies);
			lastExecutionTimeMillis = System.currentTimeMillis();
		}
		simulation.step();
	}

	/**
	 * Set number of ticks that should get executed per second.
	 * Set to 0 if as much as possible.
	 * @param tps
	 */
	public synchronized void setTicksPerSecond(double tps) {
		if (tps == 0)
			setInfinite(true);
		else
			sleepMillies = (int) (1000 / tps);
	}

	public synchronized int getTicks() {
		return ticks;
	}

	public synchronized void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public synchronized int getRuns() {
		return runs;
	}

	public synchronized void setRuns(int ticks, int runs) {
		this.ticks = ticks;
		this.initTicks = ticks;
		this.runs = runs;
	}

	public synchronized void start() {
		notify();
	}

	public synchronized void stop() {
		ticks = 0;
		initTicks = 0;
		runs = 0;
		infinite = false;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void setInfinite(boolean infinite) {
		this.infinite = infinite;
		sleepMillies = 0;
	}

	public synchronized boolean isInfinite() {
		return infinite;
	}

	public synchronized void pause() {
		running = false;
		try {
			wait();
		} catch (InterruptedException e) {
		}
		running = true;
	}
}
