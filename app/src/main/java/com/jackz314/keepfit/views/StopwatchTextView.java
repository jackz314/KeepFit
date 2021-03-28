/*
 * Android Stopwatch Widget: turns a TextView into a timer.
 * Copyright (C) 2011 Euan Freeman <euan04@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jackz314.keepfit.views;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.TextView;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Turns a TextView widget into a timer, with full stop-watch
 * functionality. Uses a Handler to update at a given time
 * interval.
 * 
 * The constructor takes a TextView and a time in milliseconds,
 * specifying the update interval. This implementation is
 * independent of the TextView widget.
 *  
 * @author Euan Freeman
 * 
 * @see TextView
 * @see Handler
 */
// from https://github.com/Sabirjan/Android-Stopwatch-TextView/blob/master/StopwatchTextView.java
public class StopwatchTextView implements Runnable, Closeable {
	private static final String TAG = "StopwatchTextView";

	@Override
	public void close() throws IOException {
		handlerThread.quit();
		handler.removeCallbacks(this);
	}

	public enum TimerState {STOPPED, PAUSED, RUNNING};

	private final TextView widget;
	private long updateInterval;
	private long time;
	private long startTime;
	private TimerState state;
	private final Handler handler;

	private OnTimeUpdateListener updateListener;
	private int listenerUpdateScale; // scale based on updateInterval

	private int currScale;

	private final HandlerThread handlerThread;

	private Activity activity;

	public StopwatchTextView(TextView widget, long updateInterval, Activity activity) {
		this.widget = widget;
		this.updateInterval = updateInterval;
		time = 0;
		startTime = 0;
		state = TimerState.STOPPED;
		handlerThread = new HandlerThread("StopwatchThread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		this.activity = activity;
	}

	// don't call this directly
	@Override
	public void run() {
		time = System.currentTimeMillis();
		long millis = time - startTime;
		long seconds = (long) (millis / 1000);

		activity.runOnUiThread(() -> widget.setText(String.format(Locale.getDefault(), "%02d:%02d.%03d", seconds / 60, seconds % 60, millis % 1000)));

		if (updateListener != null) {
			currScale += 1;
			if (currScale >= listenerUpdateScale){
				currScale = 0;
				updateListener.onTimeUpdate(getElapsedTime());
			}
		}

		if (state == TimerState.RUNNING) {
			handler.postDelayed(this, updateInterval);
		}
	}
	
	/**
	 * Sets the timer into a running state and
	 * initialises all time values.
	 */
	public void start() {
		startTime = time = System.currentTimeMillis();
		state = TimerState.RUNNING;
		
		handler.post(this);
	}

	/**
	 * Resets the timer.
	 */
	public void reset() {
		start();
	}

	/**
	 * Puts the timer into a paused state.
	 */
	public void pause() {
		handler.removeCallbacks(this);
		
		state = TimerState.PAUSED;
	}
	
	/**
	 * Resumes the timer.
	 */
	public void resume() {
		state = TimerState.RUNNING;
		
		startTime = System.currentTimeMillis() - (time - startTime);
		
		handler.post(this);
	}

	/**
	 * Stops the timer and resets all time values.
	 */
	public void stop() {
		handler.removeCallbacks(this);
		
		time = 0;
		startTime = 0;
		state = TimerState.STOPPED;
		
		widget.setText("00:00.000");
	}

	/**
	 * Returns the interval (in ms) at which
	 * the timer widget is updated.
	 * 
	 * @return
	 * 		Time in milliseconds
	 */
	public long getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * Sets the update interval for the
	 * timer widget.
	 * 
	 * @param updateInterval
	 * 		Interval in milliseconds
	 */
	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}
	
	/**
	 * Returns the current state of the stop-watch.
	 * 
	 * @return
	 * 		State of stop-watch
	 */
	public TimerState getState() {
		return state;
	}

	/**
	 * Returns the elapsed time so far
	 * @return elapsed time in milliseconds
	 */
	public long getElapsedTime() {
		return time - startTime;
	}

	public void setOnTimeUpdateListener(OnTimeUpdateListener listener, int updateScale){
		updateListener = listener;
		listenerUpdateScale = updateScale;
	}

	@FunctionalInterface
	public interface OnTimeUpdateListener{
		void onTimeUpdate(long elapsedTime);
	}


}