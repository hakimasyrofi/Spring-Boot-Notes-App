package com.spring.notes.app.service;

import com.spring.notes.app.dto.request.CreateNoteRequest;
import com.spring.notes.app.dto.request.UpdateNoteRequest;
import com.spring.notes.app.dto.response.NoteResponse;
import com.spring.notes.app.dto.response.PageResponse;
import com.spring.notes.app.entity.Note;
import com.spring.notes.app.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NoteService {
    
    // Basic CRUD operations (user-specific)
    NoteResponse createNote(CreateNoteRequest request, User user);
    NoteResponse getNoteById(Long id, User user);
    NoteResponse updateNote(Long id, UpdateNoteRequest request, User user);
    void deleteNote(Long id, User user);
    
    // List operations (user-specific)
    List<NoteResponse> getAllNotes(User user);
    PageResponse<NoteResponse> getAllNotes(User user, Pageable pageable);
    
    // Filter operations (user-specific)
    List<NoteResponse> getNotesByStatus(Note.Status status, User user);
    List<NoteResponse> getNotesByPriority(Note.Priority priority, User user);
    List<NoteResponse> getNotesByCategory(String category, User user);
    
    // Filter operations with pagination (user-specific)
    PageResponse<NoteResponse> getNotesByStatus(Note.Status status, User user, Pageable pageable);
    PageResponse<NoteResponse> getNotesByPriority(Note.Priority priority, User user, Pageable pageable);
    PageResponse<NoteResponse> getNotesByCategory(String category, User user, Pageable pageable);
    
    // Search operations (user-specific)
    List<NoteResponse> searchNotes(String searchTerm, User user);
    PageResponse<NoteResponse> searchNotes(String searchTerm, User user, Pageable pageable);
    
    // Date range operations (user-specific)
    List<NoteResponse> getNotesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, User user);
    
    // Status operations (user-specific)
    NoteResponse completeNote(Long id, User user);
    NoteResponse archiveNote(Long id, User user);
    NoteResponse activateNote(Long id, User user);
    
    // Statistics (user-specific)
    List<String> getAllCategories(User user);
    long getTotalNotesCount(User user);
    long getNotesCountByStatus(Note.Status status, User user);
    
    // Admin-only methods
    List<NoteResponse> getAllNotesAdmin();
    PageResponse<NoteResponse> getAllNotesAdmin(Pageable pageable);
    NoteResponse getNoteByIdAdmin(Long id);
    void deleteNoteAdmin(Long id);
    long getNotesCountByPriority(Note.Priority priority);
}
