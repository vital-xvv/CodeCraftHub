package com.codecrafthub.controller;

import com.codecrafthub.model.Course;
import com.codecrafthub.model.Course.CourseStatus;
import com.codecrafthub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for course CRUD operations.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * POST /api/courses
     * <p>
     * Creates a new course.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course createCourse(
            @Valid @RequestBody Course course) {

        return courseService.createCourse(course);
    }

    /**
     * GET /api/courses
     * <p>
     * Returns all courses.
     */
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    /**
     * GET /api/courses/stats
     * <p>
     * Returns the total number of courses and the number
     * of courses in each status.
     * <p>
     * This endpoint is declared before /{id} so that "stats"
     * is handled as a fixed path rather than an ID.
     */
    @GetMapping("/stats")
    public Map<String, Object> getCourseStatistics() {
        List<Course> courses = courseService.getAllCourses();

        long notStartedCount = courses.stream()
                .filter(course -> course.getStatus() == CourseStatus.NOT_STARTED)
                .count();

        long inProgressCount = courses.stream()
                .filter(course -> course.getStatus() == CourseStatus.IN_PROGRESS)
                .count();

        long completedCount = courses.stream()
                .filter(course -> course.getStatus() == CourseStatus.COMPLETED)
                .count();

        /*
         * LinkedHashMap preserves the order of fields
         * in the JSON response.
         */
        Map<String, Object> statistics = new LinkedHashMap<>();

        statistics.put("total_courses", courses.size());
        statistics.put("Not Started", notStartedCount);
        statistics.put("In Progress", inProgressCount);
        statistics.put("Completed", completedCount);

        return statistics;
    }

    /**
     * GET /api/courses/{id}
     * <p>
     * Returns one course by ID.
     */
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    /**
     * PUT /api/courses/{id}
     * <p>
     * Updates an existing course.
     */
    @PutMapping("/{id}")
    public Course updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course course) {

        return courseService.updateCourse(id, course);
    }

    /**
     * DELETE /api/courses/{id}
     * <p>
     * Deletes an existing course.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}
