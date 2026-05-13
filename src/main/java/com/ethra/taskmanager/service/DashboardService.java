package com.ethra.taskmanager.service;

import com.ethra.taskmanager.dto.response.DashboardStatsResponse;
import com.ethra.taskmanager.dto.response.TaskResponse;
import com.ethra.taskmanager.entity.User;
import com.ethra.taskmanager.enums.TaskStatus;
import com.ethra.taskmanager.repository.ProjectRepository;
import com.ethra.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public DashboardService(TaskRepository taskRepository,
                            ProjectRepository projectRepository,
                            UserService userService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        User currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();

        long todoCount = taskRepository.countByAssignedToIdAndStatus(userId, TaskStatus.TODO);
        long inProgressCount = taskRepository.countByAssignedToIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        long doneCount = taskRepository.countByAssignedToIdAndStatus(userId, TaskStatus.DONE);
        long totalTasks = todoCount + inProgressCount + doneCount;

        long overdueCount = taskRepository
                .findByAssignedToIdAndStatusNotAndDueDateBefore(
                        userId, TaskStatus.DONE, LocalDate.now())
                .size();

        long totalProjects = projectRepository.findProjectsByUserId(userId).size();

        return DashboardStatsResponse.builder()
                .totalTasks(totalTasks)
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                .overdueCount(overdueCount)
                .totalProjects(totalProjects)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        User currentUser = userService.getCurrentUser();

        return taskRepository.findByAssignedToId(currentUser.getId()).stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
