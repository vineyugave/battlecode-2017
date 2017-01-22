package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.BaseRobot;
import ddframework.util.CombatUtil;
import ddframework.util.Navigation;
import ddframework.util.RandomUtil;

import java.util.Random;

public class SoldierRobot extends BaseRobot {
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
            exploreDirection = new Direction((float)Math.random() * 2 * (float)Math.PI);
        }
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        // If there are any nearby enemy robots
        if (visibleHostiles.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                RobotInfo target = Navigation.findClosestRobot(visibleHostiles,rc);
                rc.fireSingleShot(rc.getLocation().directionTo(target.location));
            }
        }

        boolean foundHighPriorityTarget = false;
        // Stay near gardeners or archons to protect them
        if (visibleFriendlies.length > 0) {
            // search for all gardeners first
            for (RobotInfo robot : visibleFriendlies) {
                if (robot.type == RobotType.GARDENER) {
                    Navigation.patrol(robot, rc);
                    foundHighPriorityTarget = true;
                }
            }
            if (!foundHighPriorityTarget){
                // search for archons if we didn't find a gardener
                for (RobotInfo robot : visibleFriendlies) {
                    if (robot.type == RobotType.ARCHON) {
                        Navigation.patrol(robot, rc);
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
        if (!Navigation.tryMove(exploreDirection, rc)) {
            exploreDirection = exploreDirection.rotateLeftDegrees(90);
            Navigation.tryMove(exploreDirection, rc);
        }

    }
}
