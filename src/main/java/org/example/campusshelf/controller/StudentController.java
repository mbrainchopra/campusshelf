package org.example.campusshelf.controller;

import org.example.campusshelf.entity.Student;
import org.example.campusshelf.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("student", new Student());
        return "student/register";
    }

    @PostMapping("/register")
    public String registerStudent(
            @ModelAttribute("student") Student student,
            Model model) {

        String email = student.getEmail() == null
                ? ""
                : student.getEmail().trim();

        if (email.isBlank()) {
            model.addAttribute("error", "Email is required.");
            return "student/register";
        }

        if (studentRepository.existsByEmailIgnoreCase(email)) {
            model.addAttribute("error", "An account already exists with this email.");
            return "student/register";
        }

        student.setName(student.getName() == null ? "" : student.getName().trim());
        student.setEmail(email);
        student.setPassword(student.getPassword() == null ? "" : student.getPassword().trim());

        studentRepository.save(student);

        model.addAttribute("student", new Student());
        model.addAttribute("message", "Registration successful! You can now login.");

        return "student/register";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "student/login";
    }

    @PostMapping("/login")
    public String loginStudent(
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        String cleanEmail = email == null ? "" : email.trim();

        return studentRepository
                .findByEmailAndPassword(cleanEmail, password)
                .map(student ->
                        "redirect:/students/dashboard?studentId=" + student.getId()
                )
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid email or password.");
                    return "student/login";
                });
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam Long studentId,
            Model model) {

        Student student = studentRepository
                .findById(studentId)
                .orElse(null);

        if (student == null) {
            return "redirect:/students/login";
        }

        model.addAttribute("student", student);

        return "student/dashboard";
    }
}
