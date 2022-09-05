package com.example.taskManagementApplication.repository;

import com.example.taskManagementApplication.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByNameStatus(String назначена);
}
