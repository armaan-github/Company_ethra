package com.ethra.taskmanager.dto.request;

import com.ethra.taskmanager.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 300, message = "Title must not exceed 300 characters")
    private String title;

    private String description;

    private Priority priority;

    private LocalDate dueDate;

    private Long assignedToId;
}
