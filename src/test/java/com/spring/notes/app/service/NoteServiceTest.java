package com.spring.notes.app.service;

import com.spring.notes.app.dto.request.CreateNoteRequest;
import com.spring.notes.app.dto.request.UpdateNoteRequest;
import com.spring.notes.app.dto.response.NoteResponse;
import com.spring.notes.app.dto.response.PageResponse;
import com.spring.notes.app.entity.Note;
import com.spring.notes.app.entity.User;
import com.spring.notes.app.exception.ResourceNotFoundException;
import com.spring.notes.app.repository.NoteRepository;
import com.spring.notes.app.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User testUser;
    private Note testNote;
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

        testNote = Note.builder()
                .id(1L)
                .title("Test Note")
                .content("Test content")
                .status(Note.Status.ACTIVE)
                .priority(Note.Priority.MEDIUM)
                .category("Test Category")
                .user(testUser)
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
    }

    @Test
    void testCreateNote_Success() {
        // Given
    Note savedNote = Note.builder()
        .id(1L)
        .title(createRequest.getTitle())
        .content(createRequest.getContent())
        .status(Note.Status.ACTIVE)
        .priority(createRequest.getPriority())
        .category(createRequest.getCategory())
        .user(testUser)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // When
        NoteResponse response = noteService.createNote(createRequest, testUser);

        // Then
    assertNotNull(response);
    assertEquals(createRequest.getTitle(), response.getTitle());
    assertEquals(createRequest.getContent(), response.getContent());
    assertEquals(Note.Status.ACTIVE, response.getStatus());
    assertEquals(createRequest.getPriority(), response.getPriority());
    assertEquals(createRequest.getCategory(), response.getCategory());
    // UserId is not present in NoteResponse, so skip this assertion

        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void testGetNoteById_Success() {
        // Given
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testNote));

        // When
        NoteResponse response = noteService.getNoteById(1L, testUser);

        // Then
        assertNotNull(response);
        assertEquals(testNote.getId(), response.getId());
        assertEquals(testNote.getTitle(), response.getTitle());
        assertEquals(testNote.getContent(), response.getContent());

        verify(noteRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    void testGetNoteById_NotFound() {
        // Given
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            noteService.getNoteById(1L, testUser);
        });

        verify(noteRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    void testUpdateNote_Success() {
        // Given
        Note updatedNote = Note.builder()
                .id(1L)
                .title(updateRequest.getTitle())
                .content(updateRequest.getContent())
                .status(updateRequest.getStatus())
                .priority(updateRequest.getPriority())
                .category(updateRequest.getCategory())
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);

        // When
        NoteResponse response = noteService.updateNote(1L, updateRequest, testUser);

        // Then
        assertNotNull(response);
        assertEquals(updateRequest.getTitle(), response.getTitle());
        assertEquals(updateRequest.getContent(), response.getContent());
        assertEquals(updateRequest.getStatus(), response.getStatus());
        assertEquals(updateRequest.getPriority(), response.getPriority());
        assertEquals(updateRequest.getCategory(), response.getCategory());

        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void testUpdateNote_NotFound() {
        // Given
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            noteService.updateNote(1L, updateRequest, testUser);
        });

        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void testDeleteNote_Success() {
        // Given
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testNote));
        doNothing().when(noteRepository).delete(testNote);

        // When
        noteService.deleteNote(1L, testUser);

        // Then
        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository).delete(testNote);
    }

    @Test
    void testDeleteNote_NotFound() {
        // Given
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            noteService.deleteNote(1L, testUser);
        });

        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void testGetAllNotes_Success() {
        // Given
        List<Note> notes = Arrays.asList(testNote);
    when(noteRepository.findByUser(testUser)).thenReturn(notes);

        // When
        List<NoteResponse> responses = noteService.getAllNotes(testUser);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testNote.getId(), responses.get(0).getId());

    verify(noteRepository).findByUser(testUser);
    }

    @Test
    void testGetAllNotesWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Note> notes = Arrays.asList(testNote);
        Page<Note> notePage = new PageImpl<>(notes, pageable, 1);
        
    when(noteRepository.findByUser(testUser, pageable)).thenReturn(notePage);

        // When
        PageResponse<NoteResponse> response = noteService.getAllNotes(testUser, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());

    verify(noteRepository).findByUser(testUser, pageable);
    }

    @Test
    void testGetNotesByStatus_Success() {
        // Given
        List<Note> notes = Arrays.asList(testNote);
    when(noteRepository.findByStatusAndUser(Note.Status.ACTIVE, testUser))
        .thenReturn(notes);

        // When
        List<NoteResponse> responses = noteService.getNotesByStatus(Note.Status.ACTIVE, testUser);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(Note.Status.ACTIVE, responses.get(0).getStatus());

    verify(noteRepository).findByStatusAndUser(Note.Status.ACTIVE, testUser);
    }

    @Test
    void testSearchNotes_Success() {
        // Given
        List<Note> notes = Arrays.asList(testNote);
    when(noteRepository.searchByTitleOrContentAndUser("test", testUser)).thenReturn(notes);

        // When
        List<NoteResponse> responses = noteService.searchNotes("test", testUser);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());

    verify(noteRepository).searchByTitleOrContentAndUser("test", testUser);
    }

    @Test
    void testCompleteNote_Success() {
        // Given
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        // When
        NoteResponse response = noteService.completeNote(1L, testUser);

        // Then
        assertNotNull(response);
        assertEquals(Note.Status.COMPLETED, response.getStatus());

        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void testGetTotalNotesCount_Success() {
        // Given
        when(noteRepository.countByUser(testUser)).thenReturn(5L);

        // When
        long count = noteService.getTotalNotesCount(testUser);

        // Then
        assertEquals(5L, count);
        verify(noteRepository).countByUser(testUser);
    }
}
