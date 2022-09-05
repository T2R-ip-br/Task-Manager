package com.example.taskManagementApplication;

import com.example.taskManagementApplication.entity.Status;
import com.example.taskManagementApplication.repository.StatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@SpringBootApplication
public class TaskManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskManagementApplication.class, args);
	}

	@Bean
	public CommandLineRunner CommandLineRunnerBean(StatusRepository statusRepository) {
		return (args) -> {
			Status statusAssigned = new Status("Назначена");
			Status statusInProgress = new Status("Выполняется");
			Status statusSuspended = new Status("Приостановлена");
			Status StatusCompleted = new Status("Завершена");

			statusRepository.saveAll(
					List.of(
							statusAssigned,
							statusInProgress,
							statusSuspended,
							StatusCompleted
					));
		};
	}

	// TODO: Найти работу =)

	// Выполнено:
		// Реализовать редактирование задач
		// Реализовать удаление задач
		// Реализовать инициализацию даты завершения задачи
		// Реализовать отображение задач в виде дерева (смотри: TaskController: 67 строчка)
		// Сделать имя задачи ссылкой для открытия описания задачи
		// На странице отображения инф. о задаче добавить кнопку добавления подзадачи
		// На странице отображения инф. о задаче добавить кнопку удаления задачи
		// Реализовать подсчёт трудоёмкости и затраченного времени
		// На странице отображения инф. о задаче слева должен быть список всех задач
		// Поработать над дизайном
		// Реализовать корректное отображение ошибок
		// Добавить логику проверки статуса задачи и подзадач при редактировании задачи
		// Отредактировать код (CSS, All-HTML, Entity-file, Repository-file, Controller-file, Service-file)
		// Продокументировать код (CSS, All-HTML, Entity-file, Repository-file, Controller-file, Service-file)

}
