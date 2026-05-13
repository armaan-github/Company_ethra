package com.ethra.taskmanager.repository;

import com.ethra.taskmanager.entity.Task;
import com.ethra.taskmanager.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedToId(Long userId);

    long countByAssignedToIdAndStatus(Long userId, TaskStatus status);

    /** Overdue tasks: status is not DONE and due date has passed */
    List<Task> findByAssignedToIdAndStatusNotAndDueDateBefore(
            Long userId, TaskStatus excludeStatus, LocalDate date);

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
}
