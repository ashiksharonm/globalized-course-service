package com.globallearning.courseservice.controller;

import com.globallearning.courseservice.dto.request.ProgressEventRequest;
import com.globallearning.courseservice.dto.response.CoursePreviewResponse;
import com.globallearning.courseservice.dto.response.ProgressResponse;
import com.globallearning.courseservice.service.CoursePreviewService;
import com.globallearning.courseservice.service.ProgressService;
import com.globallearning.courseservice.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVC slice tests for {@link CoursePreviewController} and
 * {@link ProgressController}.
 *
 * <p>
 * Uses {@code @WebMvcTest} to load only the web layer (no DB, no full context).
 * Service dependencies are mocked to isolate HTTP contract validation.
 */
@WebMvcTest(controllers = { CoursePreviewController.class, ProgressController.class })
class CourseAndProgressControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoursePreviewService coursePreviewService;

    @MockBean
    private ProgressService progressService;

    private final UUID COURSE_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    @Test
    @DisplayName("GET /courses/{id}/preview returns 200 with localized data")
    void getCoursePreviewReturns200() throws Exception {
        CoursePreviewResponse response = CoursePreviewResponse.builder()
                .courseId(COURSE_ID)
                .title("Intro ML FR")
                .description("Desc")
                .durationFormatted("2h 30min")
                .locale("fr")
                .fallbackUsed(true)
                .build();

        when(coursePreviewService.getPreview(eq(COURSE_ID), eq("fr-CA"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/courses/{id}/preview", COURSE_ID)
                .header("Accept-Language", "fr-CA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Intro ML FR"))
                .andExpect(jsonPath("$.locale").value("fr"))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.durationFormatted").value("2h 30min"));
    }

    @Test
    @DisplayName("GET /courses/{id}/preview returns 404 for unknown course")
    void getCoursePreviewReturns404() throws Exception {
        when(coursePreviewService.getPreview(any(), any()))
                .thenThrow(new ResourceNotFoundException("Course not found"));

        mockMvc.perform(get("/api/v1/courses/{id}/preview", UUID.randomUUID())
                .header("Accept-Language", "en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Course not found"));
    }

    @Test
    @DisplayName("POST /users/{userId}/courses/{courseId}/progress returns 200")
    void recordProgressReturns200() throws Exception {
        ProgressResponse response = ProgressResponse.builder()
                .courseId(COURSE_ID).userId("user-1")
                .completedLessons(3).totalLessons(10)
                .percentageComplete(30).progressMessage("30% complete").locale("en")
                .build();

        when(progressService.recordProgress(anyString(), any(UUID.class), any(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/users/user-1/courses/{courseId}/progress", COURSE_ID)
                .header("Accept-Language", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProgressEventRequest("lesson-01"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentageComplete").value(30))
                .andExpect(jsonPath("$.progressMessage").value("30% complete"));
    }

    @Test
    @DisplayName("POST /progress with blank lessonId returns 400 validation error")
    void recordProgressValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/users/user-1/courses/{courseId}/progress", COURSE_ID)
                .header("Accept-Language", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lessonId\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("GET /users/{userId}/courses/{courseId}/progress returns localized message")
    void getProgressReturnsLocalizedMessage() throws Exception {
        ProgressResponse response = ProgressResponse.builder()
                .courseId(COURSE_ID).userId("user-1")
                .completedLessons(5).totalLessons(10)
                .percentageComplete(50).progressMessage("50% terminé — continuez !").locale("fr")
                .build();

        when(progressService.getProgress(anyString(), any(UUID.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/users/user-1/courses/{courseId}/progress", COURSE_ID)
                .header("Accept-Language", "fr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressMessage").value("50% terminé — continuez !"))
                .andExpect(jsonPath("$.locale").value("fr"));
    }
}
