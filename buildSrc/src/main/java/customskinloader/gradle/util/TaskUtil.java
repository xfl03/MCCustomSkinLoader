package customskinloader.gradle.util;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class TaskUtil {
    public static void withTask(Project project, String taskName, Action<? super Task> action) {
        Task task = project.getTasks().findByName(taskName);
        if (task == null) {
            project.getTasks().whenTaskAdded(task0 -> {
                if (taskName.equals(task0.getName())) {
                    action.execute(task0);
                }
            });
        } else {
            action.execute(task);
        }
    }
}
