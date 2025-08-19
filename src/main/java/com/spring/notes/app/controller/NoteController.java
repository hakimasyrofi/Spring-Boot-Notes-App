package com.spring.notes.app.controller;

import com.spring.notes.app.dto.request.CreateNoteRequest;
import com.spring.notes.app.dto.request.UpdateNoteRequest;
import com.spring.notes.app.dto.response.ApiResponse;
import com.spring.notes.app.dto.response.NoteResponse;
import com.spring.notes.app.dto.response.PageResponse;
import com.spring.notes.app.entity.Note;
import com.spring.notes.app.entity.User;
import com.spring.notes.app.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Notes management API")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {
    
    private final NoteService noteService;
    
    @PostMapping
    @Operation(summary = "Create a new note", description = "Create a new note with title, content, priority, and category")
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Creating new note for user: {} with title: {}", user.getUsername(), request.getTitle());
        
        NoteResponse noteResponse = noteService.createNote(request, user);
        ApiResponse<NoteResponse> response = ApiResponse.success("Note created successfully", noteResponse);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get note by ID", description = "Retrieve a specific note by its ID")
    public ResponseEntity<ApiResponse<NoteResponse>> getNoteById(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Fetching note with ID: {} for user: {}", id, user.getUsername());
        
        NoteResponse noteResponse = noteService.getNoteById(id, user);
        ApiResponse<NoteResponse> response = ApiResponse.success("Note retrieved successfully", noteResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update note", description = "Update an existing note by its ID")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Updating note with ID: {} for user: {}", id, user.getUsername());
        
        NoteResponse noteResponse = noteService.updateNote(id, request, user);
        ApiResponse<NoteResponse> response = ApiResponse.success("Note updated successfully", noteResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user's note", description = "Delete user's own note by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("User {} deleting note with ID: {}", user.getUsername(), id);
        
        noteService.deleteNote(id, user);
        ApiResponse<Void> response = ApiResponse.success("Note deleted successfully", null);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete note (Admin)", description = "Delete any note by its ID (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteNoteAdmin(@PathVariable Long id) {
        log.info("Admin deleting note with ID: {}", id);
        
        noteService.deleteNoteAdmin(id);
        ApiResponse<Void> response = ApiResponse.success("Note deleted successfully", null);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all notes", description = "Retrieve all notes with optional pagination and sorting")
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> getAllNotes(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Fetching all notes for user: {} - Page: {}, Size: {}", user.getUsername(), page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<NoteResponse> pageResponse = noteService.getAllNotes(user, pageable);
        ApiResponse<PageResponse<NoteResponse>> response = ApiResponse.success("Notes retrieved successfully", pageResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all notes (Admin)", description = "Retrieve all notes from all users (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> getAllNotesAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Admin fetching all notes - Page: {}, Size: {}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<NoteResponse> pageResponse = noteService.getAllNotesAdmin(pageable);
        ApiResponse<PageResponse<NoteResponse>> response = ApiResponse.success("Notes retrieved successfully", pageResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get note by ID (Admin)", description = "Retrieve any note by its ID (Admin only)")
    public ResponseEntity<ApiResponse<NoteResponse>> getNoteByIdAdmin(@PathVariable Long id) {
        log.info("Admin fetching note with ID: {}", id);
        
        NoteResponse noteResponse = noteService.getNoteByIdAdmin(id);
        ApiResponse<NoteResponse> response = ApiResponse.success("Note retrieved successfully", noteResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get notes by status", description = "Retrieve notes filtered by status")
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> getNotesByStatus(
            @PathVariable Note.Status status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Fetching notes by status: {} for user: {}", status, user.getUsername());
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<NoteResponse> pageResponse = noteService.getNotesByStatus(status, user, pageable);
        ApiResponse<PageResponse<NoteResponse>> response = ApiResponse.success("Notes retrieved successfully", pageResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get notes by priority", description = "Retrieve notes filtered by priority")
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> getNotesByPriority(
            @PathVariable Note.Priority priority,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Fetching notes by priority: {} for user: {}", priority, user.getUsername());
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<NoteResponse> pageResponse = noteService.getNotesByPriority(priority, user, pageable);
        ApiResponse<PageResponse<NoteResponse>> response = ApiResponse.success("Notes retrieved successfully", pageResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get notes by category", description = "Retrieve notes filtered by category")
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> getNotesByCategory(
            @PathVariable String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Fetching notes by category: {} for user: {}", category, user.getUsername());
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<NoteResponse> pageResponse = noteService.getNotesByCategory(category, user, pageable);
        ApiResponse<PageResponse<NoteResponse>> response = ApiResponse.success("Notes retrieved successfully", pageResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search notes", description = "Search notes by title or content")
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> searchNotes(
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Searching notes with query: '{}' for user: {}", q, user.getUsername());
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<NoteResponse> pageResponse = noteService.searchNotes(q, user, pageable);
        ApiResponse<PageResponse<NoteResponse>> response = ApiResponse.success("Notes retrieved successfully", pageResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get notes in date range", description = "Retrieve notes created within a specific date range")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotesInDateRange(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Fetching notes created between {} and {} for user: {}", startDate, endDate, user.getUsername());
        
        List<NoteResponse> notes = noteService.getNotesCreatedBetween(startDate, endDate, user);
        ApiResponse<List<NoteResponse>> response = ApiResponse.success("Notes retrieved successfully", notes);
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete note", description = "Mark a note as completed")
    public ResponseEntity<ApiResponse<NoteResponse>> completeNote(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Completing note with ID: {} for user: {}", id, user.getUsername());
        
        NoteResponse noteResponse = noteService.completeNote(id, user);
        ApiResponse<NoteResponse> response = ApiResponse.success("Note completed successfully", noteResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive note", description = "Archive a note")
    public ResponseEntity<ApiResponse<NoteResponse>> archiveNote(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Archiving note with ID: {} for user: {}", id, user.getUsername());
        
        NoteResponse noteResponse = noteService.archiveNote(id, user);
        ApiResponse<NoteResponse> response = ApiResponse.success("Note archived successfully", noteResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate note", description = "Reactivate an archived or completed note")
    public ResponseEntity<ApiResponse<NoteResponse>> activateNote(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Activating note with ID: {} for user: {}", id, user.getUsername());
        
        NoteResponse noteResponse = noteService.activateNote(id, user);
        ApiResponse<NoteResponse> response = ApiResponse.success("Note activated successfully", noteResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve all distinct categories used by the user")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Fetching all categories for user: {}", user.getUsername());
        
        List<String> categories = noteService.getAllCategories(user);
        ApiResponse<List<String>> response = ApiResponse.success("Categories retrieved successfully", categories);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get notes statistics", description = "Retrieve statistics about user's notes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Fetching statistics for user: {}", user.getUsername());
        
        long totalNotes = noteService.getTotalNotesCount(user);
        long activeNotes = noteService.getNotesCountByStatus(Note.Status.ACTIVE, user);
        long completedNotes = noteService.getNotesCountByStatus(Note.Status.COMPLETED, user);
        long archivedNotes = noteService.getNotesCountByStatus(Note.Status.ARCHIVED, user);
        
        Map<String, Object> statistics = Map.of(
                "totalNotes", totalNotes,
                "activeNotes", activeNotes,
                "completedNotes", completedNotes,
                "archivedNotes", archivedNotes
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success("Statistics retrieved successfully", statistics);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin statistics", description = "Retrieve comprehensive statistics about all notes (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStatistics() {
        log.info("Admin fetching comprehensive statistics");
        
        Map<String, Object> statistics = Map.of(
                "highPriorityNotes", noteService.getNotesCountByPriority(Note.Priority.HIGH),
                "urgentNotes", noteService.getNotesCountByPriority(Note.Priority.URGENT)
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success("Admin statistics retrieved successfully", statistics);
        
        return ResponseEntity.ok(response);
    }
}
