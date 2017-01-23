package ddframework.robots;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TreeInfo;

import java.util.ArrayList;
import java.util.List;

public class SmartBaseRobot extends BaseRobot {

	private MapLocation mCachedLocation;
	private TreeInfo[] mCachedVisibleTrees;
	private RobotInfo[] mCachedVisibleFriendlies;
	private RobotInfo[] mCachedVisibleHostiles;

	public SmartBaseRobot(RobotController rc) {
		super(rc);
	}

	@Override
	protected void onGameRound(RobotController rc) throws Exception {
		// get my location
		mCachedLocation = rc.getLocation();

		// sense nearby trees
		mCachedVisibleTrees = rc.senseNearbyTrees();

		// sense nearby friendly and enemy robots in a single expensive call
		List<RobotInfo> friendlies = new ArrayList<>();
		List<RobotInfo> enemies = new ArrayList<>();
		RobotInfo[] allRobots = rc.senseNearbyRobots();
		for (RobotInfo robot : allRobots) {
			if (robot.getTeam() == getTeam()) {
				friendlies.add(robot);
			} else {
				enemies.add(robot);
			}
		}
		mCachedVisibleFriendlies = friendlies.toArray(new RobotInfo[friendlies.size()]);
		mCachedVisibleHostiles = enemies.toArray(new RobotInfo[enemies.size()]);

		// Prioritize dodging incoming bullets every round
		dodgeIncomingBullets();
	}

	public MapLocation getCachedLocation() {
		return mCachedLocation;
	}

	public TreeInfo[] getCachedVisibleTrees() {
		return mCachedVisibleTrees;
	}

	public RobotInfo[] getCachedVisibleFriendlies() {
		return mCachedVisibleFriendlies;
	}

	public RobotInfo[] getCachedVisibleHostiles() {
		return mCachedVisibleHostiles;
	}
}
