package ddframework.algorithms;

import java.util.PriorityQueue;

/**
 * Created by Viney Ugave (viney@vinzzz.com) on 1/23/17
 */

public class AStar extends BaseAlgorithm {

    public static float diagonalCost = 14.0f;
    public static float vHCost = 10.0f;
    private static Cell current;

    static class Cell{
        float heuristicCost = 0; //Heuristic cost
        float finalCost = 0; //G+H
        int i, j;
        Cell parent;

        Cell(int i, int j){
            this.i = i;
            this.j = j;
        }

        @Override
        public String toString(){
            return "["+this.i+", "+this.j+"]";
        }
    }

    //Blocked cells are just null Cell values in grid
    static Cell [][] grid = new Cell[5][5];

    static PriorityQueue<Cell> open;

    static boolean closed[][];
    static int startI, startJ;
    static int endI, endJ;

    public static void setCosts(float strideLength, float diagonalStrideLength){
        vHCost = strideLength;
        diagonalCost = diagonalStrideLength;
    }

    public static void setBlocked(int i, int j){
        grid[i][j] = null;
    }

    public static void setStartCell(int i, int j){
        startI = i;
        startJ = j;
    }

    public static void setEndCell(int i, int j){
        endI = i;
        endJ = j;
    }

    public static void init(int x, int y) {
        AStar.grid = new Cell[x][y];
        AStar.closed = new boolean[x][y];

        open = new PriorityQueue<>((Object o1, Object o2) -> {
            Cell c1 = (Cell)o1;
            Cell c2 = (Cell)o2;

            return c1.finalCost<c2.finalCost?-1:
                    c1.finalCost>c2.finalCost?1:0;
        });

        for(int i=0;i<x;++i){
            for(int j=0;j<y;++j){
                grid[i][j] = new Cell(i, j);
                grid[i][j].heuristicCost = Math.abs(i-endI)+Math.abs(j-endJ);
//                  System.out.print(grid[i][j].heuristicCost+" ");
            }
//              System.out.println();
        }
        grid[startI][startJ].finalCost = 0;

        //TODO figure how to dynamically update blocked cells
           /*
             Set blocked cells. Simply set the cell values to null
             for blocked cells.
           */
//        for(int i=0;i<blocked.length;++i){
//            setBlocked(blocked[i][0], blocked[i][1]);
//        }

        AStar();

        //Trace back the path
        System.out.println("******Path: ");
        Cell current = grid[endI][endJ];
        System.out.print(current);
        while(current.parent!=null){
            System.out.print(" -> "+current.parent);
            current = current.parent;
        }
        System.out.println();

//        if(closed[endI][endJ]){
//            //Trace back the path
//            System.out.println("Path: ");
//            Cell current = grid[endI][endJ];
//            System.out.print(current);
//            while(current.parent!=null){
//                System.out.print(" -> "+current.parent);
//                current = current.parent;
//            }
//            System.out.println();
//        }else System.out.println("No possible path");
    }


    static void checkAndUpdateCost(Cell current, Cell t, float cost){
        if(t == null || closed[t.i][t.j])return;
        float t_final_cost = t.heuristicCost+cost;

        boolean inOpen = open.contains(t);
        if(!inOpen || t_final_cost<t.finalCost){
            t.finalCost = t_final_cost;
            t.parent = current;
            if(!inOpen)open.add(t);
        }
    }

    public static void AStar(){

        //add the start location to open list.
        open.add(grid[startI][startJ]);


        while(true){
            current = open.poll();
            if(current==null)break;
            closed[current.i][current.j]=true;

            if(current.equals(grid[endI][endJ])){
                return;
            }

            Cell t;
            if(current.i-1>=0){
                t = grid[current.i-1][current.j];
                checkAndUpdateCost(current, t, current.finalCost+ vHCost);

                if(current.j-1>=0){
                    t = grid[current.i-1][current.j-1];
                    checkAndUpdateCost(current, t, current.finalCost+ diagonalCost);
                }

                if(current.j+1<grid[0].length){
                    t = grid[current.i-1][current.j+1];
                    checkAndUpdateCost(current, t, current.finalCost+ diagonalCost);
                }
            }

            if(current.j-1>=0){
                t = grid[current.i][current.j-1];
                checkAndUpdateCost(current, t, current.finalCost+ vHCost);
            }

            if(current.j+1<grid[0].length){
                t = grid[current.i][current.j+1];
                checkAndUpdateCost(current, t, current.finalCost+ vHCost);
            }

            if(current.i+1<grid.length){
                t = grid[current.i+1][current.j];
                checkAndUpdateCost(current, t, current.finalCost+ vHCost);

                if(current.j-1>=0){
                    t = grid[current.i+1][current.j-1];
                    checkAndUpdateCost(current, t, current.finalCost+ diagonalCost);
                }

                if(current.j+1<grid[0].length){
                    t = grid[current.i+1][current.j+1];
                    checkAndUpdateCost(current, t, current.finalCost+ diagonalCost);
                }
            }
        }
    }

    public static Cell getCurrent() {
        return current;
    }
}
