package com.example.taskManagementApplication.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity(name = "task")
public class Task {

    @Id
    @SequenceGenerator(
            name = "task_sequence",
            sequenceName = "task_sequence",
            initialValue = 1,
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "task_sequence"
    )
    private Long id;

    private String nameTask;
    @Lob
    private String description;
    private String listOfPerformers;
    private Date dateOfRegistration;

    @ManyToOne
    @JoinColumn(
            name = "status_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "status_fk"
            )
    )
    private Status status;

    private Integer plannedComplexity;
    private String actualExecutionTime;
    private Date dateOfCompletion;

    @ManyToOne
    @JoinColumn(
            name = "link_task_id",
            nullable = true,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "link_task_fk"
            )
    )
    private Task linkTask;

    public Task(
            String nameTask,
            String description,
            String listOfPerformers,
            Integer plannedComplexity
    ) {
        this.nameTask = nameTask;
        this.description = description;
        this.listOfPerformers = listOfPerformers;
        this.plannedComplexity = plannedComplexity;
    }

    public Task(
            String nameTask,
            String description,
            String listOfPerformers,
            Integer plannedComplexity,
            Task task
    ) {
        this.nameTask = nameTask;
        this.description = description;
        this.listOfPerformers = listOfPerformers;
        this.plannedComplexity = plannedComplexity;
        this.linkTask = task;
    }

    public Task(
            String nameTask,
            String description,
            String listOfPerformers,
            Date dateOfRegistration,
            Status status,
            Integer plannedComplexity,
            String actualExecutionTime,
            Date dateOfCompletion
    ) {
        this.nameTask = nameTask;
        this.description = description;
        this.listOfPerformers = listOfPerformers;
        this.dateOfRegistration = dateOfRegistration;
        this.status = status;
        this.plannedComplexity = plannedComplexity;
        this.actualExecutionTime = actualExecutionTime;
        this.dateOfCompletion = dateOfCompletion;
    }

    @Override
    public String toString() {
        return "Task{" +
                "nameTask='" + nameTask + '\'' +
                ", description='" + description + '\'' +
                ", listOfPerformers='" + listOfPerformers + '\'' +
                ", plannedComplexity=" + plannedComplexity +
                ", status=" + status.getId() +
                '}';
    }
}
