import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class UAVProject {

    static DecimalFormat df = new DecimalFormat("#.000");

    public static void main(String[] args) throws InterruptedException, IOException {

        int networkTime = 2000; // Total number of executions
        double executionTime = 100; // Loop time in ms - 1000 = 1 second, change
        // to 100-200 for faster executions

        // The other calculates interactions of nodes via a communication radius
        // (both moving and non moving nodes).
        // ctrl + f " Range = " to change the communication radius.
        // Change networkTime (right above this) to extend simulation Time,
        // decrease runTime to increase loop time (60 = 1sec).
        // You can change the bounds on the network size too.
        // Change new Network(200, 200...) to your bounds (500, 500) etc in
        // either generation method (generateNetwork...).
        // You can also change the # of static and dynamic nodes in both
        // generation methods. Just change the for loops (read further down).

        // Network sim

        // Un-comment either CreateSimEnviron() or modifiedL() below. L is the
        // spray one,
        // et and em is communication one.

        // Run one simulator for


        // Trying to remove results file from any previous executions...
        try {
            Path fileToDeletePath = Paths.get("Test.csv");
            Path secondFileToDeletePath = Paths.get("movement_plot.csv");
            Path thirdFileToDeletePath = Paths.get("base_station_coordinates.csv");
            Files.delete(fileToDeletePath);
            Files.delete(secondFileToDeletePath);
            Files.delete(thirdFileToDeletePath);
        } catch(NoSuchFileException e) {
            System.out.println("Files not found, moving on...");
        }

        //Spinning up threads for each simulation...
        for (int z = 0; z < 1; z++) {
            CreateSimEnviron(generateNForEmandEd(networkTime), executionTime);
        }

        // After the x amount of simulations are done we need to write code to
        // print results after, below here.
        // Empty for now (Check how long each node took and how long it was
        // disconnected from connectedToWifi).

    }

    // generateNFormEmandEd - This function just generates a network (i.e. Nodes, Base Stations, etc...) with the input network parameters...
    //                        The returned network is passed to the CreateSimEnvironment and utilized when actually starting the simulation.
    public static Network generateNForEmandEd(int networkTime) {

        Network n = new Network(300, 300, networkTime);

        // 25 dynamic nodes.

        for (int i = 0; i < 50; i++) {

            Node x = new Node(n, "Dynamic");

            x.setNodeNum(i);
            x.setStart();
            double speed = Math.round(x.GenerateRandom(5) + 1);
            x.setSpeed((int) speed);
            x.setPause(1);
            x.setMessage("Traveling");
            // x.setRunTime((int) Math.ceil(x.GenerateRandom(networkTime)));

            n.addNode(x);
            n.nodeWifiTimes.put(i, 0.0);

        }

        int size = n.nodes.size();

        // 10 static nodes.

        for (int i = 0; i < 15; i++) {

            Node x = new Node(n, "Base");
            x.setNodeNum(size + i);
            x.setStart();
            x.setSpeed(0);
            x.setMessage("Base");
            // System.out.println(n.nodes.size() + i);

            n.addNode(x);

        }

        return n;

    }

    // public static String checkBoundary(Network n, Node node) {
    //
    // String result = "None";
    //
    // if (node.currentX > n.getWidth()) {
    //
    // node.currentX = node.currentX - n.getWidth();
    // return "Right";
    //
    // }
    //
    // if (node.currentX < 0) {
    //
    // node.currentX = node.currentX + n.getWidth();
    // return "Left";
    //
    // }
    //
    // if (node.currentY > n.getHeight()) {
    //
    // node.currentY = node.currentY - n.getHeight();
    // return "Top";
    //
    // }
    //
    // if (node.currentY < 0) {
    //
    // node.currentY = node.currentY + n.getHeight();
    // return "Bottom";
    //
    // }
    //
    // return result;
    //
    // }

    // selectBaseDestination - Called by CreateSimEnvironment. Gives each dynamic node a base station (destination) to move to.
    //
    //                         Issue: Why are we passing the network to the function if it is not used? Commented for now...
    //                         Also, we should just prevent the base station issue earlier! Rather than blocking them here possibly...
    public static void selectBaseDestination(/*Network n,*/ Node curr, ArrayList<Node> baseStations) {

        int num = curr.baseNode;
        boolean loop = true;

        if (baseStations.size() < 2) {
            System.out.println("YOU NEED MORE THAN ONE BASE STATION SO YOU CAN SWITCH!");
            System.exit(0);
        }

        if (num == -1) {

            num = (int) curr.GenerateRandom(baseStations.size());

            curr.setBaseNode(baseStations.get(num).nodeNum);
            curr.endX = baseStations.get(num).currentX;
            curr.endY = baseStations.get(num).currentY;

        }

        else {

            // To stop from picking same base station as target dest

            while (loop) {

                // System.out.println("In here now!");

                num = (int) curr.GenerateRandom(baseStations.size());

                if (curr.baseNode == baseStations.get(num).nodeNum) {
                    // System.out.println("Repeat!");
                }

                else {

                    curr.setBaseNode(baseStations.get(num).nodeNum);
                    curr.endX = baseStations.get(num).currentX;
                    curr.endY = baseStations.get(num).currentY;
                    loop = false;

                }

            }
        }

        // Print lines for when we reach destinations and switch to new target
        // base station afer pausing

        // System.out.println("Base Node for this node: " + curr.baseNode);
        // System.out.println("Current Node's position: " + curr.currentX + ", "
        // + curr.currentY);
        // System.out.println(
        // "Base Node Position: " + baseStations.get(num).currentX + ", " +
        // baseStations.get(num).currentY);

    }

    //takes all the nodes and adjusts their routes in order to maximize wifi connectivity
    public static void routeAdjustments(Network n, ArrayList<Node> movingNodes, double dynamicNodeThreshold, double destinationThreshold){

        Node nodeToFollow = null;
        double adjustedAngle = 0.0;
        double distanceToDestination = Double.MAX_VALUE;

        //looping through all dynamic nodes
        for(Node node: movingNodes){
            //checks if current node has a base station as a neighbor
            if(!hasBaseStationNeighbor(node)) {


                //destinationThreshold check
                //if node current distance from destination is within a current threshold then just head straight to destination
                distanceToDestination = euclideanDistance(node.currentX, node.endX, node.currentY, node.endY);
                if(distanceToDestination < destinationThreshold){
                    Coordinates currentLocation = new Coordinates(node.currentX, node.currentY);
                    Coordinates destinationLocation = new Coordinates(node.endX, node.endY);
                    node.setAngle(getAngle(currentLocation, destinationLocation));
                }
                //otherwise path adjustment towards dynamic nodes with wifi connection
                else{

                    //finds node with closest destination to current node to use that nodes path to follow
                    nodeToFollow = findNodeWithClosestDestination(n, node);

                    if(nodeToFollow != null) {

                        //dynamicNodeThreshold - needs to looked into further
                        //checks to see if the destinations of nodeToFollow and current node aren't too far apart
                        //or else path adjustments will take the node in the wrong direction
                        double destinationDistanceApart = euclideanDistance(node.endX, nodeToFollow.endX, node.endY, nodeToFollow.endY);

                        //if threshold met then continue adjusting angle to follow other node
                        if (destinationDistanceApart < dynamicNodeThreshold) {
                            //adjusts angle
                            adjustedAngle = determineAdjustedAngle(node, nodeToFollow);
                            node.setAngle(adjustedAngle);
                        }
                        //if threshold not met then set angle to head directly towards destination
                        else {
                            Coordinates currentLocation = new Coordinates(node.currentX, node.currentY);
                            Coordinates destinationLocation = new Coordinates(node.endX, node.endY);
                            node.setAngle(getAngle(currentLocation, destinationLocation));
                        }

                    }

                }
            }
        }

    }

    //returns the adjusted angle taken from midway-point between destination of the new nodes
    public static double determineAdjustedAngle(Node node1, Node node2){

        //find midway-point
        Coordinates midPoint = findMidwayPoint(node1.endX, node1.endY, node2.endX, node2.endY);
        Coordinates startPoint = new Coordinates(node1.currentX, node1.currentY);
        double adjustedAngle = getAngle(startPoint, midPoint);

        return adjustedAngle;
    }

    //returns the coordinate object for the midwaypoint between the two node
    public static Coordinates findMidwayPoint(double x1, double y1, double x2, double y2){

        double midX = (x1+x2)/2;
        double midY = (y1+y2)/2;

        Coordinates midPoint = new Coordinates(midX, midY);

        return midPoint;

    }

    //checks neighbors of the node to see which of its neighbors destination is closest to current nodes destination and is also connected to wifi
    public static Node findNodeWithClosestDestination(Network n, Node currNode){

        Node bestNode = null;
        HashSet<Node> neighbors = currNode.getNeighborNodes();
        int currBaseStationNum = currNode.baseNode;
        Node currBaseStation = n.getNode(n, currBaseStationNum);
        double currX = currBaseStation.currentX;
        double currY = currBaseStation.currentY;
        int node2BaseStationNum = -1;
        Node node2BaseStation = null;
        double node2X = -1.0;
        double node2Y = -1.0;
        double bestDistance = Double.MAX_VALUE;

        //loops through set of neighbor nodes to determine which one has the closest destination to the current node's destination
        for(Node i: neighbors){
            if(i.connectedToWifi){

                node2BaseStationNum = i.baseNode;
                node2BaseStation = n.getNode(n, node2BaseStationNum);
                node2X = node2BaseStation.currentX;
                node2Y = node2BaseStation.currentY;

                if(bestDistance > euclideanDistance(currX, node2X, currY, node2Y)){
                    bestDistance = euclideanDistance(currX, node2X, currY, node2Y);
                    bestNode = i;
                }

            }
        }

        return bestNode;
    }

    //returns the euclidean distance taken from two points
    public static double euclideanDistance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(((y1 - x1) * (y1 - x1)) + ((y2 - x2) * (y2 - x2)));
    }

    //checks if any of the node's neighbors is a base station
    public static boolean hasBaseStationNeighbor(Node currNode){

        boolean hasBaseStation = false;
        HashSet<Node> neighbors = currNode.getNeighborNodes();

        //loops through set of neighbor nodes to determine if one is a base station
        for(Node i: neighbors){
            if(i.getType().equals("Base")){
                hasBaseStation = true;
            }
        }

        return hasBaseStation;

    }

    //
    public static HashSet<Node> inRangeWifi(Network n, HashSet<Node> finalList, HashSet<Node> currNs, long simStart) {

        HashSet<Node> wifiNodes = new HashSet<Node>();
        List<Node> currNeighbs = new ArrayList<Node>(currNs);

        // int counter = counter++;
        // System.out.println("COUNTER: " + counter);

        // This needs to be modified. This will check if two moving nodes are in
        // range of each other.
        // After we check, we need to implement the modified path

        for (int outer = 0; outer < currNeighbs.size(); outer++) {

            Node x = currNeighbs.get(outer);
            //@Zach: This could be doubling down on the nodes in here... Double check...
            finalList.add(x);

            for (int inner = 0; inner < n.movingNodes.size(); inner++) {

                Node y = n.movingNodes.get(inner);

                if (x.nodeNum != y.nodeNum && x.message.equals("Traveling")) {

                    Double distance = Math.sqrt((x.currentY - y.currentY) * (x.currentY - y.currentY)
                            + (x.currentX - y.currentX) * (x.currentX - y.currentX));

                    if (distance <= y.range) {

                        boolean init = false;

                        for (int g = 0; g < n.movingNodes.size(); g++) {

                            if (wifiNodes.contains(n.movingNodes.get(g).baseNode)) {

                                init = true;

                            }
                        }

                        if (!init && !finalList.contains(y)) {

                            System.out.println("Node #" + x.nodeNum + " is also in connectedToWifi range of: " + y.nodeNum
                                    + " with distance: " + distance);

                            wifiNodes.add(y);
                            finalList.add(y);
                            x.addNeighbor(y);

                        }
                    }

                }
            }
        }

        if (wifiNodes.size() > 0) {

            HashSet<Node> wifiHash = new HashSet<Node>(wifiNodes);
            inRangeWifi(n, finalList, wifiHash, simStart);

        }

        return finalList;

    }

    public static boolean inRangeBaseStation(Network n, Node curr, ArrayList<Node> baseNodeList, long simStart) {

        // Checks if in range of ANY base station for connectedToWifi purposes, not
        // destination
        boolean inRange = false;

        Node x = curr;
        Node dest = n.getNode(n, x.baseNode);

        for (int inner = 0; inner < baseNodeList.size(); inner++) {

            Node y = baseNodeList.get(inner);

            Double distance = Math.sqrt((x.currentY - y.currentY) * (x.currentY - y.currentY)
                    + (x.currentX - y.currentX) * (x.currentX - y.currentX));

            if (distance <= x.range && x.getMessage().equals("Traveling")) {

                // if (!encounteredBaseCurr.containsKey(x.nodeNum)) {

                System.out.println("Base node: " + y.nodeNum + " and Moving Node: " + x.nodeNum
                        + " are in range with distance: " + distance);

                if (y.nodeNum == dest.nodeNum) {

                    inRange = true;
                    x.addNeighbor(y);

                    System.out.println("IN RANGE OF TARGET BASE NODE!\n");
                    x.setMessage("Arrived");

                }

                else {

                    inRange = true;

                    System.out.println("In range of non target base.\n");

                }
            }
        }

        return inRange;

    }

    public static void CreateSimEnviron(Network n, double runTime) throws IOException {

        long time = System.currentTimeMillis();

        ArrayList<Node> existingNodes = n.getNodes();
        for (int nodes = 0; nodes < existingNodes.size(); nodes++) {
            // Segmenting up the network information that was passed in...
            // All dynamic nodes are separated here...
            if (existingNodes.get(nodes).getType().equals("Dynamic")) {

                Node adds = existingNodes.get(nodes);
                n.movingNodes.add(adds);

            }

            //All base stations are separated here...
            else {

                Node adds = existingNodes.get(nodes);
                n.baseNodes.add(adds);

            }
        }

        //Giving each dynamic node a base station to travel to...
        for (int addDest = 0; addDest < n.movingNodes.size(); addDest++) {

            selectBaseDestination(/*n,*/ n.movingNodes.get(addDest), n.baseNodes);

        }

        //Determining and setting the angle at which the nodes must travel to reach the destination base station...
        //
        //      Rerouting: Setting angle is going to be important for our rerouting process so this is a good example of implementation...
        for (int move = 0; move < n.movingNodes.size(); move++) {

            int baseNode = n.movingNodes.get(move).baseNode;
            // System.out.println(getAngle(n.movingNodes.get(move),
            // n.baseNodes.get(baseNode - n.movingNodes.size())));
            n.movingNodes.get(move).setAngle(getAngle(n.movingNodes.get(move), n.baseNodes.get(baseNode - n.movingNodes.size())));

        }

        PrintWriter baseStationOut = null;

        // Setting up to print out the output CSV...
        try {
            baseStationOut = new PrintWriter(new FileWriter("base_station_coordinates.csv", true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int baseStationIndex = 0; baseStationIndex < n.baseNodes.size(); baseStationIndex++) {
            Node tempBaseStation = n.baseNodes.get(baseStationIndex);
            baseStationOut.write(tempBaseStation.nodeNum + "," + tempBaseStation.currentX + "," + tempBaseStation.currentY + "\n");
        }

        baseStationOut.close();

        //Simulation setup is complete, actually start the node movement...
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {

                // Determines total sim executions
                //
                //      Rerouting: totalsimrunning is set to 0 initially and is only incremented here...
                //                 I am really confused by the use of System.currentTImeMillis here though.
                //                 Im not sure how youre making sure this works correctly.
                //                 To be honest, Im not sure why System time us used at all... Its hard to follow.
                if (n.totalsimrunning < n.getRunTime()) {

                    n.totalsimrunning = n.totalsimrunning + 1;

                    // System.out.println(n.totalsimrunning);

                    System.out.println("Elapsed time: " + (System.currentTimeMillis() - time) + "----------\n");

                    HashSet<Node> currNeighbs = new HashSet<Node>();
                    HashSet<Node> finalList = new HashSet<Node>();

                    PrintWriter out = null;
                    PrintWriter movementOut = null;

                    // Setting up to print out the output CSV...
                    try {
                        out = new PrintWriter(new FileWriter("Test.csv", true));
                        movementOut = new PrintWriter(new FileWriter("movement_plot.csv", true));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    StringBuilder sb = new StringBuilder();
                    StringBuilder plottingSb = new StringBuilder();

                    sb.append(System.currentTimeMillis() - time);
                    sb.append(',');
                    plottingSb.append(n.totalsimrunning + ":");

                    // Lets decrypt this wizardry here...
                    // Its seeming like a lot of string building for logging purposes...
                    for (int movePos = 0; movePos < n.movingNodes.size(); movePos++) {

                        // We clear the list of neighborNodes for some reason...
                        //
                        // Pulkit says to clear the list of neighbor nodes after every iteration...
                        //      Apparently every other list in the simulation gets purged after each iteration...
                        n.movingNodes.get(movePos).neighborNodes = new HashSet<Node>();

                        sb.append("Node: " + n.movingNodes.get(movePos).nodeNum);
                        sb.append(',');
                        sb.append("X: " + n.movingNodes.get(movePos).currentX);
                        sb.append(',');
                        sb.append("Y: " + n.movingNodes.get(movePos).currentY);
                        sb.append(',');
                        sb.append(n.movingNodes.get(movePos).message);
                        sb.append(',');
                        sb.append("Dest: " + n.movingNodes.get(movePos).baseNode);
                        sb.append(',');

                        plottingSb.append(n.movingNodes.get(movePos).nodeNum + ",");
                        plottingSb.append(n.movingNodes.get(movePos).currentX + ",");
                        plottingSb.append(n.movingNodes.get(movePos).currentY + ",");
                        plottingSb.append(n.movingNodes.get(movePos).baseNode + ";");

                        // We actually update nodes accordinaly here...
                        // Traveling node's positions are incremented. - Traveling is default.
                        if (n.movingNodes.get(movePos).message.equals("Traveling")) {

                            double speed = n.movingNodes.get(movePos).speed;
                            // double ratio = speed * (runTime / 60);
                            double ratio = speed;
                            // System.out.println(ratio);

                            double x_inc = (ratio * Math.cos(n.movingNodes.get(movePos).angle));
                            double y_inc = (ratio * Math.sin(n.movingNodes.get(movePos).angle));

                            // System.out.println(x_inc + " - " + y_inc);

                            n.movingNodes.get(movePos).currentX = n.movingNodes.get(movePos).currentX + x_inc;
                            n.movingNodes.get(movePos).currentY = n.movingNodes.get(movePos).currentY + y_inc;

                            // System.out.println("(" +
                            // n.movingNodes.get(movePos).currentX + ", " +
                            // n.movingNodes.get(movePos).currentY + ")");
                            // checkBoundary(n, n.movingNodes.get(movePos));

                            // n.movingNodes.get(movePos).runningTime = (int)
                            // (n.movingNodes.get(movePos).runningTime - 1);

                            // If we're not in range of a base station, then
                            // check if we're in range of another node.
                            // Inside that, then check if that node is in range
                            // of a basestation using recursion.

                        }
                        // If its not labeled as traveling, then it must be paused for some reason...
                        // Various conditions may apply here...
                        else {
                            // If pause time remaining, decrement...
                            if (n.movingNodes.get(movePos).remainingPause > 0) {

                                System.out.println("Pausing");

                                n.movingNodes.get(movePos).remainingPause = n.movingNodes.get(movePos).remainingPause - 1;

                            }
                            // If pause time over, reset movement parameters and set too traveling status.
                            else {

                                System.out.println("Unpausing\n");

                                Node curr = n.getNode(n, movePos);
                                curr.setCoords(Double.parseDouble(df.format(curr.currentX)),
                                        Double.parseDouble(df.format(curr.currentY)));

                                // n.movingNodes.get(movePos).setAngle();
                                // n.movingNodes.get(movePos).setPause(n.movingNodes.get(movePos).GenerateRandom(3)
                                // * 60);
                                n.movingNodes.get(movePos).setPause(1);
                                // n.movingNodes.get(movePos).setRunTime(
                                // (int)
                                // Math.ceil(n.movingNodes.get(movePos).GenerateRandom(n.getRunTime())));
                                double speed = Math.round(n.movingNodes.get(movePos).GenerateRandom(5) + 1);
                                n.movingNodes.get(movePos).setSpeed((int) speed);
                                // System.out.println("New Angle/Direction: " +
                                // n.movingNodes.get(movePos).angle);
                                n.movingNodes.get(movePos).setMessage("Traveling");

                                selectBaseDestination(/*n,*/ n.movingNodes.get(movePos), n.baseNodes);

                                int baseNode = n.movingNodes.get(movePos).baseNode;

                                // System.out.println(getAngle(n.movingNodes.get(movePos),
                                // n.getNode(n, baseNode)) + "\n");
                                n.movingNodes.get(movePos)
                                        .setAngle(getAngle(n.movingNodes.get(movePos), n.getNode(n, baseNode)));

                            }

                        }

                        //If the current node is within range of its destination base station, add it to the list of arrived nodes...
                        //
                        //      Rerouting: We may need to add additional code here!
                        //                 We would want to not only check inRageBaseStation, but also if it can hear
                        //                 any of the other dynamic nodes and, if so, is it connected to a connectedToWifi?
                        if (inRangeBaseStation(n, n.movingNodes.get(movePos), n.baseNodes, time)) {

                            Node curr = n.getNode(n, movePos);
                            finalList.add(curr);
                            // System.out.println("ADDING: " + curr.nodeNum);

                        }

                    }


                    sb.append('\n');
                    plottingSb.append("\n");
                    out.write(sb.toString());
                    movementOut.write(plottingSb.toString());
                    out.close();
                    movementOut.close();

                    // For every node within the list of nodes that are within range of a base station...
                    for (Node curr : finalList) {

                        //For every node within the list of dynamic nodes...
                        for (int checkMoving = 0; checkMoving < n.movingNodes.size(); checkMoving++) {

                            // Make sure that the node within range of destination isnt the same node.
                            if (n.movingNodes.get(checkMoving).nodeNum != curr.nodeNum) {

                                Node two = n.getNode(n, n.movingNodes.get(checkMoving).nodeNum);

                                double distance = Math.sqrt(Math.pow((two.currentY - curr.currentY), 2)
                                        + Math.pow((two.currentX - curr.currentX), 2));

                                // System.out.println("D: " + distance);

                                if (distance <= curr.range) {

                                    System.out.println("Node #" + two.nodeNum + " is also in connectedToWifi range of: "
                                            + curr.nodeNum + " with distance: " + distance);
                                    currNeighbs.add(two);
                                    curr.addNeighbor(two);

                                    // Rerouting: I think we want to set the current node's connectedToWifi-connected flag to true.
                                    //            I think we also want to add the node to the list of connectedToWifi-connected nodes in finalNodes.
                                    //curr.connectedToWifi = true;
                                    //finalList.add(two);
                                }

                            }

                        }

                    }

                    // This function call takes the list of dynamic nodes that are connected to a base station and then
                    // updates the dynamic nodes if they are multi-hop connected.
                    HashSet<Node> fin = inRangeWifi(n, finalList, currNeighbs, time);

                    double destinationThreshold = 2 * n.movingNodes.get(0).range;
                    double dynamicNodeThreshold = 2 * n.movingNodes.get(0).range;

                    //function calls takes list of all nodes and adjusts their paths
                    //comment out line below to run without route adjustments aka all nodes will move in straight
                    //line towards destination
                    routeAdjustments(n, n.movingNodes, dynamicNodeThreshold, destinationThreshold);

                    //No one is connected to a base station if this if statement is true!
                    if (fin.isEmpty()) {
                        // System.out.println("Final Wifi is empty!");
                    }

                    else {


                        // This segment of code just seems to go through the list of dynamic nodes and creates a list of
                        // every node that was connected at a certain time interval within the nodeWifiTimes
                        System.out.print("Final Wifi: ");

                        for (Node node : fin) {

                            System.out.print(node.nodeNum + " ");
                            node.connectedToWifi = true;
                            n.nodeWifiTimes.put(node.nodeNum, n.nodeWifiTimes.get(node.nodeNum) + runTime / 1000);

                        }

                        System.out.println("\n");

						/*
						//Printing out neighbors
						for (int i = 0; i < existingNodes.size(); i++){
							HashSet<Node> neighbors = existingNodes.get(i).getNeighborNodes();
							System.out.print("Node# " + existingNodes.get(i).nodeNum + " Neighbors: ");
							for (Node node : neighbors) {
								System.out.print(node.nodeNum + " ");
							}
						}
						*/

                        for (Node node : fin) {

                            node.connectedToWifi = false;

                        }

                        System.out.println("\n");



                    }
                }

                else {

                    t.cancel();
                    t.purge();

                }

            }

        }, 0, (int) runTime);

        long totalTime = (long) ((n.getRunTime() * runTime) + 5000);


        Timer x = new Timer();
        x.schedule(new TimerTask() {
            @Override
            public void run() {

                // System.out.println("-------\n(DONE)");

                for (int printMove = 0; printMove < n.movingNodes.size(); printMove++) {

                    // System.out.println("Node #: " + (printMove));
                    // System.out.println("Start: (" +
                    // n.movingNodes.get(printMove).startX + ", "
                    // + n.movingNodes.get(printMove).startY + ")");
                    // System.out.println("Finish: (" +
                    // n.movingNodes.get(printMove).currentX + ", "
                    // + n.movingNodes.get(printMove).currentY + ")");

                    Node curr = n.getNode(n, printMove);
                    curr.setCoords(curr.currentX, curr.currentY);

                    // System.out.println("Angle/Direction: " +
                    // n.movingNodes.get(printMove).angle + "\n");

                }

                double keyCounter = 0.0;
                double wifiTimeAverage = 0.0;

                for (Integer name : n.nodeWifiTimes.keySet()) {

                    keyCounter++;
                    wifiTimeAverage += n.nodeWifiTimes.get(name);
                    String key = name.toString();
                    String value = n.nodeWifiTimes.get(name).toString();
                    System.out.println("Node #" + key + " was in Wifi Range for: " + value + " seconds");

                }

                wifiTimeAverage = wifiTimeAverage / keyCounter;
                System.out.println("Average Node Time Connected to Wifi: " + wifiTimeAverage + " seconds");
                System.out.println(
                        "Total simulation time: " + ((System.currentTimeMillis() - time) - 5000) / 1000 + " seconds");

                x.cancel();
                x.purge();

                // System.out.println("\nList of encountered Dynamic nodes: ");

            }

        }, totalTime, 2147483647);

    }

    public static double getAngle(Node firstPoint, Node secondPoint) {

        double deltaX = secondPoint.currentX - firstPoint.currentX;
        double deltaY = secondPoint.currentY - firstPoint.currentY;
        double angle = Math.atan2(deltaY, deltaX);

        return angle;

    }

    public static double getAngle(Coordinates startPoint, Coordinates endPoint){

        double deltaX = endPoint.x - startPoint.x;
        double deltaY = endPoint.y - startPoint.y;
        double angle = Math.atan2(deltaY, deltaX);

        return angle;

    }

    public static Object copy(Object orig) {
        Object obj = null;
        try {

            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

}

class Network implements Serializable {

    HashMap<Integer, Double> nodeWifiTimes = new HashMap<Integer, Double>();

    ArrayList<Node> movingNodes = new ArrayList<Node>();
    ArrayList<Node> baseNodes = new ArrayList<Node>();

    private int width;
    private int height;
    private int totalruntime;
    int totalsimrunning = 0;
    int totalCopied = 0;

    ArrayList<Node> nodes = new ArrayList<Node>();

    public Network(int width, int height, int time) {

        this.width = width;
        this.height = height;
        this.totalruntime = time;

    }

    public void addNode(Node node) {

        nodes.add(node);

    }

    public ArrayList<Node> getNodes() {

        return nodes;

    }

    public Node getNode(Network n, int num) {

        for (int i = 0; i < n.nodes.size(); i++) {

            if (n.nodes.get(i).nodeNum == num) {

                return n.nodes.get(i);

            }

        }

        return null;

    }

    public int getRunTime() {

        return this.totalruntime;

    }

    public int getWidth() {

        return this.width;

    }

    public int getHeight() {

        return this.height;

    }

    public int getTotalRunning() {

        return this.totalsimrunning;

    }

}

class Coordinates implements Serializable {

    double x;
    double y;

    public Coordinates(double x, double y) {

        this.x = x;
        this.y = y;

    }

}

class Node implements Serializable {

    ArrayList<Coordinates> coords = new ArrayList<Coordinates>();
    HashSet<Node> neighborNodes = new HashSet<Node>();

    int baseNode = -1;
    boolean connectedToWifi = false;

    double initialPause;
    double remainingPause;
    double angle;
    double distanceTraveled = 0;

    int nodeNum;
    int speed;
    int runningTime;
    int range = 25;

    double startX;
    double startY;
    double currentX;
    double currentY;

    double endX;
    double endY;

    double wifiConnect = 0;

    String type = null;
    String message = null;
    Network n;

    public Node(Network network, String type) {

        this.n = network;
        this.type = type;

    }

    public void setBaseNode(int num) {

        this.baseNode = num;

    }

    public void setCoords(double x, double y) {

        DecimalFormat df = new DecimalFormat("#.000");

        Coordinates c = new Coordinates(Double.valueOf(df.format(x)), Double.valueOf(df.format(y)));

        boolean exists = false;

        for (int i = 0; i < this.coords.size(); i++) {

            if (this.coords.get(i).x == x && this.coords.get(i).y == y) {

                exists = true;

            }

        }

        if (!exists) {

            this.coords.add(c);

        }

    }

    public void setStart() {

        DecimalFormat df = new DecimalFormat("#.000");

        this.startX = GenerateRandom(n.getWidth());
        this.startY = GenerateRandom(n.getHeight());
        this.currentX = this.startX;
        this.currentY = this.startY;

        Coordinates c = new Coordinates(Double.valueOf(df.format(this.startX)), Double.valueOf(df.format(this.startY)));
        this.coords.add(c);

    }

    public void setNodeNum(int NodeNum) {

        this.nodeNum = NodeNum;

    }

    public void setSpeed(int Speed) {

        this.speed = Speed;

    }

    public void setPause(double Pause) {

        this.initialPause = Pause;
        this.remainingPause = Pause;

    }

    public void setRunTime(int Time) {

        this.runningTime = Time;

    }

    public void setAngle(double Angle) {

        /*
        Random r = new Random();
        double direction = 0 + (Math.PI - 0) * r.nextDouble();


        int random = r.nextInt(10 - 1 + 1) + 1;

        if (random % 2 == 0) {

            direction = direction * -1;

        }
        */
        // this.angle = direction;
        this.angle = Angle;

    }

    public void setMessage(String newMessage) {

        this.message = newMessage;

    }

    public String getType() {

        return this.type;

    }

    public Double getDistanceT() {

        return this.distanceTraveled;

    }

    public String getMessage() {

        return this.message;

    }

    public double GenerateRandom(double value) {

        Random r = new Random();
        double randomValue = 0 + (value - 0) * r.nextDouble();

        return randomValue;

    }

    public void addNeighbor(Node neighbor){
        neighborNodes.add(neighbor);
    }

    public void removeNeighbor(Node neighbor){
        neighborNodes.remove(neighbor);
    }

    public HashSet<Node> getNeighborNodes(){
        return neighborNodes;
    }

}

