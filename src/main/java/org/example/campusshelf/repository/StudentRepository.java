package org.example.campusshelf.repository;

import org.example.campusshelf.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmailAndPassword(String email, String password);

    boolean existsByEmailIgnoreCase(String email);
}
