package leti.practice;

public class Launcher {
    public static void main(String[] args) {
        boolean isConsoleLineInterface = false;

        for (String arg : args) {
            if (arg.equals("-cli")) {
                isConsoleLineInterface = true;
                break;
            }
        }

        if (isConsoleLineInterface) {
            ConsoleController consoleController = new ConsoleController();
            consoleController.run(args);
        } else {
            try {
                App.main(args);
            } catch (NoClassDefFoundError e) {
                System.out.println("If you want to call the CLI, add the '-cli' flag.");
            }
        }
    }

}
