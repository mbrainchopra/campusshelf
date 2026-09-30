package org.example.campusshelf.controller;

import org.example.campusshelf.entity.Material;
import org.example.campusshelf.entity.Student;
import org.example.campusshelf.repository.MaterialRepository;
import org.example.campusshelf.repository.StudentRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialRepository materialRepository;
    private final StudentRepository studentRepository;

    private final Path uploadDirectory =
            Paths.get("uploads", "materials").toAbsolutePath().normalize();

    public MaterialController(
            MaterialRepository materialRepository,
            StudentRepository studentRepository) {

        this.materialRepository = materialRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public String viewMaterials(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String department,
            @RequestParam(required = false, defaultValue = "") String semester,
            Model model) {

        List<Material> materials;

        boolean hasSearch = !search.isBlank();
        boolean hasDepartment = !department.isBlank();
        boolean hasSemester = !semester.isBlank();

        if (hasSearch && hasDepartment && hasSemester) {
            materials = materialRepository
                    .findByTitleContainingIgnoreCaseAndDepartmentAndSemester(
                            search, department, semester);
        } else if (hasSearch && hasDepartment) {
            materials = materialRepository
                    .findByTitleContainingIgnoreCaseAndDepartment(
                            search, department);
        } else if (hasSearch && hasSemester) {
            materials = materialRepository
                    .findByTitleContainingIgnoreCaseAndSemester(
                            search, semester);
        } else if (hasDepartment && hasSemester) {
            materials = materialRepository
                    .findByDepartmentAndSemester(
                            department, semester);
        } else if (hasSearch) {
            materials = materialRepository
                    .findByTitleContainingIgnoreCase(search);
        } else if (hasDepartment) {
            materials = materialRepository
                    .findByDepartment(department);
        } else if (hasSemester) {
            materials = materialRepository
                    .findBySemester(semester);
        } else {
            materials = materialRepository.findAll();
        }

        model.addAttribute("materials", materials);
        model.addAttribute("search", search);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedSemester", semester);

        return "material/materials";
    }

    @GetMapping("/add")
    public String showAddMaterialPage(
            @RequestParam Long studentId,
            Model model) {

        Student student = studentRepository
                .findById(studentId)
                .orElse(null);

        if (student == null) {
            return "redirect:/students/login";
        }

        model.addAttribute("material", new Material());
        model.addAttribute("studentId", studentId);

        return "material/add-material";
    }

    @PostMapping("/save")
    public String saveMaterial(
            @ModelAttribute("material") Material material,
            @RequestParam Long studentId,
            @RequestParam("file") MultipartFile file,
            Model model) throws IOException {

        Student student = studentRepository
                .findById(studentId)
                .orElse(null);

        if (student == null) {
            return "redirect:/students/login";
        }

        if (file == null || file.isEmpty()) {
            model.addAttribute("material", material);
            model.addAttribute("studentId", studentId);
            model.addAttribute("error", "Please select a file to upload.");
            return "material/add-material";
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            model.addAttribute("material", material);
            model.addAttribute("studentId", studentId);
            model.addAttribute("error", "Invalid file name.");
            return "material/add-material";
        }

        String extension = "";

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex).toLowerCase();
        }

        if (!extension.equals(".pdf")
                && !extension.equals(".doc")
                && !extension.equals(".docx")) {

            model.addAttribute("material", material);
            model.addAttribute("studentId", studentId);
            model.addAttribute("error", "Only PDF, DOC and DOCX files are allowed.");
            return "material/add-material";
        }

        Files.createDirectories(uploadDirectory);

        String safeFileName =
                UUID.randomUUID() + extension;

        Path filePath =
                uploadDirectory.resolve(safeFileName).normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IOException("Invalid file path.");
        }

        file.transferTo(filePath);

        material.setFileName(safeFileName);
        material.setOriginalFileName(originalFileName);
        material.setStudent(student);

        materialRepository.save(material);

        return "redirect:/materials";
    }

    @GetMapping("/view/{id}")
    public String viewMaterial(
            @PathVariable Long id,
            Model model) {

        Material material = materialRepository
                .findById(id)
                .orElse(null);

        if (material == null) {
            return "redirect:/materials";
        }

        model.addAttribute("material", material);

        return "material/material-details";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable Long id) throws IOException {

        Material material = materialRepository
                .findById(id)
                .orElse(null);

        if (material == null
                || material.getFileName() == null
                || material.getFileName().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        Path filePath =
                uploadDirectory.resolve(material.getFileName()).normalize();

        if (!filePath.startsWith(uploadDirectory)
                || !Files.exists(filePath)) {

            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);

        String downloadName = material.getOriginalFileName();

        if (downloadName == null || downloadName.isBlank()) {
            downloadName = material.getFileName();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadName + "\""
                )
                .body(resource);
    }

    @GetMapping("/delete/{id}")
    public String deleteMaterial(
            @PathVariable Long id) throws IOException {

        Material material = materialRepository
                .findById(id)
                .orElse(null);

        if (material == null) {
            return "redirect:/materials";
        }

        deletePhysicalFile(material.getFileName());

        materialRepository.delete(material);

        return "redirect:/materials";
    }

    public void deletePhysicalFile(String fileName) throws IOException {

        if (fileName == null || fileName.isBlank()) {
            return;
        }

        Path filePath =
                uploadDirectory.resolve(fileName).normalize();

        if (filePath.startsWith(uploadDirectory)
                && Files.exists(filePath)) {

            Files.delete(filePath);
        }
    }
}
