package com.example.taskManagementApplication.service;

import com.example.taskManagementApplication.entity.Status;
import com.example.taskManagementApplication.entity.Task;
import com.example.taskManagementApplication.exception.ExclusionTaskProcessing;
import com.example.taskManagementApplication.repository.StatusRepository;
import com.example.taskManagementApplication.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class TaskService {

    private final StatusRepository statusRepository;
    private final TaskRepository taskRepository;

    /*
    * Сохранение задачи в БД
    * При сохранении автоамтически добавляется дата регистрации задачи
    * И устанавливается статус "Назначена"
    * #line-break# - позволяет отформатировать описание задачи и в
    * дальнейшем при отображении восстановить все переносы строк
    * */
    public void saveTask(Task task) {
        task.setDescription(
                task.getDescription()
                        .replaceAll("\n", "#line-break#"));
        task.setDateOfRegistration(new Date());
        task.setStatus(statusRepository.findByNameStatus("Назначена"));

        taskRepository.save(task);
    }

    // Получение списка всех задач
    public List<Task> findAllTask() {
        return taskRepository.findAll();
    }

    // Получение списка всех подзадач, указанной задачи
    public List<Task> findAllBylinkTaskId(Long idTask) {
        return taskRepository.findAllBylinkTaskId(idTask);
    }

    // Получение задачи
    public Task findById(Long idTask) {

        Task task = taskRepository.findById(idTask).get();

        // восстановление переносов строк
        task.setDescription(
                task.getDescription()
                        .replaceAll("#line-break#", "\n"));
        return task;
    }

    // Сервис сохранения нового статуса задачи
    public void updateStatus(Long idTask, Task task) throws ExclusionTaskProcessing {

        Task initialTask = taskRepository.findById(idTask).get();

        // Выполняется если планируется установить статус "Завершена"
        if(task.getStatus().getNameStatus().equals("Завершена")) {

            // Проверка задачи на возможность изменения статуса на "Завершена"
            if(initialTask.getStatus().getNameStatus().equals("Назначена")) {
                throw new ExclusionTaskProcessing("Задача может быть переведена в статус \"Завершена\" только из статуса \"Выполняется\" или \"Приостановлена\"!");
            }

            // Проверка подзадач на возможность изменения статуса на "Завершена"
            checkingStatusSubtasks(initialTask);
            // изменение статуса подзадач на "Завершена"
            Status status = statusRepository.findByNameStatus("Завершена");
            changeStatusSubtasks(initialTask, status);
            // Сохранение даты и времени завершения задачи и фактического времени выполнения задачи
            initialTask.setDateOfCompletion(new Date());
            Long actualExecutionTime = initialTask.getDateOfCompletion().getTime() - initialTask.getDateOfRegistration().getTime();
            initialTask.setActualExecutionTime(timeFormatting(actualExecutionTime));
            // Изменение даты завершения подзадач
            changingCompletionDateSubtasks(initialTask);
        }

        initialTask.setStatus(task.getStatus());

        taskRepository.save(initialTask);
    }

    // Проверка подзадач на возможность изменения статуса на "Завершена"
    private void checkingStatusSubtasks(Task task) throws ExclusionTaskProcessing {
        List<Task> subtasks = taskRepository.findAllBylinkTaskId(task.getId());
        for (Task subtask: subtasks) {
            if(subtask.getStatus().getNameStatus().equals("Назначена")) {
                throw new ExclusionTaskProcessing("Задача не может быть переведена в статус \"Завершена\", так как одна или несколько подзадач находятся в статусе \"Назначена\"!");
            }
            // Проверка подзадач текущей подзадачи
            checkingStatusSubtasks(subtask);
        }
    }

    // изменение статуса подзадач
    private void changeStatusSubtasks(Task task, Status status) {
        List<Task> subtasks = taskRepository.findAllBylinkTaskId(task.getId());
        for (Task subtask: subtasks) {
            subtask.setStatus(status);
            taskRepository.save(subtask);
            // изменение статуса подзадач текущей подзадачи
            changeStatusSubtasks(subtask, status);
        }
    }

    // Изменение даты завершения подзадач
    private void changingCompletionDateSubtasks(Task task) {
        List<Task> subtasks = taskRepository.findAllBylinkTaskId(task.getId());
        for (Task subtask: subtasks) {
            subtask.setDateOfCompletion(new Date());
            Long actualExecutionTime = subtask.getDateOfCompletion().getTime() - subtask.getDateOfRegistration().getTime();
            subtask.setActualExecutionTime(String.valueOf(timeFormatting(actualExecutionTime)));
            taskRepository.save(subtask);
            // Изменение даты завершения подзадач текущей подзадачи
            changingCompletionDateSubtasks(subtask);
        }
    }

    // Сервис сохранения изменений задачи
    public void updateTask(Long idTask, Task task) throws ExclusionTaskProcessing{

        Task initialTask = taskRepository.findById(idTask).get();

        if(initialTask.getStatus().getNameStatus().equals("Назначена")) {
            throw new ExclusionTaskProcessing("Задача может быть переведена в статус \"Завершена\" только из статуса \"Выполняется\" или \"Приостановлена\"!");
        }

        // Проверка подзадач на возможность изменения статуса на "Завершена"
        checkingStatusSubtasks(initialTask);
        // изменение статуса подзадач на "Завершена"
        Status status = statusRepository.findByNameStatus("Завершена");
        changeStatusSubtasks(initialTask, status);
        // Сохранение даты и времени завершения задачи и фактического времени выполнения задачи
        initialTask.setDateOfCompletion(new Date());
        Long actualExecutionTime = initialTask.getDateOfCompletion().getTime() - initialTask.getDateOfRegistration().getTime();
        initialTask.setActualExecutionTime(timeFormatting(actualExecutionTime));
        // Изменение даты завершения подзадач
        changingCompletionDateSubtasks(initialTask);

        task.setId(idTask);
        task.setDateOfRegistration(initialTask.getDateOfRegistration());
        task.setDateOfCompletion(initialTask.getDateOfCompletion());

        taskRepository.save(task);
    }

    // Удаление задачи
    public void deleteTask(Long idTask) throws ExclusionTaskProcessing {

        Task task = taskRepository.findById(idTask).get();

        // Проверка на то, что задачая является завершённой перед удалением
        if(!task.getStatus().getNameStatus().equals("Завершена")) {
            throw new ExclusionTaskProcessing("Задача не может быть удалена, так как её статус не является: \"Завершена\"!");
        }

        // Перенос подзадач на уровень выше
        List<Task> taskList = taskRepository.findAllBylinkTaskId(idTask);
        for (Task subtask: taskList) {
            subtask.setLinkTask(task.getLinkTask());
        }

        taskRepository.deleteById(idTask);
    }

    // Форматирование времени
    public String timeFormatting(long actualExecutionTime) {
        String timeFormatting = "";
        Long remainder;

        timeFormatting += actualExecutionTime/(1000*60*60*24);
        timeFormatting += " д. ";

        actualExecutionTime = actualExecutionTime % (1000*60*60*24);
        remainder = actualExecutionTime/(1000*60*60);
        if(remainder/10 == 0) {
            timeFormatting += "0" + remainder + ":";
        } else {
            timeFormatting += remainder + ":";
        }

        actualExecutionTime = actualExecutionTime % (1000*60*60);
        remainder = actualExecutionTime/(1000*60);
        if(remainder/10 == 0) {
            timeFormatting += "0" + remainder;
        } else {
            timeFormatting += remainder;
        }

        return timeFormatting;
    }
}
