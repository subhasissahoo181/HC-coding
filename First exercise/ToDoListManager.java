import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class ToDoListManager {

    // Task class with Builder Pattern
    public static class Task {
        private final String description;
        private final Optional<String> dueDate;
        private boolean isCompleted;

        private Task(TaskBuilder builder) {
            this.description = builder.description;
            this.dueDate = Optional.ofNullable(builder.dueDate);
            this.isCompleted = false; // Default to not completed
        }

        public String getDescription() {
            return description;
        }

        public Optional<String> getDueDate() {
            return dueDate;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public void markCompleted() {
            this.isCompleted = true;
        }

        public static class TaskBuilder {
            private final String description;
            private String dueDate;

            public TaskBuilder(String description) {
                this.description = description;
            }

            public TaskBuilder dueDate(String dueDate) {
                this.dueDate = dueDate;
                return this;
            }

            public Task build() {
                return new Task(this);
            }
        }

        @Override
        public String toString() {
            return description + (isCompleted ? " - Completed" : " - Pending") +
                    (dueDate.isPresent() ? ", Due: " + dueDate.get() : "");
        }
    }

    // Memento class for undo/redo functionality
    private static class Memento {
        private final List<Task> state;

        private Memento(List<Task> state) {
            this.state = state;
        }

        private List<Task> getState() {
            return state;
        }
    }

    // ToDoListManager class methods and members
    private List<Task> tasks = new ArrayList<>();
    private Stack<Memento> undoStack = new Stack<>();
    private Stack<Memento> redoStack = new Stack<>();

    public void addTask(Task task) {
        saveState();
        tasks.add(task);
        redoStack.clear(); // Clear redo stack after a new action
    }

    public void markTaskCompleted(String description) {
        Task task = findTask(description);
        if (task != null) {
            saveState();
            task.markCompleted();
            redoStack.clear();
        }
    }

    public void deleteTask(String description) {
        Task task = findTask(description);
        if (task != null) {
            saveState();
            tasks.remove(task);
            redoStack.clear();
        }
    }

    public List<String> viewTasks(String filter) {
        return tasks.stream()
                .filter(task -> filter.equals("Show all") ||
                        (filter.equals("Show completed") && task.isCompleted()) ||
                        (filter.equals("Show pending") && !task.isCompleted()))
                .map(Task::toString)
                .collect(Collectors.toList());
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(saveToMemento());
            restoreFromMemento(undoStack.pop());
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(saveToMemento());
            restoreFromMemento(redoStack.pop());
        }
    }

    private Task findTask(String description) {
        return tasks.stream().filter(task -> task.getDescription().equals(description)).findFirst().orElse(null);
    }

    private void saveState() {
        undoStack.push(saveToMemento());
    }

    private Memento saveToMemento() {
        return new Memento(new ArrayList<>(tasks));
    }

    private void restoreFromMemento(Memento memento) {
        tasks = memento.getState();
    }

    public static void main(String[] args) {
        ToDoListManager manager = new ToDoListManager();

        // Add tasks
        Task task1 = new Task.TaskBuilder("Buy groceries").dueDate("2023-09-20").build();
        Task task2 = new Task.TaskBuilder("Submit assignment").build();
        manager.addTask(task1);
        manager.addTask(task2);

        // View tasks
        System.out.println("All Tasks:");
        manager.viewTasks("Show all").forEach(System.out::println);

        // Mark task as completed
        manager.markTaskCompleted("Buy groceries");

        // View tasks after marking as completed
        System.out.println("\nTasks after marking 'Buy groceries' as completed:");
        manager.viewTasks("Show all").forEach(System.out::println);

        // Undo action
        manager.undo();
        System.out.println("\nTasks after undo:");
        manager.viewTasks("Show all").forEach(System.out::println);

        // Redo action
        manager.redo();
        System.out.println("\nTasks after redo:");
        manager.viewTasks("Show all").forEach(System.out::println);
    }
}
