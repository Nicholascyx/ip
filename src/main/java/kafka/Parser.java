package kafka;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;

/**
 * This class parses user input commands and executes corresponding actions within the Kafka application.
 * It interacts with TaskList, Storage, and Ui objects to manage tasks, persist data, and provide user feedback.
 */
public class Parser {

    /**
     * Parses the given user input and executes the appropriate command.
     *
     * @param userInput The user input string.
     * @param taskList The TaskList object to manage tasks.
     * @param storage The Storage object for data persistence.
     * @param ui The Ui object for user interface interactions.
     * @throws KafkaException If an error occurs during command parsing or execution.
     * @throws IOException If an error occurs during file operations.
     * @throws DateTimeParseException If an error occurs while parsing date and time strings.
     */
    public static String parseCommand(String userInput, TaskList taskList, Storage storage, Ui ui) throws KafkaException, IOException, DateTimeParseException {
        String[] splitInput = userInput.trim().split(" ", 2);

        if (splitInput[0] == null) {
            return "";
        }

        String command = splitInput[0].toLowerCase();
        String arguments = splitInput.length > 1 ? splitInput[1] : "";
        String output = "";

        switch (command) {
        case "bye":
            output = ui.goodbye();
            break;
        case "list":
            output = ui.getList() + "\n" + taskList.printList();
            break;
        case "mark":
            int taskNumberMark = Integer.parseInt(arguments);
            Task taskToMark = taskList.tasks.get(taskNumberMark - 1);
            taskList.mark(taskToMark);
            storage.writeToFile(taskList.tasks);
            output = ui.mark(taskToMark);
            break;
        case "unmark":
            int taskNumberUnmark = Integer.parseInt(arguments);
            Task taskToUnmark = taskList.tasks.get(taskNumberUnmark - 1);
            taskList.unmark(taskToUnmark);
            storage.writeToFile(taskList.tasks);
            output = ui.unmark(taskToUnmark);
            break;
        case "delete":
            if (taskList.tasks.isEmpty()) {
                return "";
            }
            int taskNumberDelete = Integer.parseInt(arguments);
            Task taskToDelete = taskList.tasks.get(taskNumberDelete - 1);
            taskList.delete(taskNumberDelete);
            storage.writeToFile(taskList.tasks);
            output = ui.delete(taskToDelete, taskList);
            break;
        case "find":
            if (arguments.isEmpty()) {
                throw new KafkaException("It seems you've left the details blank. Even the simplest tasks need some direction, don't you think?");
            }
            TaskList temp = taskList.find(arguments.toLowerCase());
            if (temp.isEmpty()) {
                throw new KafkaException("Hmm, it seems that no task aligns with that word... mind trying again?");
            }
            output = ui.find() + "\n" + temp.printList();
            break;
        case "todo":
            if (arguments.isEmpty()) {
                throw new KafkaException("It seems you've left the details blank. Even the simplest tasks need some direction, don't you think?");
            }
            Task todo = new Todo(arguments, false);
            taskList.addTask(todo);
            storage.writeToFile(taskList.tasks);
            output = ui.addTask(todo, taskList);
            break;
        case "deadline":
            if (arguments.isEmpty()) {
                throw new KafkaException("It seems you've left the details blank. Even the simplest tasks need some direction, don't you think?");
            }
            String[] deadlineParts = arguments.split("/by ");
            if (deadlineParts.length < 2) {
                throw new KafkaException("It appears the details for this deadline task are off. Let's give it another go, shall we?");
            }
            LocalDateTime by = LocalDateTimeConverter.getLocalDateTime(deadlineParts[1]);
            Task deadline = new Deadline(deadlineParts[0], by, false);
            taskList.addTask(deadline);
            storage.writeToFile(taskList.tasks);
            output = ui.addTask(deadline, taskList);
            break;
        case "event":
            if (arguments.isEmpty()) {
                throw new KafkaException("It seems you've left the details blank. Even the simplest tasks need some direction, don't you think?");
            }
            String[] eventParts = arguments.split("/from | /to ");
            if (eventParts.length < 3) {
                throw new KafkaException("It appears the details for this event task are off. Let's give it another go, shall we?");
            }
            LocalDateTime from = LocalDateTimeConverter.getLocalDateTime(eventParts[1]);
            LocalDateTime to = LocalDateTimeConverter.getLocalDateTime(eventParts[2]);
            Task event = new Event(eventParts[0], from, to, false);
            taskList.addTask(event);
            storage.writeToFile(taskList.tasks);
            output = ui.addTask(event, taskList);
            break;
        default:
            throw new KafkaException("Hmm... I'm not sure what you're getting at. Care to enlighten me?");
        }
        return output;
    }
}
