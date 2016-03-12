

public class Controller {
	private Model model;
	
	private String ELEVATOR_IP = "localhost";
	private int ELEVATOR_PORT = 4711;

	public static void main (String [] args) throws InterruptedException {
		new Controller().run(args[0], args[1]);
	}

	private void run(String nrOfElevators, String floors) throws InterruptedException {
		model = new Model(Integer.parseInt(nrOfElevators), Integer.parseInt(floors));
		
		System.out.println(model.Connect(ELEVATOR_IP, ELEVATOR_PORT));
		
		while (true) {
			System.out.println(model.getInput());
		}
	}
}
