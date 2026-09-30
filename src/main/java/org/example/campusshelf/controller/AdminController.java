package org.example.campusshelf.controller;

import org.example.campusshelf.entity.Material;
import org.example.campusshelf.entity.Book;
import org.example.campusshelf.entity.Student;
import org.example.campusshelf.repository.BookRepository;
import org.example.campusshelf.repository.MaterialRepository;
import org.example.campusshelf.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final StudentRepository studentRepository;
    private final BookRepository bookRepository;
    private final MaterialRepository materialRepository;

    private final Path materialUploadDirectory =
            Paths.get("uploads", "materials").toAbsolutePath().normalize();

    private final Path bookUploadDirectory =
            Paths.get("uploads", "books").toAbsolutePath().normalize();

    public AdminController(
            StudentRepository studentRepository,
            BookRepository bookRepository,
            MaterialRepository materialRepository) {

        this.studentRepository = studentRepository;
        this.bookRepository = bookRepository;
        this.materialRepository = materialRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if ("admin".equals(username) && "admin123".equals(password)) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error", "Invalid username or password.");
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("studentCount", studentRepository.count());
        model.addAttribute("bookCount", bookRepository.count());
        model.addAttribute("materialCount", materialRepository.count());

        return "admin/dashboard";
    }

    @GetMapping("/students")
    public String students(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String success,
            Model model) {

        model.addAttribute("students", studentRepository.findAll());

        if ("content".equals(error)) {
            model.addAttribute(
                    "error",
                    "This student cannot be deleted because books or study materials are linked to the account."
            );
        }

        if ("deleted".equals(success)) {
            model.addAttribute(
                    "success",
                    "Student deleted successfully."
            );
        }

        return "admin/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {

        Student student = studentRepository
                .findById(id)
                .orElse(null);

        if (student == null) {
            return "redirect:/admin/students?error=missing";
        }

        long bookCount = bookRepository.countByStudent_Id(id);
        long materialCount = materialRepository.countByStudent_Id(id);

        if (bookCount > 0 || materialCount > 0) {
            return "redirect:/admin/students?error=content";
        }

        studentRepository.delete(student);

        return "redirect:/admin/students?success=deleted";
    }

    @GetMapping("/books")
    public String books(Model model) {

        model.addAttribute("books", bookRepository.findAll());

        return "admin/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) throws IOException {

        Book book = bookRepository.findById(id).orElse(null);

        if (book != null) {
            deleteBookImage(book.getImage());
            bookRepository.delete(book);
        }

        return "redirect:/admin/books";
    }

    @GetMapping("/materials")
    public String materials(Model model) {

        model.addAttribute("materials", materialRepository.findAll());

        return "admin/materials";
    }
    @GetMapping("/materials/delete/{id}")
    public String deleteMaterial(@PathVariable Long id) throws IOException {

        Material material = materialRepository
                .findById(id)
                .orElse(null);

        if (material == null) {
            return "redirect:/admin/materials";
        }

        if (material.getFileName() != null
                && !material.getFileName().isBlank()) {

            Path filePath =
                    materialUploadDirectory.resolve(material.getFileName()).normalize();

            if (filePath.startsWith(materialUploadDirectory)
                    && Files.exists(filePath)) {

                Files.delete(filePath);
            }
        }

        materialRepository.delete(material);

        return "redirect:/admin/materials";
    }
    private void deleteBookImage(String imageName) throws IOException {

        if (imageName == null || imageName.isBlank()) {
            return;
        }

        Path filePath = bookUploadDirectory.resolve(imageName).normalize();

        if (filePath.startsWith(bookUploadDirectory) && Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

}
