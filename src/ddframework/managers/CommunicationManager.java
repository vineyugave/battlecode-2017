package ddframework.managers;

import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class CommunicationManager {

	private static final int UNKNOWN = Integer.MIN_VALUE;
	private static final int IDX_STACK_END = GameConstants.BROADCAST_MAX_CHANNELS;

	public interface KnownIndexes {
		int FARMER_COUNT = 0;

		// all static indexes must be less than STACK_POINTER in order to not interfere with the stack
		int STACK_POINTER = 128;
		int STACK_START = STACK_POINTER + 1;
	}

	private final RobotController mRc;

	private boolean mInitialized;
	private int mCachedNextWritePointer;
	private boolean mDirty;

	public CommunicationManager(RobotController rc) throws Exception {
		mRc = rc;
		mInitialized = false;
		mDirty = false;
	}

	/**
	 * Initialize the stack, if necessary
	 */
	private void init() throws Exception {
		if (!mInitialized) {
			mCachedNextWritePointer = mRc.readBroadcast(KnownIndexes.STACK_POINTER);
			if (mCachedNextWritePointer == 0) {
				mCachedNextWritePointer = KnownIndexes.STACK_START;
			}
			mInitialized = true;
		}
	}

	/**
	 * Writes any bookkeeping to the buffer if necessary
	 */
	public void flush() throws Exception {
		if (mDirty) {
			init();
			mRc.broadcast(KnownIndexes.STACK_POINTER, mCachedNextWritePointer);
			mDirty = false;
		}
	}

	/**
	 * Writes to a known location
	 * @param knownLocation the location. must be less than {@link KnownIndexes#STACK_POINTER}
	 * @param value the value
	 */
	public void write(int knownLocation, int value) throws Exception {
		if (knownLocation < KnownIndexes.STACK_POINTER) {
			mRc.broadcast(knownLocation, value);
		}
	}

	/**
	 * Reads a messsage from a known location in the shared buffer
	 *
	 * @param knownLocation should be less than {@link KnownIndexes#STACK_POINTER}
	 * @return the message
	 */
	public int read(int knownLocation) throws Exception {
		return mRc.readBroadcast(knownLocation);
	}

	public int getStackSize() throws Exception {
		init();
		return mCachedNextWritePointer - KnownIndexes.STACK_START;
	}

	public boolean isEmpty() throws Exception {
		return getStackSize() == 0;
	}

	/**
	 * Push a message onto the stack
	 *
	 * @param message the message
	 * @return {@code true} if the message was pushed successfully
	 */
	public boolean pushMessage(int message) throws Exception {
		init();

		if (mCachedNextWritePointer >= IDX_STACK_END) {
			return false;
		}

		mRc.broadcast(mCachedNextWritePointer, message);
		mCachedNextWritePointer++;
		mDirty = true;

		return true;
	}

	/**
	 * Reads a message from the stack without modifying it
	 * @param idxFromTop the distance from the top of the stack to read. i.e., 0 means the most recently written message
	 * @return the message at the given index, or {@link #UNKNOWN} if the requested index is out of range
	 */
	public int readStack(int idxFromTop) throws Exception {
		init();
		final int readIdx = mCachedNextWritePointer - 1 - idxFromTop;
		if (readIdx < KnownIndexes.STACK_START) {
			return UNKNOWN;
		}
		return mRc.readBroadcast(readIdx);
	}

	/**
	 * Pops a message from the stack
	 * @return the message at the top of the stack, or {@link #UNKNOWN} if the stack is empty
	 */
	public int popMessage() throws Exception {
		init();
		if (isEmpty()) {
			return UNKNOWN;
		}

		mCachedNextWritePointer--;
		final int result = mRc.readBroadcast(mCachedNextWritePointer);
		mDirty = true;

		return result;
	}

}
