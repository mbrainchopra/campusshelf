package org.example.campusshelf.controller;

import org.example.campusshelf.entity.Book;
import org.example.campusshelf.entity.Student;
import org.example.campusshelf.repository.BookRepository;
import org.example.campusshelf.repository.StudentRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;

    private final Path uploadDirectory =
            Paths.get("uploads", "books").toAbsolutePath().normalize();

    public BookController(
            BookRepository bookRepository,
            StudentRepository studentRepository) {

        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public String viewBooks(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String department,
            @RequestParam(required = false, defaultValue = "") String semester,
            Model model) {

        List<Book> books;

        boolean hasSearch = !search.isBlank();
        boolean hasDepartment = !department.isBlank();
        boolean hasSemester = !semester.isBlank();

        if (hasSearch && hasDepartment && hasSemester) {
            books = bookRepository.findByBookNameContainingIgnoreCaseAndDepartmentAndSemester(
                    search, department, semester);
        } else if (hasSearch && hasDepartment) {
            books = bookRepository.findByBookNameContainingIgnoreCaseAndDepartment(
                    search, department);
        } else if (hasSearch && hasSemester) {
            books = bookRepository.findByBookNameContainingIgnoreCaseAndSemester(
                    search, semester);
        } else if (hasDepartment && hasSemester) {
            books = bookRepository.findByDepartmentAndSemester(department, semester);
        } else if (hasSearch) {
            books = bookRepository.findByBookNameContainingIgnoreCase(search);
        } else if (hasDepartment) {
            books = bookRepository.findByDepartment(department);
        } else if (hasSemester) {
            books = bookRepository.findBySemester(semester);
        } else {
            books = bookRepository.findAll();
        }

        model.addAttribute("books", books);
        model.addAttribute("search", search);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedSemester", semester);

        return "book/books";
    }

    @GetMapping("/add")
    public String showAddBookPage(
            @RequestParam Long studentId,
            Model model) {

        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null) {
            return "redirect:/students/login";
        }

        model.addAttribute("book", new Book());
        model.addAttribute("studentId", studentId);

        return "book/add-book";
    }

    @PostMapping("/save")
    public String saveBook(
            @ModelAttribute("book") Book book,
            @RequestParam Long studentId,
            @RequestParam(value = "bookImage", required = false) MultipartFile bookImage,
            Model model) throws IOException {

        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null) {
            return "redirect:/students/login";
        }

        if (bookImage != null && !bookImage.isEmpty()) {
            String contentType = bookImage.getContentType();

            if (contentType == null || !isAllowedImageType(contentType)) {
                model.addAttribute("book", book);
                model.addAttribute("studentId", studentId);
                model.addAttribute("error", "Only JPG, JPEG, PNG and WEBP images are allowed.");
                return "book/add-book";
            }

            if (bookImage.getSize() > 10 * 1024 * 1024) {
                model.addAttribute("book", book);
                model.addAttribute("studentId", studentId);
                model.addAttribute("error", "Book image must be 10 MB or smaller.");
                return "book/add-book";
            }

            Files.createDirectories(uploadDirectory);

            String extension = getExtension(bookImage.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + extension;
            Path filePath = uploadDirectory.resolve(storedFileName).normalize();

            if (!filePath.startsWith(uploadDirectory)) {
                throw new IOException("Invalid image path.");
            }

            Files.copy(bookImage.getInputStream(), filePath);
            book.setImage(storedFileName);
        }

        book.setStudent(student);
        bookRepository.save(book);

        return "redirect:/books";
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<Resource> bookImage(@PathVariable Long id) throws IOException {

        Book book = bookRepository.findById(id).orElse(null);

        if (book == null || book.getImage() == null || book.getImage().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = uploadDirectory.resolve(book.getImage()).normalize();

        if (!filePath.startsWith(uploadDirectory) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);
        String contentType = Files.probeContentType(filePath);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (contentType != null) {
            try {
                mediaType = MediaType.parseMediaType(contentType);
            } catch (Exception ignored) {
                // Keep application/octet-stream when the content type cannot be parsed.
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }

    @GetMapping("/view/{id}")
    public String viewBook(
            @PathVariable Long id,
            Model model) {

        Book book = bookRepository.findById(id).orElse(null);

        if (book == null) {
            return "redirect:/books";
        }

        model.addAttribute("book", book);
        return "book/book-details";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) throws IOException {

        Book book = bookRepository.findById(id).orElse(null);

        if (book != null) {
            deleteBookImage(book.getImage());
            bookRepository.delete(book);
        }

        return "redirect:/books";
    }

    public void deleteBookImage(String imageName) throws IOException {

        if (imageName == null || imageName.isBlank()) {
            return;
        }

        Path filePath = uploadDirectory.resolve(imageName).normalize();

        if (filePath.startsWith(uploadDirectory) && Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    private boolean isAllowedImageType(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/webp");
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return ".jpg";
        }

        String originalName = Paths.get(fileName).getFileName().toString();
        int dot = originalName.lastIndexOf('.');

        if (dot < 0) {
            return ".jpg";
        }

        String extension = originalName.substring(dot).toLowerCase();

        return switch (extension) {
            case ".jpg", ".jpeg", ".png", ".webp" -> extension;
            default -> ".jpg";
        };
    }
}
