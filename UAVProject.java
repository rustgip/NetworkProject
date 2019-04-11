import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class UAVProject {

	static DecimalFormat df = new DecimalFormat("#.000");

	public static void main(String[] args) throws InterruptedException, IOException {

		int networkTime = 200; // Total number of executions
		double executionTime = 300; // Loop time in ms - 1000 = 1 second, change
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

		Path fileToDeletePath = Paths.get("Test.csv");
		Files.delete(fileToDeletePath);
		
		for (int z = 0; z < 1; z++) {

			CreateSimEnviron(generateNForEmandEd(networkTime), executionTime);

		}

		// After the x amount of simulations are done we need to write code to
		// print results after, below here.
		// Empty for now (Check how long each node took and how long it was
		// disconnected from wifi).

	}

	public static Network generateNForEmandEd(int networkTime) {

		Network n = new Network(300, 300, networkTime);

		// 25 dynamic nodes.

		for (int i = 0; i < 5; i++) {

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

		for (int i = 0; i < 2; i++) {

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

	public static void selectBaseDestination(Network n, Node curr, ArrayList<Node> baseStations) {

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

							System.out.println("Node #" + x.nodeNum + " is also in wifi range of: " + y.nodeNum
									+ " with distance: " + distance);

							wifiNodes.add(y);
							finalList.add(y);

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

		// Checks if in range of ANY base station for wifi purposes, not
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

			if (existingNodes.get(nodes).getType().equals("Dynamic")) {

				Node adds = existingNodes.get(nodes);
				n.movingNodes.add(adds);

			}

			else {

				Node adds = existingNodes.get(nodes);
				n.baseNodes.add(adds);

			}
		}

		for (int addDest = 0; addDest < n.movingNodes.size(); addDest++) {

			selectBaseDestination(n, n.movingNodes.get(addDest), n.baseNodes);

		}

		for (int move = 0; move < n.movingNodes.size(); move++) {

			int baseNode = n.movingNodes.get(move).baseNode;
			// System.out.println(getAngle(n.movingNodes.get(move),
			// n.baseNodes.get(baseNode - n.movingNodes.size())));
			n.movingNodes.get(move)
					.setAngle(getAngle(n.movingNodes.get(move), n.baseNodes.get(baseNode - n.movingNodes.size())));

		}

		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {

				// Determines total sim executions

				if (n.totalsimrunning < n.getRunTime()) {

					n.totalsimrunning = n.totalsimrunning + 1;

					// System.out.println(n.totalsimrunning);

					System.out.println("Elapsed time: " + (System.currentTimeMillis() - time) + "----------\n");

					HashSet<Node> currNeighbs = new HashSet<Node>();
					HashSet<Node> finalList = new HashSet<Node>();

					PrintWriter out = null;
					
					try {
						out = new PrintWriter(new FileWriter("Test.csv", true));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
			        StringBuilder sb = new StringBuilder();
					
			        sb.append(System.currentTimeMillis() - time);
			        sb.append(',');
			        
					for (int movePos = 0; movePos < n.movingNodes.size(); movePos++) {

				        sb.append("Node: " + n.movingNodes.get(movePos).nodeNum);
				        sb.append(',');
				        sb.append("X: " + n.movingNodes.get(movePos).currentX);
				        sb.append(',');
				        sb.append("Y: " + n.movingNodes.get(movePos).currentY);
				        sb.append(',');
				        sb.append(n.movingNodes.get(movePos).message);
				        sb.append(',');
				        sb.append("Base: " + n.movingNodes.get(movePos).baseNode);
				        sb.append(',');
						
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

						else {

							if (n.movingNodes.get(movePos).remainingPause > 0) {

								System.out.println("Pausing");

								n.movingNodes.get(movePos).remainingPause = n.movingNodes.get(movePos).remainingPause
										- 1;

							}

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

								selectBaseDestination(n, n.movingNodes.get(movePos), n.baseNodes);

								int baseNode = n.movingNodes.get(movePos).baseNode;

								// System.out.println(getAngle(n.movingNodes.get(movePos),
								// n.getNode(n, baseNode)) + "\n");
								n.movingNodes.get(movePos)
										.setAngle(getAngle(n.movingNodes.get(movePos), n.getNode(n, baseNode)));

							}

						}

						if (inRangeBaseStation(n, n.movingNodes.get(movePos), n.baseNodes, time)) {

							Node curr = n.getNode(n, movePos);
							finalList.add(curr);
							// System.out.println("ADDING: " + curr.nodeNum);

						}

					}
					

					sb.append('\n');
			        out.write(sb.toString());
			        out.close();

					for (Node curr : finalList) {

						for (int checkMoving = 0; checkMoving < n.movingNodes.size(); checkMoving++) {

							if (n.movingNodes.get(checkMoving).nodeNum != curr.nodeNum) {

								Node two = n.getNode(n, n.movingNodes.get(checkMoving).nodeNum);

								double distance = Math.sqrt(Math.pow((two.currentY - curr.currentY), 2)
										+ Math.pow((two.currentX - curr.currentX), 2));

								// System.out.println("D: " + distance);

								if (distance <= curr.range) {

									System.out.println("Node #" + two.nodeNum + " is also in wifi range of: "
											+ curr.nodeNum + " with distance: " + distance);
									currNeighbs.add(two);

								}

							}

						}

					}

					HashSet<Node> fin = inRangeWifi(n, finalList, currNeighbs, time);

					if (fin.isEmpty()) {

						// System.out.println("Final Wifi is empty!");

					}

					else {

						System.out.print("Final Wifi: ");

						for (Node node : fin) {

							System.out.print(node.nodeNum + " ");
							n.nodeWifiTimes.put(node.nodeNum, n.nodeWifiTimes.get(node.nodeNum) + runTime / 1000);

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

				for (Integer name : n.nodeWifiTimes.keySet()) {

					String key = name.toString();
					String value = n.nodeWifiTimes.get(name).toString();
					System.out.println("Node #" + key + " was in Wifi Range for: " + value + " seconds");

				}

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

	int baseNode = -1;

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

		Random r = new Random();
		double direction = 0 + (Math.PI - 0) * r.nextDouble();

		int random = r.nextInt(10 - 1 + 1) + 1;

		if (random % 2 == 0) {

			direction = direction * -1;

		}

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

}
