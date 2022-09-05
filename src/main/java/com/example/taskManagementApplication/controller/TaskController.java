package com.example.taskManagementApplication.controller;

import com.example.taskManagementApplication.entity.Status;
import com.example.taskManagementApplication.exception.ExclusionTaskProcessing;
import com.example.taskManagementApplication.repository.StatusRepository;
import com.example.taskManagementApplication.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.taskManagementApplication.entity.Task;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(path = "/api/v1/")
@AllArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final StatusRepository statusRepository;

    // Страница добавления задачи
    @GetMapping("/task")
    public String addTask(Model model) {
        model.addAttribute("task", new Task());
        return "add-task";
    }

    // Страница добавления подзадачи
    @GetMapping("/subtask/{idTask}")
    public String addSubtask(
            @PathVariable("idTask") Long idTask,
            Model model
    ) {
        Task task = new Task();
        task.setLinkTask(taskService.findById(idTask));
        model.addAttribute("task", task);
        return "add-task";
    }

    // Сохранение добавляемой задачи или подзадачи
    @PostMapping("/task")
    public ModelAndView addTask( @ModelAttribute Task task ) {
        taskService.saveTask(task);
        return new ModelAndView("redirect:/api/v1/tasks");
    }

    // Страница отображения всех задач
    @GetMapping("/tasks")
    public String showTasks(Model model) {
        List<Task> tasks = taskService.findAllTask();
        model.addAttribute("tasks", tasks);
        model.addAttribute("taskTree", generatingTaskTree(taskService.findAllBylinkTaskId(null), ""));
        return "show-tasks";
    }

    // Страница отображения информации о задаче
    @GetMapping("/task/{idTask}")
    public String showInfoTask(
            @PathVariable("idTask") Long idTask,
            Model model
    ) {
        Task task = taskService.findById(idTask);
        List<Task> tasks = taskService.findAllTask();
        List<Task> subtasks = taskService.findAllBylinkTaskId(idTask);
        List<Status> statuses = statusRepository.findAll();
        model.addAttribute("task", task);
        model.addAttribute("tasks", tasks);
        model.addAttribute("statuses", statuses);
        model.addAttribute("subtasks", subtasks);
        model.addAttribute("taskTree", shortGeneratingTaskTree(taskService.findAllBylinkTaskId(null), ""));
        return "showInfoAboutTask";
    }

    // Обновления статуса задачи
    @PostMapping("/task/update-status/{idTask}")
    public ModelAndView updateStatusTask(
            @PathVariable("idTask") Long idTask,
            @ModelAttribute Task task
    ) throws UnsupportedEncodingException {

        try {
            taskService.updateStatus(idTask, task);

        } catch (ExclusionTaskProcessing exclusionTaskProcessing) {

            String url = "redirect:/api/v1/task/{idTask}?error=";
            url += URLEncoder.encode(String.valueOf(exclusionTaskProcessing), "UTF-8");

            return new ModelAndView(url);
        }
        return new ModelAndView("redirect:/api/v1/task/" + idTask);
    }

    // Страница изменения задачи
    @GetMapping("/task/update/{idTask}")
    public String updateTask(
            @PathVariable("idTask") Long idTask,
            Model model
    ) {
        List<Status> statuses = statusRepository.findAll();
        Task task = taskService.findById(idTask);
        model.addAttribute("task", task);
        model.addAttribute("statuses", statuses);
        return "update-task";
    }

    // Обновления задачи
    @PostMapping("/task/update/{idTask}")
    public ModelAndView updateTask(
            @PathVariable("idTask") Long idTask,
            @ModelAttribute Task task
    ) throws UnsupportedEncodingException {

        try {
            taskService.updateTask(idTask, task);

        } catch (ExclusionTaskProcessing exclusionTaskProcessing) {

            String url = "redirect:/api/v1/task/update/{idTask}?error=";
            url += URLEncoder.encode(String.valueOf(exclusionTaskProcessing), "UTF-8");

            return new ModelAndView(url);
        }
        return new ModelAndView("redirect:/api/v1/task/" + idTask);
    }

    // Страница удаления задачи
    @GetMapping("/task/delete/{idTask}*")
    public String deleteTask(
            @PathVariable("idTask") Long idTask,
            Model model
    ) {
        Task task = taskService.findById(idTask);
        model.addAttribute("task", task);
        return "delete-task";
    }

    // Удаление задачи
    @PostMapping("/task/delete/{idTask}")
    public ModelAndView deleteTask( @PathVariable("idTask") Long idTask ) throws ExclusionTaskProcessing, UnsupportedEncodingException {

        try {
            taskService.deleteTask(idTask);

        } catch (ExclusionTaskProcessing exclusionTaskProcessing) {

            String url = "redirect:/api/v1/task/delete/{idTask}?error=";
            url += URLEncoder.encode(String.valueOf(exclusionTaskProcessing), "UTF-8");

            return new ModelAndView(url);
        }
        return new ModelAndView("redirect:/api/v1/tasks");
    }

    // Генерация дерева задач из тегов html
    private String generatingTaskTree(List<Task> tasks, String htmlCode) {

        String ul;
        if(htmlCode.isEmpty()) {
            ul = "<ul class=\"treeCSS\">";
        } else {
            ul = "<ul>";
        }
        String ulEnd = "</ul>";
        String li = "<li";
        String liEnd = "</li>";
        Long dateOfCompletion;

        if(!tasks.isEmpty()) {
            htmlCode += ul;
            for (Task task : tasks) {
                // Получение времени выполнения задачи в мс если задача уже завершена
                if(task.getDateOfCompletion() != null) {
                    dateOfCompletion = task.getDateOfCompletion().getTime() - task.getDateOfRegistration().getTime();
                } else { // получение времени выполнения задачи с момента регистрации если задача ещё не завершена
                    Date actualTime = new Date();
                    dateOfCompletion = actualTime.getTime() - task.getDateOfRegistration().getTime();
                }
                htmlCode += li +
                        " style=\"cursor:pointer;\"> <a href=\"/api/v1/task/" + task.getId() + "\">" +
                        task.getNameTask() +
                        // отображение Плановой трудоёмкости и суммы трудоёмкости в месте с подзадачами в скобках
                        ", Плановая трудоёмкость задачи: " + task.getPlannedComplexity() + " (" + sumComplexitySubtasks(task, 0) + ")" +
                        ", Время выполнения задачи: " + taskService.timeFormatting(dateOfCompletion) +
                        ", Статус задачи: [" + task.getStatus().getNameStatus() + "]</a>";
                htmlCode = generatingTaskTree(taskService.findAllBylinkTaskId(task.getId()), htmlCode);
                htmlCode += liEnd;
            }
            htmlCode += ulEnd;
        }
        return htmlCode;
    }

    // Генерация дерева задач из тегов html сокращённая версия
    private String shortGeneratingTaskTree(List<Task> tasks, String htmlCode) {

        String ul;
        if(htmlCode.isEmpty()) {
            ul = "<ul class=\"treeCSS\">";
        } else {
            ul = "<ul>";
        }
        String ulEnd = "</ul>";
        String li = "<li";
        String liEnd = "</li>";

        if(!tasks.isEmpty()) {
            htmlCode += ul;
            for (Task task : tasks) {
                htmlCode += li +
                        " style=\"cursor:pointer;\"> <a href=\"/api/v1/task/" + task.getId() + "\">" + task.getNameTask() + "</a>";
                htmlCode = shortGeneratingTaskTree(taskService.findAllBylinkTaskId(task.getId()), htmlCode);
                htmlCode += liEnd;
            }
            htmlCode += ulEnd;
        }
        return htmlCode;
    }

    // Расчёт суммы планнируемых трудозатрат
    private int sumComplexitySubtasks(Task task, int sum) {
        sum = task.getPlannedComplexity();
        List<Task> subtasks = taskService.findAllBylinkTaskId(task.getId());
        for (Task subtask: subtasks) {
            sum += sumComplexitySubtasks(subtask, sum);
        }
        return sum;
    }
}
