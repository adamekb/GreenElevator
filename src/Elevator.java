

public class Elevator {
	private boolean stops[];
	private Float position = new Float(0);
	private Integer direction = 0;
	private Integer lastDirection = 0;
	private Boolean initiated = false;
	private Integer floorIndicator = 0;
	private Boolean willTurnAround = false;
	private Integer turningFloor = 0;


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

	public synchronized void removeStop(int floor) {
		stops[floor] = false;
		if (willTurnAround) {
			if (floor == turningFloor) {
				willTurnAround = false;
			}
		}
	}

	public Float getPosition() {
		synchronized (position) {
			return position;
		}
	}

	public void setPosition(Float position) {
		// initiated is always synchronized with position
		synchronized (position) {
			if(!initiated) {
				initiated = true;
			}
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

	public Boolean isInitiated() {
		synchronized (position) {
			return initiated;
		}
	}

	public Integer getFloorIndicator() {
		synchronized (floorIndicator) {
			return floorIndicator;
		}
	}

	public void setFloorIndicator(Integer floorIndicator) {
		synchronized (floorIndicator) {
			this.floorIndicator = floorIndicator;
		}
	}

	public Boolean willTurnAround() {
		synchronized (willTurnAround) {
			return willTurnAround;
		}
	}
	
	public void setWillTurnAround() {
		synchronized (willTurnAround) {
			this.willTurnAround = true;
		}
	}

	public Integer getTurningFloor() {
		synchronized (turningFloor) {
			return turningFloor;
		}
	}
	
	public void setTurningFloor(Integer floor) {
		synchronized (turningFloor) {
			this.turningFloor = floor;
		}
	}
}
