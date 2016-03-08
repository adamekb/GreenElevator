

public class Controller {
	private Model model;
	
	private String ELEVATOR_IP = "192.168.0.11";
	private int ELEVATOR_PORT = 4711;

	public static void main (String [] args) throws InterruptedException {
		new Controller().run(args[0], args[1]);
	}

	private void run(String nrOfElevators, String floors) throws InterruptedException {
		Model model = new Model(Integer.parseInt(nrOfElevators), Integer.parseInt(floors));
		
		String connectionStatus = model.Connect(ELEVATOR_IP, ELEVATOR_PORT);
		System.out.println(connectionStatus);
		
		while (true) {
			System.out.println(model.getInput());
		}
	}
}
