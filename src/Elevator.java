

public class Elevator {
	private boolean stops[];
	private Float position;
	private Integer direction = 0;


	public Elevator(int floors) {
		stops = new boolean[floors];
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
			this.direction = direction;
		}
	}
}
