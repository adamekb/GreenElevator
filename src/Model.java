

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Model {

	private int MOVE_UP = 1;
	private int MOVE_DOWN = -1;
	private int STOP = 0;
	private int OPEN_DOOR = 1;
	private int CLOSE_DOOR = -1;

	private Elevator[] elevators;
	private double velocity;
	private int nrOfElevators, floors;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader input;

	public Model (int nrOfElevators, int floors) {
		this.nrOfElevators = nrOfElevators;
		this.floors = floors;
		elevators = new Elevator[nrOfElevators+1];
	}


	public String Connect(String ip, int port) {
		while (socket == null) {
			try {
				socket = new Socket(ip, port);
				writer = new PrintWriter(socket.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				return "Could not connect";
			}
		}
		
		for (int i = 1; i <= nrOfElevators; i++) {
			elevators[i] = new Elevator(floors);
			sendCommand("w " + i);
		}
		sendCommand("v");
		
		return "Connected...";
	}

	public String getInput() {
		String msg = null;
		try {
			msg = input.readLine();
			while (msg == null) {
				msg = input.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
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
			sendElevator(Integer.parseInt(msgArray[1]), Integer.parseInt(msgArray[2]));
			break;
		case "p":
			setStop(Integer.parseInt(msgArray[1]), Integer.parseInt(msgArray[2]));
			break;
		case "v":
			velocity = Double.parseDouble(msgArray[1]);
			break;
		case "f":
			setPosition(Integer.parseInt(msgArray[1]), Float.parseFloat(msgArray[2]));
			break;
		default:
			System.err.println("Error reading " + msgArray);
			break;
		}
	}

	private void setPosition(int elevator, float position) {
		elevators[elevator].setPosition(position);
		int intPos = Math.round(position);
		if (Math.abs(intPos - position) < 0.025 && elevators[elevator].getStop(intPos)) {
			elevators[elevator].removeStop(intPos);
			sendCommand("m " + elevator + " " +  STOP);
			openDoor(elevator);
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
		elevators[elevator].setStop(stopFloor);
		if (elevators[elevator].getDirection() == STOP) {
			float pos = elevators[elevator].getPosition();
			float distance = stopFloor - pos;
			if (distance < 0) {
				sendCommand("m " + elevator + " " +  MOVE_DOWN);
			} else {
				sendCommand("m " + elevator + " " +  MOVE_UP);
			}
		}
		
	}


	private void sendElevator(int toFloor, int direction) {
		int bestElev = 0;
		float bestDist = 999;
		for (int i = 1; i <= nrOfElevators; i++) {
			float pos = elevators[i].getPosition();
			int dir = elevators[i].getDirection();
			float distance = toFloor - pos;
			if (distance <= 0 && dir == MOVE_DOWN) {
				if (direction == dir && bestDist > distance) {
					bestDist = distance;
					bestElev = i;
				}
			} else if (distance >= 0 && dir == MOVE_UP) {
				if (direction == dir && bestDist > distance) {
					bestDist = distance;
					bestElev = i;
				}
			} else { // dir == STOP
				if (bestDist > distance) {
					bestDist = distance;
					bestElev = i;
				}
			}
			elevators[bestElev].setStop(toFloor);
			
			if (elevators[bestElev].getDirection() == STOP) {
				if (bestDist < 0) {
					sendCommand("m " + bestElev + " " +  MOVE_DOWN);
				} else if (bestDist > 0) {
					sendCommand("m " + bestElev + " " + MOVE_UP);
				} else {
					elevators[bestElev].removeStop(toFloor);
					openDoor(bestElev);	
				}
			}
		}
	}

	public void sendCommand(String command) {
		System.out.println("Sending: " + command);
		synchronized (writer) {
			writer.println(command);
		}
	}
}
