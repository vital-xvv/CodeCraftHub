package com.codecrafthub.service;

import com.codecrafthub.exception.CourseNotFoundException;
import com.codecrafthub.exception.FileStorageException;
import com.codecrafthub.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service layer responsible for course operations and JSON file storage.
 */
@Service
public class CourseService {

    /*
     * The required JSON file name.
     *
     * This file will be created in the application's
     * current working directory.
     */
    private final Path coursesFile = Path.of("courses.json");

    private final ObjectMapper objectMapper;

    public CourseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        /*
         * JavaTimeModule allows Jackson to handle:
         * - LocalDate
         * - Instant
         */
        this.objectMapper.registerModule(new JavaTimeModule());

        createFileIfItDoesNotExist();
    }

    /**
     * Returns every course from courses.json.
     */
    public synchronized List<Course> getAllCourses() {
        return readCoursesFromFile();
    }

    /**
     * Finds one course by its ID.
     */
    public synchronized Course getCourseById(Long id) {
        return readCoursesFromFile()
                .stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    /**
     * Creates a new course.
     * <p>
     * The ID is generated automatically, starting at 1.
     * The createdAt timestamp is also generated automatically.
     */
    public synchronized Course createCourse(Course course) {
        List<Course> courses = readCoursesFromFile();

        long nextId = getNextId(courses);

        course.setId(nextId);
        course.setCreatedAt(java.time.Instant.now());

        courses.add(course);
        writeCoursesToFile(courses);

        return course;
    }

    /**
     * Updates an existing course.
     * <p>
     * The original ID and createdAt values are preserved.
     */
    public synchronized Course updateCourse(Long id, Course updatedCourse) {
        List<Course> courses = readCoursesFromFile();

        Course existingCourse = courses
                .stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new CourseNotFoundException(id));

        /*
         * Preserve server-controlled fields.
         */
        updatedCourse.setId(existingCourse.getId());
        updatedCourse.setCreatedAt(existingCourse.getCreatedAt());

        int courseIndex = courses.indexOf(existingCourse);
        courses.set(courseIndex, updatedCourse);

        writeCoursesToFile(courses);

        return updatedCourse;
    }

    /**
     * Deletes a course by ID.
     */
    public synchronized void deleteCourse(Long id) {
        List<Course> courses = readCoursesFromFile();

        boolean wasRemoved = courses.removeIf(
                course -> course.getId().equals(id)
        );

        if (!wasRemoved) {
            throw new CourseNotFoundException(id);
        }

        writeCoursesToFile(courses);
    }

    /**
     * Creates courses.json automatically if it does not exist.
     */
    private void createFileIfItDoesNotExist() {
        try {
            if (Files.notExists(coursesFile)) {
                Files.writeString(coursesFile, "[]");
            }
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Unable to create courses.json",
                    exception
            );
        }
    }

    /**
     * Reads the complete JSON file and converts it into a list of courses.
     */
    private List<Course> readCoursesFromFile() {
        try {
            String json = Files.readString(coursesFile);

            if (json.isBlank()) {
                return new ArrayList<>();
            }

            List<Course> courses = objectMapper.readValue(
                    json,
                    new TypeReference<List<Course>>() {
                    }
            );

            /*
             * Return a mutable list so that create, update,
             * and delete operations can modify it.
             */
            return new ArrayList<>(courses);

        } catch (IOException exception) {
            throw new FileStorageException(
                    "Unable to read courses.json",
                    exception
            );
        }
    }

    /**
     * Converts the course list to formatted JSON and saves it.
     * <p>
     * A temporary file is used first. This reduces the risk of
     * leaving courses.json partially written.
     */
    private void writeCoursesToFile(List<Course> courses) {
        Path temporaryFile = Path.of("courses.json.tmp");

        try {
            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(courses);

            Files.writeString(temporaryFile, json);

            /*
             * Replace the original file with the completed temporary file.
             */
            try {
                Files.move(
                        temporaryFile,
                        coursesFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                /*
                 * Some operating systems do not support atomic moves.
                 * Fall back to a normal replacement move.
                 */
                Files.move(
                        temporaryFile,
                        coursesFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException exception) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException ignored) {
                // Do not hide the original file error.
            }

            throw new FileStorageException(
                    "Unable to write courses.json",
                    exception
            );
        }
    }

    /**
     * Calculates the next ID.
     * <p>
     * For an empty file, the first ID is 1.
     */
    private long getNextId(List<Course> courses) {
        return courses
                .stream()
                .map(Course::getId)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0L) + 1;
    }
}
