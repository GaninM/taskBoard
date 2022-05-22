package com.example.taskboard.repository;

import com.example.taskboard.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Modifying
    @Query(value = "select * from Task t where t.type like concat ('%', :type, '%')", nativeQuery = true)
    List<Task> findByType(@Param("type") String type);
}
