package org.example.campusshelf.repository;

import org.example.campusshelf.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByBookNameContainingIgnoreCase(String bookName);

    List<Book> findByDepartment(String department);

    List<Book> findBySemester(String semester);

    List<Book> findByBookNameContainingIgnoreCaseAndDepartment(
            String bookName,
            String department
    );

    List<Book> findByBookNameContainingIgnoreCaseAndSemester(
            String bookName,
            String semester
    );

    List<Book> findByDepartmentAndSemester(
            String department,
            String semester
    );

    List<Book> findByBookNameContainingIgnoreCaseAndDepartmentAndSemester(
            String bookName,
            String department,
            String semester
    );

    long countByStudent_Id(Long studentId);
}
