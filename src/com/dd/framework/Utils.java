package com.dd.framework;

import battlecode.common.Direction;

import java.util.Random;

public final class Utils {

	public static final long RANDOM_SEED = 5;

	private static final Random sRand = new Random(RANDOM_SEED);

	private Utils() {
		// do not instantiate
	}

	/**
	 * Returns a random Direction
	 *
	 * @return a random Direction
	 */
	public static Direction randomDirection() {
		return new Direction((float) Math.random() * 2 * (float) Math.PI);
	}

	public static void shuffle(Object[] array) {
		final int size = array.length;
		for (int i = 0; i < size; i++) {
			int pos = sRand.nextInt(size);
			swap(array, i, pos);
		}
	}

	private static void swap(Object[] array, int i, int j) {
		Object t = array[i];
		array[i] = array[j];
		array[j] = t;
	}


}
