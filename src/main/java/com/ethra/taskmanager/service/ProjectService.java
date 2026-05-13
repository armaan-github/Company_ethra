package com.ethra.taskmanager.service;

import com.ethra.taskmanager.dto.request.ProjectRequest;
import com.ethra.taskmanager.dto.response.ProjectResponse;
import com.ethra.taskmanager.dto.response.UserResponse;
import com.ethra.taskmanager.entity.Project;
import com.ethra.taskmanager.entity.ProjectMember;
import com.ethra.taskmanager.entity.User;
import com.ethra.taskmanager.enums.Role;
import com.ethra.taskmanager.exception.AccessDeniedException;
import com.ethra.taskmanager.exception.DuplicateResourceException;
import com.ethra.taskmanager.exception.ResourceNotFoundException;
import com.ethra.taskmanager.repository.ProjectMemberRepository;
import com.ethra.taskmanager.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          UserService userService) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userService = userService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = userService.getCurrentUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        project = projectRepository.save(project);

        // Automatically add the creator as a project member
        ProjectMember creatorMember = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .build();
        memberRepository.save(creatorMember);

        return ProjectResponse.fromEntity(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects() {
        User currentUser = userService.getCurrentUser();

        // Admins can see all projects, members only see their own
        List<Project> projects;
        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findProjectsByUserId(currentUser.getId());
        }

        return projects.stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        User currentUser = userService.getCurrentUser();
        Project project = findProjectOrThrow(projectId);

        verifyProjectAccess(project, currentUser);
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        User currentUser = userService.getCurrentUser();
        Project project = findProjectOrThrow(projectId);

        verifyAdminAccess(currentUser);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project = projectRepository.save(project);

        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        User currentUser = userService.getCurrentUser();
        verifyAdminAccess(currentUser);

        Project project = findProjectOrThrow(projectId);
        projectRepository.delete(project);
    }

    @Transactional
    public UserResponse addMember(Long projectId, Long userId) {
        User currentUser = userService.getCurrentUser();
        verifyAdminAccess(currentUser);

        Project project = findProjectOrThrow(projectId);
        User newMember = userService.getUserById(userId);

        if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(newMember)
                .build();
        memberRepository.save(member);

        return UserResponse.fromEntity(newMember);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        User currentUser = userService.getCurrentUser();
        verifyAdminAccess(currentUser);

        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member not found in this project"));

        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getProjectMembers(Long projectId) {
        User currentUser = userService.getCurrentUser();
        Project project = findProjectOrThrow(projectId);
        verifyProjectAccess(project, currentUser);

        return memberRepository.findByProjectId(projectId).stream()
                .map(pm -> UserResponse.fromEntity(pm.getUser()))
                .collect(Collectors.toList());
    }

    // ---- Internal helpers ----

    public Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    private void verifyAdminAccess(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this action");
        }
    }

    public void verifyProjectAccess(Project project, User user) {
        if (user.getRole() == Role.ADMIN) return;

        boolean isMember = memberRepository.existsByProjectIdAndUserId(
                project.getId(), user.getId());
        if (!isMember) {
            throw new AccessDeniedException("You don't have access to this project");
        }
    }
}
