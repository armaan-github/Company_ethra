package com.ethra.taskmanager.dto.response;

import com.ethra.taskmanager.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private UserResponse createdBy;
    private int memberCount;
    private int taskCount;
    private LocalDateTime createdAt;

    public static ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdBy(UserResponse.fromEntity(project.getCreatedBy()))
                .memberCount(project.getMembers() != null ? project.getMembers().size() : 0)
                .taskCount(project.getTasks() != null ? project.getTasks().size() : 0)
                .createdAt(project.getCreatedAt())
                .build();
    }
}
