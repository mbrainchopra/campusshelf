package org.example.campusshelf.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String subject;

    private String department;

    private String semester;

    private String type;

    @Column(length = 1000)
    private String description;

    private String fileName;

    private String originalFileName;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
}
