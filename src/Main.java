import java.io.*;
import java.util.*;

/**
 * A* Search algorithm for solving a m by n maze by moving 1 step at a time in 4 directions(up, down, left, right) only.
 *
 * @author Sukrucan Taylan Isikoglu
 */
public class Main {
    /**
     * Map class holds the game environment(Dimensions of the map, obstacles, starting position and goal)
     */
    static class Map {
        int width;
        int height;
        Node start;
        Node goal;
        ArrayList<Node> obstacles;

        /**
         * Initializes the game map by reading the txt file specified at pathToTxtFile
         * @param pathToTxtFile absolute path to a txt file
         * @throws FileNotFoundException
         */
        Map(String pathToTxtFile) throws FileNotFoundException {
            File file = new File(pathToTxtFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            try {
                //First line is map dimensions
                String str = reader.readLine();
                String[] firstLine = str.split(",");
                height = Integer.parseInt(firstLine[0]);
                width = Integer.parseInt(firstLine[1]);

                //Second line is starting position
                str = reader.readLine();
                String[] secondLine = str.split(",");
                start = new Node(Integer.parseInt(secondLine[0]), Integer.parseInt(secondLine[1]), 0, null);

                //Third line is goal position
                str = reader.readLine();
                String[] thirdLine = str.split(",");
                goal = new Node(Integer.parseInt(thirdLine[0]), Integer.parseInt(thirdLine[1]), 0, null);

                //Fourth line is obstacle coordinates separated by "-"
                str = reader.readLine();
                String[] fourthLine = str.split("-");
                obstacles = new ArrayList<>();
                for (String obstacle: fourthLine) {
                    String[] coordinates = obstacle.split(",");
                    obstacles.add(new Node(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]),
                            0, null));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This class represents a node in our search
     */
    static class Node {
        int x;
        int y;
        int g;
        Node parent;

        /**
         * Creates a node specified at (x,y) coordinates.
         * @param x the x coordinate of the node
         * @param y the y coordinate of the node
         * @param g the cost to move from starting position to this node
         * @param parent the parent node of this node
         */
        Node(int x, int y, int g, Node parent){
            this.x = x;
            this.y = y;
            this.g = g;

            this.parent = parent;
        }

        /**
         * Calculates the estimated cost to reach the goal from this node, using the manhattan distance heuristic.
         * @param goal The goal node we want to reach
         * @return the cost estimate
         */
        int calculateEstimate(Node goal) {
            return g + calculateManhattanDistance(this, goal);
        }

        /**
         * @param n the node we want to compare to
         * @return true if this node has same coordinates with the node n
         */
        boolean sameCoordinates(Node n) {
            return (x == n.x) && (y == n.y);
        }

        /**
         *
         * @param rhs
         * @returntrue if this node is equal to rhs, IF AND ONLY IF they are equal in position AND cost g.
         */
        @Override
        public boolean equals(Object rhs){
            if (this == rhs) {
                return true;
            } else if (rhs == null) {
                return false;
            } else if (rhs instanceof Node) {
                Node tmp = (Node) rhs;
                return x == tmp.x
                        && y == tmp.y
                        && g == tmp.g;
            }
            return false;
        }

        @Override
        public String toString(){
            return "(x,y) = (" + x + ", " + y + ")";
        }
    }

    /**
     *
     * @param a
     * @param b
     * @return the manhattan distance between the nodes a and b
     */
    static int calculateManhattanDistance(Node a, Node b){
        return Math.abs(a.x - b.x) + Math.abs(a.y + b.y);
    }

    /**
     *
     * @param n the node we want to generate successors for
     * @param map the map of the game we are playing in
     * @return the list of nodes neighboring to node n that are valid positions in the game map
     */
    static List<Node> generateSuccessors(Node n, Map map) {
        ArrayList<Node> nodes = new ArrayList<>();
        //Notice new node's g is increased by one because cost to move from starting point to this node is one
        //tile + the cost to move to the previous tile
        //Up
        Node tmp = new Node(n.x, n.y + 1, n.g + 1, n);
        if (isValid(tmp, map))
            nodes.add(tmp);
        //Down
        tmp = new Node(n.x, n.y - 1, n.g + 1, n);
        if (isValid(tmp, map))
            nodes.add(tmp);
        //Right
        tmp = new Node(n.x + 1, n.y, n.g + 1, n);
        if (isValid(tmp, map))
            nodes.add(tmp);
        //Left
        tmp = new Node(n.x - 1, n.y, n.g + 1, n);
        if (isValid(tmp, map))
            nodes.add(tmp);

        return nodes;
    }

    /**
     *
     * @param node the node we want to check for validity
     * @param map the game map we are playing in
     * @return true if the node is within bounds of the map and is not an obstacle
     */
    static boolean isValid(Node node, Map map) {
        boolean isObstacle = false;
        for (Node obstacle:
             map.obstacles) {
            isObstacle = node.sameCoordinates(obstacle);
            if (isObstacle) {
                break;
            }
        }
        return node.x > 0 && node.y > 0 && node.x <= map.width && node.y <= map.height
                && (!isObstacle);
    }

    public static void main(String[] args) {
        Map gameMap;
        try {
            gameMap = new Map("C:\\Users\\the_s\\Desktop\\input.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("input dosyası bulunamadı!");
            return;
        }

        //We use a PriorityQueue for openList so that polling it always returns the node with minimum costEstimate
        PriorityQueue<Node> openList = new PriorityQueue<Node>(10,
                Comparator.comparingInt((Node a) -> a.calculateEstimate(gameMap.goal)));
        //We use a HashSet because checking if a node is in it is O(1)
        HashSet<Node> closedList = new HashSet<>();

        openList.add(gameMap.start);

        while (!openList.isEmpty()) {
            Node tmp = openList.poll();

            //If we reached the goal, end the game and print the path
            if(tmp.x == gameMap.goal.x && tmp.y == gameMap.goal.y){
                ArrayList<Node> pathToGoal = new ArrayList<>();

                while (tmp.parent != null) {
                    pathToGoal.add(tmp);
                    tmp = tmp.parent;
                }

                for (int i = pathToGoal.size() - 1; i >= 0; i--){
                    System.out.println(pathToGoal.get(i));
                }
                break;
            }

            SuccessorLoop:
            for (Node successor: generateSuccessors(tmp, gameMap)) {
                //Check if a node with same coordinates exists in openList that has better cost estimate
                //If so skip this node
                //Since PriorityQueue.contains() is O(n) operation, we might as well check with a for loop
                for (Node n: openList) {
                    if (successor.sameCoordinates(n) && n.calculateEstimate(gameMap.goal) > successor.calculateEstimate(gameMap.goal)) {
                        continue SuccessorLoop;
                    }
                }
                //Check if a node with same coordinates exists in closedList that has better cost estimate
                //If so skip this node
                if (closedList.contains(successor)) {
                    for (Node n: closedList) {
                        if (n.sameCoordinates(successor) && n.calculateEstimate(gameMap.goal) > successor.calculateEstimate(gameMap.goal)) {
                            continue SuccessorLoop;
                        }
                    }
                }
                //If not add this node to openList
                openList.add(successor);
            }
            //Add current node to closedList
            closedList.add(tmp);
        }
    }
}

