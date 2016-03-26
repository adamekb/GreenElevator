import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * java -classpath C:\Users\Adam\Downloads\elevator\lib\elevator.jar -Djava.security.policy=C:\Users\Adam\Downloads\elevator\lib\rmi.policy -Djava.rmi.server.codebase=file:C:\Users\Adam\Downloads\elevator\lib\elevator.jar elevator.Elevators -top 5 -number 5 -tcp
 */

public class Model {

	public static final int MOVE_UP = 1;
	public static final int MOVE_DOWN = -1;
	public static final int STOP = 0;
	private final int OPEN_DOOR = 1;
	private final int CLOSE_DOOR = -1;
	private final int EMERGENCY_STOP = 32000;
	private final double PRECISION = 0.05;

	private Elevator[] elevators;
	private double velocity;
	private int nrOfElevators, floors;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader input;

	public Model (int nrOfElevators, int floors) {
		this.nrOfElevators = nrOfElevators;
		this.floors = floors;
		elevators = new Elevator[nrOfElevators + 1];
	}


	public boolean Connect(String ip, int port) {
		while (socket == null) {
			try {
				socket = new Socket(ip, port);
				writer = new PrintWriter(socket.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				return false;
			}
		}
		new Thread() {
			public void run() {
				initiate();
			}
		}.start();
		return true;
	}

	// Not using first index in elevators[] for simplicity
	public void initiate() {
		System.out.println("Initiating elevator.");
		for (int i = 1; i <= nrOfElevators; i++) {
			elevators[i] = new Elevator(floors);
			//Get current position
			sendCommand("w " + i);
		}
		//Get current velocity
		sendCommand("v");

		int initElevs = 0;

		//Making sure all elevators have been initialized
		while (initElevs < nrOfElevators) {
			boolean[] inits = new boolean[nrOfElevators + 1];
			for (int i = 1; i <= nrOfElevators; i++) {
				if (!inits[i] && elevators[i].isInitiated()) {
					initElevs++;
					inits[i] = true;
				}
			}
		}
		System.out.println("Elevator ready to run.");
	}

	public String getInput() {
		String msg = null;
		try {
			msg = input.readLine();
			while (msg == null) {
				msg = input.readLine();
			}
		} catch (Exception e) {
			return "Elevator server down.";
		}
		final String finalMsg = msg;
		new Thread() {
			public void run() {
				parseInput(finalMsg);
			}
		}.start();
		return msg;
	}

	public void parseInput(String msg) {
		String[] msgArray = msg.split(" ");
		switch (msgArray[0]) {
		case "b":
			System.out.println("Recieved: " + msg);
			sendElevator(Integer.parseInt(msgArray[1]), Integer.parseInt(msgArray[2]));
			break;
		case "p":
			System.out.println("Recieved: " + msg);
			setStop(Integer.parseInt(msgArray[1]), Integer.parseInt(msgArray[2]));
			break;
		case "v":
			velocity = Double.parseDouble(msgArray[1]);
			break;
		case "f":
			System.out.println("Recieved: " + msg);
			setPosition(Integer.parseInt(msgArray[1]), Float.parseFloat(msgArray[2]));
			break;
		default:
			System.err.println("Error reading " + msgArray);
			break;
		}
	}

	private void setPosition(int elevator, float position) {
		elevators[elevator].setPosition(position);
		int floor = Math.round(position);
		if (Math.abs(floor - position) < PRECISION) {
			setFloorIndicator(elevator, floor);
			stopElevator(elevator, floor);
		}
	}

	private void stopElevator(int elevator, int floor) {
		if (elevators[elevator].getStop(floor)) {
			elevators[elevator].setDirection(STOP);
			moveElevator(elevator, STOP);
			elevators[elevator].removeStop(floor);
			openDoor(elevator);
			int nextDirection = elevators[elevator].getNextDirection();
			if (nextDirection != STOP) {
				moveElevator(elevator, nextDirection);
			}
		}
	}


	private void openDoor(int elevator) {
		sendCommand("d " + elevator + " " +  OPEN_DOOR);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendCommand("d " + elevator + " " +  CLOSE_DOOR);
	}


	private void setStop(int elevator, int stopFloor) {
		if (stopFloor == EMERGENCY_STOP) {
			// Don't call stopElevator()
			// since it opens door.
			elevators[elevator].setDirection(STOP);
			moveElevator(elevator, STOP);
		} else {
			elevators[elevator].setStop(stopFloor);
			if (elevators[elevator].getDirection() == STOP) {
				float pos = elevators[elevator].getPosition();
				float distance = stopFloor - pos;
				if (distance < -PRECISION) { //Elevator is above
					moveElevator(elevator, MOVE_DOWN);
				} else if (distance > PRECISION) { //Elevator is down
					moveElevator(elevator, MOVE_UP);
				} else {
					elevators[elevator].removeStop(stopFloor);
					openDoor(elevator);
				}
			}
		}
	}

	private void sendElevator(int toFloor, int direction) {
		int bestElev = 999;
		float bestDist = 999;
		for (int i = 1; i <= nrOfElevators; i++) {
			float pos = elevators[i].getPosition();
			int eleDirection = elevators[i].getDirection();
			float distance = toFloor - pos;

			switch (direction) {
			case MOVE_DOWN:
				if (distance < -PRECISION) { //Elevator is above
					if (eleDirection == MOVE_UP) {
						distance = distance - (floors - 1 - pos) * 2;
					}
				} else if (distance > PRECISION) { //Elevator is down
					if (eleDirection == MOVE_UP) {
						distance = distance + (floors - 1 - pos) * 2;
					}
				}
				break;
			case MOVE_UP:
				if (distance < -PRECISION) { //Elevator is above
					if (eleDirection == MOVE_DOWN) {
						distance = distance - pos * 2;
					}
				} else if (distance > PRECISION) { //Elevator is down
					if (eleDirection == MOVE_DOWN) {
						distance = distance + pos * 2;
					}
				}
				break;
			default:
				break;
			}
			
			distance = Math.abs(distance);
			if (bestDist > distance) {
				bestDist = distance;
				bestElev = i;
			}
		}
		System.out.println("Epic algorithm have chosen elevator " + bestElev);
		setStop(bestElev, toFloor);
	}

	private void setFloorIndicator(int elevator, int floor) {
		if (elevators[elevator].getFloorIndicator() != floor) {
			elevators[elevator].setFloorIndicator(floor);
			sendCommand("s " + elevator + " " + floor);
		}
	}

	private void moveElevator(int elevator, int direction) {
		elevators[elevator].setDirection(direction);
		sendCommand("m " + elevator + " " +  direction);
	}


	public void sendCommand(String command) {
		System.out.println("Sending: " + translateCommand(command));
		synchronized (writer) {
			writer.println(command);
		}
	}

	private String translateCommand(String command) {
		String[] msgArray = command.split(" ");
		switch (msgArray[0]) {
		case "m":
			if (msgArray[2].equals("0")) {
				command = "Stop elevator " + msgArray[1];
			} else if (msgArray[2].equals("1")) {
				command = "Move elevator " + msgArray[1] + " up";
			} else {
				command = "Move elevator " + msgArray[1] + " down";
			}
			break;
		case "d":
			if (msgArray[2].equals("1")) {
				command = "Open door on elevator " + msgArray[1];
			} else {
				command = "Close door on elevator " + msgArray[1];
			}
			break;
		case "s":
			command = "Set floor indicator on elevator " + msgArray[1] + " to " + msgArray[2];
			break;
		case "w":
			command = "Inspect position of elevator " + msgArray[1];
			break;
		case "v":
			command = "Get current velocity";
			break;
		default:
			System.err.println("Error reading " + msgArray);
			break;
		}
		return command;
	}
}
