package org.example.campusshelf.repository;

import org.example.campusshelf.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByTitleContainingIgnoreCase(String title);

    List<Material> findByDepartment(String department);

    List<Material> findBySemester(String semester);

    List<Material> findByTitleContainingIgnoreCaseAndDepartment(
            String title,
            String department
    );

    List<Material> findByTitleContainingIgnoreCaseAndSemester(
            String title,
            String semester
    );

    List<Material> findByDepartmentAndSemester(
            String department,
            String semester
    );

    List<Material> findByTitleContainingIgnoreCaseAndDepartmentAndSemester(
            String title,
            String department,
            String semester
    );

    long countByStudent_Id(Long studentId);
}
