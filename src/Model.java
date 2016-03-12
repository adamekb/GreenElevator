

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Model {

	private final int MOVE_UP = 1;
	private final int MOVE_DOWN = -1;
	private final int STOP = 0;
	private final int OPEN_DOOR = 1;
	private final int CLOSE_DOOR = -1;
	private final int EMERGENCY_STOP = 32000;

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
		if (Math.abs(intPos - position) < 0.06) {
			sendCommand("s " + elevator + " " + intPos);
			if (elevators[elevator].getStop(intPos)) {
				elevators[elevator].removeStop(intPos);
				moveElevator(elevator, STOP);
				openDoor(elevator);

				boolean sentAway = false;
				if (elevators[elevator].getLastDirection() == MOVE_UP) {
					for (int i = intPos; i < floors; i++) {
						if (elevators[elevator].getStop(i)) {
							sentAway = true;
							moveElevator(elevator, MOVE_UP);
						}
					}
				} else if (elevators[elevator].getLastDirection() == MOVE_DOWN) {
					for (int i = intPos; i >= 0; i--) {
						if (elevators[elevator].getStop(i)) {
							sentAway = true;
							moveElevator(elevator, MOVE_DOWN);
						}
					}
				}
				if (!sentAway) {
					float closestDist = 999;
					int closestFloor = -1;
					boolean send = false;
					for (int i = 0; i < floors; i++) {
						if (elevators[elevator].getStop(i) && closestDist > Math.abs(position - i)) {
							closestDist = Math.abs(position - i);
							closestFloor = i;
							send = true;
						}
					}
					if (send) {
						if (closestFloor > position) {
							moveElevator(elevator, MOVE_DOWN);
						} else {
							moveElevator(elevator, MOVE_UP);
						}
					}
				}
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
			moveElevator(elevator, STOP);
		} else {
			elevators[elevator].setStop(stopFloor);
			if (elevators[elevator].getDirection() == STOP) {
				float pos = elevators[elevator].getPosition();
				float distance = stopFloor - pos;
				if (distance < 0) {
					moveElevator(elevator, MOVE_DOWN);
				} else {
					moveElevator(elevator, MOVE_UP);
				}
			}
		}
	}

	private void sendElevator(int toFloor, int direction) {
		int bestElev = 999;
		float bestDist = 999;
		int moveStoppedEle = 999;
		for (int i = 1; i <= nrOfElevators; i++) {
			float pos = elevators[i].getPosition();
			int eleDirection = elevators[i].getDirection();
			float distance = toFloor - pos;

			switch (direction) {
			case MOVE_DOWN:
				if (distance <= 0) {
					if (eleDirection == MOVE_DOWN) {
						distance = - distance + pos * 2;
					} else if (eleDirection == MOVE_UP) {
						distance = - distance + (floors - 1 - pos) * 2;
					} else { // eleDirection == STOP
						moveStoppedEle = MOVE_UP;
						distance = - distance;
					}
				} else {
					if (eleDirection == MOVE_UP) {
						distance = distance + (floors - 1 - pos) * 2;
					} else if (eleDirection == STOP) {
						moveStoppedEle = MOVE_DOWN;
					}
				}
				if (bestDist > distance) {
					bestDist = distance;
					bestElev = i;
				}
				break;
			case MOVE_UP:
				if (distance <= 0) {
					if (eleDirection == MOVE_DOWN) {
						distance = - distance + pos * 2;
					} else if (eleDirection == STOP) {
						moveStoppedEle = MOVE_UP;
						distance = - distance;
					} else { // eleDirection == MOVE_UP
						distance = - distance;
					}
				} else {
					if (eleDirection == MOVE_UP) {
						distance = distance + (floors - 1 - pos) * 2;
					} else if (eleDirection == MOVE_DOWN) {
						distance = distance + pos * 2;
					} else { // eleDirection == STOP
						moveStoppedEle = MOVE_DOWN;
					}
				}
				if (bestDist > distance) {
					bestDist = distance;
					bestElev = i;
				}
				break;
			default:
				break;
			}
		}
		System.out.println("BEST ELEVATOR: " + bestElev);
		elevators[bestElev].setStop(toFloor);

		if (elevators[bestElev].getDirection() == STOP) {
			System.out.println("elevators[bestElev].getDirection() == STOP");
			moveElevator(bestElev, moveStoppedEle);
		}
	}

	private void moveElevator(int elevator, int direction) {
		elevators[elevator].setDirection(direction);
		sendCommand("m " + elevator + " " +  direction);
	}


	public void sendCommand(String command) {
		System.out.println("Sending: " + command);
		synchronized (writer) {
			writer.println(command);
		}
	}
}
