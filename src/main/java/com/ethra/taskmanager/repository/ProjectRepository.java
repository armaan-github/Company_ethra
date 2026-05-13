package com.ethra.taskmanager.repository;

import com.ethra.taskmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Projects created by a specific user */
    List<Project> findByCreatedById(Long userId);

    /** All projects where a user is either the creator or a member */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m " +
           "WHERE p.createdBy.id = :userId OR m.user.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);
}
