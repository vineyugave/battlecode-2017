package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

public class SoldierRobot extends SmartBaseRobot {
    static private Direction exploreDirection;

    public SoldierRobot(RobotController controller) {
        super(controller);

        MapLocation myLocation = controller.getLocation();

        // move directly away from the gardener that spawned us to get out of the way
        final RobotInfo[] nearbyRobots = controller.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getType() == RobotType.GARDENER) {
                exploreDirection = new Direction(robot.getLocation(), myLocation);
                break;
            }
        }

        // no Gardener nearby, (very improbable) so pick a random direction
        if (exploreDirection == null) {
            exploreDirection = RandomUtil.randomDirection();
        }
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
	    super.onGameRound(rc);

	    RobotInfo[] visibleHostiles = getCachedVisibleHostiles();
	    RobotInfo[] visibleFriendlies = getCachedVisibleFriendlies();

	    // If there are any nearby enemy robots
        if (visibleHostiles.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                RobotInfo target = findClosestRobot(visibleHostiles);
                if (safeToFireAtTarget(target)){
                    rc.fireSingleShot(rc.getLocation().directionTo(target.location));
                }
            }
        }

        boolean foundHighPriorityTarget = false;
        // Stay near gardeners or archons to protect them
        if (visibleFriendlies.length > 0) {
            // search for all gardeners first
            for (RobotInfo robot : visibleFriendlies) {
                if (robot.type == RobotType.GARDENER) {
                    patrol(robot);
                    foundHighPriorityTarget = true;
                }
            }
            if (!foundHighPriorityTarget){
                // search for archons if we didn't find a gardener
                for (RobotInfo robot : visibleFriendlies) {
                    if (robot.type == RobotType.ARCHON) {
                        patrol(robot);
                        foundHighPriorityTarget = true;
                    }
                }
            }
            if (!foundHighPriorityTarget) {
                explore();
            }

        } else {
            explore();
        }

        explore();

        // TODO: potentially have global attack command to stop exploring and swarm a location
    }

    private void explore() throws GameActionException {
        RobotController rc = getRc();

        if (rc.hasMoved()) {
            return;
        }

        System.out.println("Exploring in a random direction: " + exploreDirection);
        if (!tryMove(exploreDirection)) {
            exploreDirection = exploreDirection.rotateLeftDegrees(90);
            tryMove(exploreDirection);
        }

    }
}
