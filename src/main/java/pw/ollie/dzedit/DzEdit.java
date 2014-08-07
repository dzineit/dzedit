package pw.ollie.dzedit;

import java.io.Console;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static pw.ollie.dzedit.Utilities.*;

/**
 * An open source Java text editor, with the aim of being very simple
 * 
 * DzEdit is operable from the command line and by some GUI for loading and
 * saving files. DzEdit is a work in progress.
 * 
 * @author DziNeIT
 * @see {@link https://github.com/DziNeIT/dzedit}
 */
public final class DzEdit {
    /**
     * The DzEdit thread pool - each window has its own thread in the thread
     * pool
     */
    public static ExecutorService threads = Executors.newCachedThreadPool();
    /**
     * The amount of windows currently open. Incremented for each construction
     * of a Window object and decremented when a window is closed. If it is 0
     * after a window has been closed (i.e there are no windows left open), the
     * application terminates
     */
    public static int curAmount = 0;

    /**
     * The Window object, which is a JFrame
     */
    private final Window window;

    // Input
    private Console console;
    private Scanner scanner;

    /**
     * The path to the file which is currently being edited
     */
    private Path path;
    /**
     * The text content of the file the last time it was saved
     */
    private String last;

    /**
     * Main constructor for DzEdit. Creates the Window object, which in turn
     * creates all of the components and changes their settings when it is
     * created
     * 
     * After this, the constructor runs listenForCommands(), which repeats until
     * the user closes the program
     */
    public DzEdit() {
        window = new Window(this);

        // Initialise input
        console = System.console();
        scanner = null;
        if (console == null) {
            scanner = new Scanner(System.in);
        }
    }

    public void run() {
        // Listen on a loop
        startListening();

        // Cleanup afterwards
        if (scanner != null) {
            scanner.close();
        }
        System.exit(0);
    }

    /**
     * Opens the specified File to the editor
     * 
     * @param file
     *            The File to open
     */
    public void open(final Path path) {
        window.getTextArea().setText(read(this.path = path));
        // Put the path to the file in the title of the window
        window.setTitle(Window.BASE_WINDOW_NAME + " - " + path.toString());
        last = window.getTextArea().getText();
    }

    /**
     * Saves the contents of the current File to the given destination File
     * 
     * @param destination
     *            The File to write the contents to
     */
    public void saveAs(final Path destination) {
        if (destination == null) {
            // Prevents NPE when user hits 'Save' without having a file open
            return;
        }

        if (!writeFile(destination, window.getTextArea().getText())) {
            System.err.println("ERROR: COULD NOT SAVE FILE");
        } else {
            System.out.println("Saved contents to file: " + destination.toString());
            open(destination);
        }
        last = window.getTextArea().getText();
    }

    /**
     * Saves current file
     */
    public void save() {
        if (!window.newFile()) {
            saveAs(path);
        }
    }

    /**
     * Listens for a commands, returning true if the application should continue
     * to listen or false if the application should terminate
     * 
     * @return Whether the application should continue running
     */
    private boolean listen() {
        final String line = getInput();
        if (line.equalsIgnoreCase("close") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
            return false;
        } else if (line.equalsIgnoreCase("save")) {
            save();
        } else if (line.startsWith("saveas")) {
            final String[] split = line.split(" ");
            String first = null;
            try {
                first = split[1];
            } catch (final ArrayIndexOutOfBoundsException e) {
                System.out.println("Must specify file after command 'saveas'");
                return true;
            }

            final List<String> list = new ArrayList<>(Arrays.asList(split));
            list.remove(0);
            saveAs(Paths.get(listToString(list, " ")));
        } else if (line.startsWith("open")) {
            final String[] split = line.split(" ");
            String first = null;
            try {
                first = split[1];
            } catch (final ArrayIndexOutOfBoundsException e) {
                System.out.println("Must specify filename after command 'open'");
                return true;
            }

            final List<String> list = new ArrayList<>(Arrays.asList(split));
            list.remove(0);
            open(Paths.get(listToString(list, " ")));
        } else {
            System.out.println("That doesn't make sense!");
        }

        return true;
    }

    /**
     * Listens for commands on a loop
     * 
     * @param scanner
     *            The Scanner to listen to
     */
    private void startListening() {
        for (boolean b = true; b; b = listen()) {
        }
    }

    /**
     * Gets the text content at the last time the file was saved
     * 
     * @return Text content of the file the last time we saved
     */
    String getLast() {
        return last;
    }

    /**
     * Gets an input string, using the Console object if it was available at
     * launch, or the Scanner object if it wasn't
     * 
     * @return An input string from the user
     */
    private String getInput() {
        if (console == null) {
            return scanner.nextLine();
        } else {
            return console.readLine();
        }
    }

    /**
     * Main method for DzEdit. Just calls the constructor
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
        // Run DzEdit in a thread in the thread pool
        threads.submit(new Runnable() {
            @Override
            public void run() {
                new DzEdit().run();
            }
        });
    }
}