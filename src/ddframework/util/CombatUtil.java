package ddframework.util;

import battlecode.common.*;

public class CombatUtil {


    public static void attackAndFollow(RobotInfo enemyRobot, RobotController rc) throws GameActionException {
        MapLocation enemyLocation = enemyRobot.getLocation();
        MapLocation myLocation = rc.getLocation();
        Direction toEnemy = myLocation.directionTo(enemyLocation);
        Direction fromEnemy = enemyLocation.directionTo(myLocation);
        Integer keepDistance = 1;

        if (enemyRobot.type == RobotType.LUMBERJACK) {
            keepDistance = 6;
        }

        if (!rc.hasMoved()){
            if (!enemyLocation.isWithinDistance(myLocation, rc.getType().strideRadius * keepDistance)) {
                Navigation.tryMove(toEnemy, rc);
            } else {
                Navigation.tryMove(fromEnemy, rc);
            }
        }

        if (!rc.hasAttacked()) {
            if (rc.canFireSingleShot()) {
                rc.fireSingleShot(toEnemy);
            }
        }
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    public static boolean willCollideWithMe(BulletInfo bullet, RobotController rc) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI / 2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    public static void dodgeIncomingBullets(RobotController rc) {
        if (rc.hasMoved()){
            // If robot has already moved this turn then we shouldn't try dodging to save byte count
            return;
        }
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bullet : bullets) {
            if (willCollideWithMe(bullet, rc)) {
                trySidestep(bullet, rc);
            }
        }
    }

    private static void trySidestep(BulletInfo b, RobotController rc) {
        if (rc.hasMoved()){
            // If robot has already moved this turn then we shouldn't try dodging to save byte count
            return;
        }
        try {
            MapLocation myLoc = rc.getLocation();
            Direction bulletToRobot = b.location.directionTo(myLoc);
            float theta = b.dir.radiansBetween(bulletToRobot);
            if(theta < 0) {
                // Bullet on My Left
                Direction moveDir = myLoc.directionTo(b.location).rotateRightDegrees(90);
                Navigation.tryMove(moveDir, rc);
            } else {
                // Bullet on My Right
                Direction moveDir = myLoc.directionTo(b.location).rotateLeftDegrees(90);
                Navigation.tryMove(moveDir, rc);
            }
        } catch (GameActionException e) {
            System.out.println("Sidestep Attempt Failed!");
            e.printStackTrace();
        }
    }
}
