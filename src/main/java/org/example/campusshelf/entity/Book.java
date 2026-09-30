package org.example.campusshelf.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookName;

    private String author;

    private String subject;

    private String department;

    private String semester;

    private Double price;

    private String bookCondition;

    @Column(length = 1000)
    private String description;

    private String image;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
}