

public class Controller {
	private Model model;

	private String ELEVATOR_IP = "localhost";
	private int ELEVATOR_PORT = 4711;

	public static void main (String [] args) throws InterruptedException {
		new Controller().run(args[0], args[1]);
	}

	private void run(String nrOfElevators, String floors) throws InterruptedException {
		model = new Model(Integer.parseInt(nrOfElevators), Integer.parseInt(floors));

		while (true) {
			if (model.Connect(ELEVATOR_IP, ELEVATOR_PORT)) {
				break;
			} else {
				System.out.println("Could not connect...");
				System.out.println("Retrying in 5 seconds");
				Thread.sleep(5000);
			}
		}

		String input = model.getInput();
		while (true) {
			//System.out.println(input);
			if (input.equals("Elevator server down.")) {
				System.out.println("Shutting down controller...");
				System.exit(1);
			}
			input = model.getInput();
		}
	}
}
