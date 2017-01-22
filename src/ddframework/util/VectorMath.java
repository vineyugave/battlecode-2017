package ddframework.util;

import battlecode.common.*;

public final class VectorMath {

    public static MapLocation addVec(MapLocation a, MapLocation b) {
        return new MapLocation(a.x+b.x, a.y+b.y);
    }

    public static MapLocation minusVec(MapLocation a, MapLocation b) {
        return new MapLocation(a.x-b.x, a.y-b.y);
    }

    public static MapLocation multiplyVec(double f, MapLocation a) {
        return new MapLocation((int)Math.round(f * a.x), (int)Math.round(f * a.y));
    }

    public static MapLocation negateVec(MapLocation a) {
        return new MapLocation(-a.x, -a.y);
    }

    public static float dotVec(MapLocation a, MapLocation b) {
        return a.x * b.x + a.y * b.y;
    }

    public static float dotVec(Direction d, MapLocation a) {
        if (d == null) {
            return 0;
        }
        return d.getDeltaY(1) * a.x + d.getDeltaY(1) * a.y;
    }

    public static float dotVec(Direction a, Direction b) {
        return a.getDeltaX(1) * b.getDeltaX(1) + a.getDeltaY(1) * b.getDeltaY(1);
    }

    public static Direction dirFromVec(MapLocation a) {
        MapLocation origin = new MapLocation(0,0);
        return origin.directionTo(a);
    }
}
