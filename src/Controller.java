

public class Controller {

	private static String DEFAULT_ELEVATOR_IP = "localhost";
	private static String DEFAULT_ELEVATOR_PORT = "4711";
	private static String DEFAULT_NR_OF_ELEVATORS = "5";
	private static String DEFAULT_NR_OF_FLOORS = "6";

	public static void main (String [] args) throws InterruptedException {
		if (args.length == 2) {
			new Controller().run(args[0], args[1], DEFAULT_ELEVATOR_IP, DEFAULT_ELEVATOR_PORT);
		} else if (args.length == 4) {
			new Controller().run(args[0], args[1], args[2], args[3]);
		} else {
			new Controller().run(DEFAULT_NR_OF_ELEVATORS, DEFAULT_NR_OF_FLOORS, DEFAULT_ELEVATOR_IP, DEFAULT_ELEVATOR_PORT);
		}
	}

	private void run(String nrOfElevators, String floors, String ip, String port) 
			throws InterruptedException {
		Model model = new Model(Integer.parseInt(nrOfElevators), Integer.parseInt(floors));

		while (true) {
			if (model.Connect(ip, Integer.parseInt(port))) {
				break;
			} else {
				System.out.println("Could not connect...");
				System.out.println("Retrying in 5 seconds");
				Thread.sleep(5000);
			}
		}

		String input = model.getInput();
		while (true) {
			if (!input.startsWith("f")) {
				System.out.println("Recieved: " + input);
			}
			if (input.equals("Elevator server down.")) {
				System.out.println("Shutting down controller...");
				System.exit(1);
			}
			input = model.getInput();
		}
	}
}
