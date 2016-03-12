

public class Elevator {
	private boolean stops[];
	private Float position;
	private Integer direction = 0;
	private Integer lastDirection = 0;


	public Elevator(int floors) {
		stops = new boolean[floors];
	}
	
	public synchronized Integer getNextDirection() {
		int floor = Math.round(position);
		Integer nextDirection = Model.STOP;
		if (lastDirection == Model.MOVE_UP) {
			for (int i = floor; i < stops.length; i++) {
				if (stops[i]) {
					nextDirection = Model.MOVE_UP;
				}
			}
		} else if (lastDirection == Model.MOVE_DOWN) {
			for (int i = floor; i >= 0; i--) {
				if (stops[i]) {
					nextDirection = Model.MOVE_DOWN;
				}
			}
		}
		
		// No stop found in last direction.
		if (nextDirection == Model.STOP) {
			for (int i = 0; i < stops.length; i++) {
				if (stops[i]) {
					int distance = i - floor;
					if (distance < 0) {
						nextDirection = Model.MOVE_DOWN;
					} else if (distance > 0) {
						nextDirection = Model.MOVE_UP;
					}
				}
			}
		}
		return nextDirection;
	}

	public void setStop(int floor) {
		synchronized (stops) {
			stops[floor] = true;
		}
	}

	public boolean getStop(int floor) {
		synchronized (stops) {
			return stops[floor];
		}
	}

	public void removeStop(int floor) {
		synchronized (stops) {
			stops[floor] = false;
		}
	}

	public Float getPosition() {
		synchronized (position) {
			return position;
		}
	}

	public void setPosition(Float position) {
		synchronized (position) {
			this.position = position;
		}
	}

	public Integer getDirection() {
		synchronized (direction) {
			return direction;
		}
	}

	public void setDirection(Integer direction) {
		synchronized (direction) {
			lastDirection = this.direction;
			this.direction = direction;
		}
	}

	public Integer getLastDirection() {
		synchronized (direction) {
			return lastDirection;
		}
	}
}

//			if (distance <= 0 && eleDirection == MOVE_DOWN) {
//				if (direction == eleDirection && bestDist > distance) {
//					bestDist = distance;
//					bestElev = i;
//				}
//			} else if (distance <= 0 && eleDirection == MOVE_UP) {
//				if (direction == eleDirection && bestDist > distance) {
//					bestDist = distance;
//					bestElev = i;
//				}
//			} else if (distance >= 0 && eleDirection == MOVE_DOWN) {
//				if (direction == eleDirection && bestDist > distance) {
//					bestDist = distance;
//					bestElev = i;
//				}
//			} else if (distance >= 0 && eleDirection == MOVE_UP) {
//				if (direction == eleDirection && bestDist > distance) {
//					bestDist = distance;
//					bestElev = i;
//				}
//			} else { // dir == STOP
//				if (bestDist > distance) {
//					bestDist = distance;
//					bestElev = i;
//				}
//			}
