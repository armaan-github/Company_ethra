package com.ethra.taskmanager.service;

import com.ethra.taskmanager.dto.request.StatusUpdateRequest;
import com.ethra.taskmanager.dto.request.TaskRequest;
import com.ethra.taskmanager.dto.response.TaskResponse;
import com.ethra.taskmanager.entity.Project;
import com.ethra.taskmanager.entity.Task;
import com.ethra.taskmanager.entity.User;
import com.ethra.taskmanager.enums.Priority;
import com.ethra.taskmanager.enums.Role;
import com.ethra.taskmanager.exception.AccessDeniedException;
import com.ethra.taskmanager.exception.ResourceNotFoundException;
import com.ethra.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository,
                       ProjectService projectService,
                       UserService userService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request) {
        User currentUser = userService.getCurrentUser();
        Project project = projectService.findProjectOrThrow(projectId);
        projectService.verifyProjectAccess(project, currentUser);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .project(project)
                .createdBy(currentUser)
                .build();

        // Assign to a specific user if provided
        if (request.getAssignedToId() != null) {
            User assignee = userService.getUserById(request.getAssignedToId());
            task.setAssignedTo(assignee);
        }

        task = taskRepository.save(task);
        return TaskResponse.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId) {
        User currentUser = userService.getCurrentUser();
        Project project = projectService.findProjectOrThrow(projectId);
        projectService.verifyProjectAccess(project, currentUser);

        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = findTaskOrThrow(taskId);

        User currentUser = userService.getCurrentUser();
        projectService.verifyProjectAccess(task.getProject(), currentUser);

        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        User currentUser = userService.getCurrentUser();
        Task task = findTaskOrThrow(taskId);
        projectService.verifyProjectAccess(task.getProject(), currentUser);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getAssignedToId() != null) {
            User assignee = userService.getUserById(request.getAssignedToId());
            task.setAssignedTo(assignee);
        }

        task = taskRepository.save(task);
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, StatusUpdateRequest request) {
        User currentUser = userService.getCurrentUser();
        Task task = findTaskOrThrow(taskId);

        // Only assigned user or admin can change status
        boolean isAssigned = task.getAssignedTo() != null
                && task.getAssignedTo().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isAssigned && !isAdmin) {
            throw new AccessDeniedException(
                    "Only the assigned user or an admin can update task status");
        }

        task.setStatus(request.getStatus());
        task = taskRepository.save(task);
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can delete tasks");
        }

        Task task = findTaskOrThrow(taskId);
        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
    }
}
