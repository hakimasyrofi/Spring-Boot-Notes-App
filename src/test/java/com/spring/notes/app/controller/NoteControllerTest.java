package com.spring.notes.app.controller;

import com.spring.notes.app.dto.request.CreateNoteRequest;
import com.spring.notes.app.dto.request.UpdateNoteRequest;
import com.spring.notes.app.dto.response.ApiResponse;
import com.spring.notes.app.dto.response.NoteResponse;
import com.spring.notes.app.dto.response.PageResponse;
import com.spring.notes.app.entity.Note;
import com.spring.notes.app.entity.User;
import com.spring.notes.app.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {

    @Mock
    private NoteService noteService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NoteController noteController;

    private User testUser;
    private NoteResponse noteResponse;
    private CreateNoteRequest createRequest;
    private UpdateNoteRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        noteResponse = NoteResponse.builder()
                .id(1L)
                .title("Test Note")
                .content("Test content")
                .status(Note.Status.ACTIVE)
                .priority(Note.Priority.MEDIUM)
                .category("Test Category")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateNoteRequest.builder()
                .title("New Note")
                .content("New content")
                .priority(Note.Priority.HIGH)
                .category("New Category")
                .build();

        updateRequest = UpdateNoteRequest.builder()
                .title("Updated Note")
                .content("Updated content")
                .status(Note.Status.COMPLETED)
                .priority(Note.Priority.LOW)
                .category("Updated Category")
                .build();

        // Mock authentication
        when(authentication.getPrincipal()).thenReturn(testUser);
    }

    @Test
    void testCreateNote_Success() {
        // Given
        when(noteService.createNote(any(CreateNoteRequest.class), any(User.class)))
                .thenReturn(noteResponse);

        // When
        ResponseEntity<ApiResponse<NoteResponse>> response = noteController.createNote(createRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Note created successfully", response.getBody().getMessage());
        assertEquals("Test Note", response.getBody().getData().getTitle());
        assertEquals("Test content", response.getBody().getData().getContent());
    }

    @Test
    void testGetNoteById_Success() {
        // Given
        when(noteService.getNoteById(1L, testUser)).thenReturn(noteResponse);

        // When
        ResponseEntity<ApiResponse<NoteResponse>> response = noteController.getNoteById(1L, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Note retrieved successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals("Test Note", response.getBody().getData().getTitle());
    }

    @Test
    void testUpdateNote_Success() {
        // Given
        NoteResponse updatedResponse = NoteResponse.builder()
                .id(1L)
                .title("Updated Note")
                .content("Updated content")
                .status(Note.Status.COMPLETED)
                .priority(Note.Priority.LOW)
                .category("Updated Category")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(noteService.updateNote(1L, updateRequest, testUser)).thenReturn(updatedResponse);

        // When
        ResponseEntity<ApiResponse<NoteResponse>> response = noteController.updateNote(1L, updateRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Note updated successfully", response.getBody().getMessage());
        assertEquals("Updated Note", response.getBody().getData().getTitle());
        assertEquals(Note.Status.COMPLETED, response.getBody().getData().getStatus());
    }

    @Test
    void testDeleteNote_Success() {
        // Given
        // Note: deleteNote method is void, so we just verify it doesn't throw exception

        // When
        ResponseEntity<ApiResponse<Void>> response = noteController.deleteNote(1L, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Note deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testGetAllNotes_Success() {
        // Given
        List<NoteResponse> notes = Arrays.asList(noteResponse);
        PageResponse<NoteResponse> pageResponse = PageResponse.<NoteResponse>builder()
                .content(notes)
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        lenient().when(noteService.getAllNotes(any(User.class), any(Pageable.class))).thenReturn(pageResponse);

        // When
        ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> response = noteController.getAllNotes(0, 10, "createdAt", "desc", authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Notes retrieved successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getTotalElements());
        assertEquals(1, response.getBody().getData().getTotalPages());
    }

    @Test
    void testGetAllNotesWithPagination_Success() {
        // Given
        List<NoteResponse> notes = Arrays.asList(noteResponse);
        PageResponse<NoteResponse> pageResponse = PageResponse.<NoteResponse>builder()
                .content(notes)
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        lenient().when(noteService.getAllNotes(any(User.class), any(Pageable.class))).thenReturn(pageResponse);

        // When
        ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> response = noteController.getAllNotes(0, 10, "createdAt", "desc", authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Notes retrieved successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getTotalElements());
        assertEquals(1, response.getBody().getData().getTotalPages());
    }

    @Test
    void testGetNotesByStatus_Success() {
        // Given
        List<NoteResponse> notes = Arrays.asList(noteResponse);
        PageResponse<NoteResponse> pageResponse = PageResponse.<NoteResponse>builder()
                .content(notes)
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        lenient().when(noteService.getNotesByStatus(any(Note.Status.class), any(User.class), any(Pageable.class))).thenReturn(pageResponse);

        // When
        ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> response = noteController.getNotesByStatus(Note.Status.ACTIVE, 0, 10, "createdAt", "desc", authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Notes retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals(Note.Status.ACTIVE, response.getBody().getData().getContent().get(0).getStatus());
    }

    @Test
    void testGetNotesByPriority_Success() {
        // Given
        List<NoteResponse> notes = Arrays.asList(noteResponse);
        PageResponse<NoteResponse> pageResponse = PageResponse.<NoteResponse>builder()
                .content(notes)
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        lenient().when(noteService.getNotesByPriority(any(Note.Priority.class), any(User.class), any(Pageable.class))).thenReturn(pageResponse);

        // When
        ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> response = noteController.getNotesByPriority(Note.Priority.MEDIUM, 0, 10, "createdAt", "desc", authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Notes retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals(Note.Priority.MEDIUM, response.getBody().getData().getContent().get(0).getPriority());
    }

    @Test
    void testGetNotesByCategory_Success() {
        // Given
        List<NoteResponse> notes = Arrays.asList(noteResponse);
        PageResponse<NoteResponse> pageResponse = PageResponse.<NoteResponse>builder()
                .content(notes)
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        lenient().when(noteService.getNotesByCategory(anyString(), any(User.class), any(Pageable.class))).thenReturn(pageResponse);

        // When
        ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> response = noteController.getNotesByCategory("Test Category", 0, 10, "createdAt", "desc", authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Notes retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals("Test Category", response.getBody().getData().getContent().get(0).getCategory());
    }

    @Test
    void testSearchNotes_Success() {
        // Given
        List<NoteResponse> notes = Arrays.asList(noteResponse);
        PageResponse<NoteResponse> pageResponse = PageResponse.<NoteResponse>builder()
                .content(notes)
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        lenient().when(noteService.searchNotes(anyString(), any(User.class), any(Pageable.class))).thenReturn(pageResponse);

        // When
        ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> response = noteController.searchNotes("test", 0, 10, "createdAt", "desc", authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Notes retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals("Test Note", response.getBody().getData().getContent().get(0).getTitle());
    }

    @Test
    void testCompleteNote_Success() {
        // Given
        NoteResponse completedResponse = NoteResponse.builder()
                .id(1L)
                .title("Test Note")
                .content("Test content")
                .status(Note.Status.COMPLETED)
                .priority(Note.Priority.MEDIUM)
                .category("Test Category")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(noteService.completeNote(1L, testUser)).thenReturn(completedResponse);

        // When
        ResponseEntity<ApiResponse<NoteResponse>> response = noteController.completeNote(1L, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Note completed successfully", response.getBody().getMessage());
        assertEquals(Note.Status.COMPLETED, response.getBody().getData().getStatus());
    }

    @Test
    void testArchiveNote_Success() {
        // Given
        NoteResponse archivedResponse = NoteResponse.builder()
                .id(1L)
                .title("Test Note")
                .content("Test content")
                .status(Note.Status.ARCHIVED)
                .priority(Note.Priority.MEDIUM)
                .category("Test Category")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(noteService.archiveNote(1L, testUser)).thenReturn(archivedResponse);

        // When
        ResponseEntity<ApiResponse<NoteResponse>> response = noteController.archiveNote(1L, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Note archived successfully", response.getBody().getMessage());
        assertEquals(Note.Status.ARCHIVED, response.getBody().getData().getStatus());
    }

    @Test
    void testGetAllCategories_Success() {
        // Given
        List<String> categories = Arrays.asList("Category 1", "Category 2", "Category 3");
        when(noteService.getAllCategories(testUser)).thenReturn(categories);

        // When
        ResponseEntity<ApiResponse<List<String>>> response = noteController.getAllCategories(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Categories retrieved successfully", response.getBody().getMessage());
        assertEquals(3, response.getBody().getData().size());
        assertEquals("Category 1", response.getBody().getData().get(0));
        assertEquals("Category 2", response.getBody().getData().get(1));
        assertEquals("Category 3", response.getBody().getData().get(2));
    }

    @Test
    void testGetStatistics_Success() {
        // Given
        when(noteService.getTotalNotesCount(testUser)).thenReturn(10L);
        when(noteService.getNotesCountByStatus(Note.Status.ACTIVE, testUser)).thenReturn(5L);
        when(noteService.getNotesCountByStatus(Note.Status.COMPLETED, testUser)).thenReturn(3L);
        when(noteService.getNotesCountByStatus(Note.Status.ARCHIVED, testUser)).thenReturn(2L);

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = noteController.getStatistics(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Statistics retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(10L, response.getBody().getData().get("totalNotes"));
        assertEquals(5L, response.getBody().getData().get("activeNotes"));
        assertEquals(3L, response.getBody().getData().get("completedNotes"));
        assertEquals(2L, response.getBody().getData().get("archivedNotes"));
    }
}
