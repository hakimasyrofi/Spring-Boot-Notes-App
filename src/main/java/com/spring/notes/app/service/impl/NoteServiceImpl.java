package com.spring.notes.app.service.impl;

import com.spring.notes.app.dto.request.CreateNoteRequest;
import com.spring.notes.app.dto.request.UpdateNoteRequest;
import com.spring.notes.app.dto.response.NoteResponse;
import com.spring.notes.app.dto.response.PageResponse;
import com.spring.notes.app.entity.Note;
import com.spring.notes.app.entity.User;
import com.spring.notes.app.exception.ResourceNotFoundException;
import com.spring.notes.app.repository.NoteRepository;
import com.spring.notes.app.service.NoteService;
import com.spring.notes.app.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NoteServiceImpl implements NoteService {
    
    private final NoteRepository noteRepository;
    private final RedisService redisService;
    
    @Override
    @CacheEvict(value = {"notes", "userNotes", "noteCategories", "noteCounts"}, allEntries = true)
    public NoteResponse createNote(CreateNoteRequest request, User user) {
        log.info("Creating new note for user: {} with title: {}", user.getUsername(), request.getTitle());
        
        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority() != null ? request.getPriority() : Note.Priority.MEDIUM)
                .category(request.getCategory())
                .status(Note.Status.ACTIVE)
                .user(user)
                .build();
        
        Note savedNote = noteRepository.save(note);
        log.info("Note created successfully with ID: {} for user: {}", savedNote.getId(), user.getUsername());
        
        // Cache the new note
        String noteKey = "note:" + savedNote.getId();
        redisService.set(noteKey, NoteResponse.from(savedNote), 30, TimeUnit.MINUTES);
        
        return NoteResponse.from(savedNote);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "notes", key = "#id")
    public NoteResponse getNoteById(Long id, User user) {
        log.info("Fetching note with ID: {} for user: {}", id, user.getUsername());
        
        // Try to get from cache first
        String noteKey = "note:" + id;
        Optional<NoteResponse> cachedNote = redisService.get(noteKey, NoteResponse.class);
        if (cachedNote.isPresent()) {
            log.debug("Note found in cache for ID: {}", id);
            return cachedNote.get();
        }
        
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));
        
        NoteResponse noteResponse = NoteResponse.from(note);
        // Cache the note for 30 minutes
        redisService.set(noteKey, noteResponse, 30, TimeUnit.MINUTES);
        
        return noteResponse;
    }
    
    @Override
    @CacheEvict(value = {"notes", "userNotes", "noteCategories", "noteCounts"}, allEntries = true)
    public NoteResponse updateNote(Long id, UpdateNoteRequest request, User user) {
        log.info("Updating note with ID: {} for user: {}", id, user.getUsername());
        
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));
        
        // Update only non-null fields
        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (request.getPriority() != null) {
            note.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            note.setStatus(request.getStatus());
            // Set completed date if status is COMPLETED
            if (request.getStatus() == Note.Status.COMPLETED && note.getCompletedAt() == null) {
                note.setCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() != Note.Status.COMPLETED) {
                note.setCompletedAt(null);
            }
        }
        if (request.getCategory() != null) {
            note.setCategory(request.getCategory());
        }
        
        Note updatedNote = noteRepository.save(note);
        log.info("Note updated successfully with ID: {} for user: {}", updatedNote.getId(), user.getUsername());
        
        // Update cache
        String noteKey = "note:" + updatedNote.getId();
        redisService.set(noteKey, NoteResponse.from(updatedNote), 30, TimeUnit.MINUTES);
        
        return NoteResponse.from(updatedNote);
    }
    
    @Override
    @CacheEvict(value = {"notes", "userNotes", "noteCategories", "noteCounts"}, allEntries = true)
    public void deleteNote(Long id, User user) {
        log.info("Deleting note with ID: {} for user: {}", id, user.getUsername());
        
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));
        
        noteRepository.delete(note);
        
        // Remove from cache
        String noteKey = "note:" + id;
        redisService.delete(noteKey);
        
        log.info("Note deleted successfully with ID: {} for user: {}", id, user.getUsername());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userNotes", key = "#user.id")
    public List<NoteResponse> getAllNotes(User user) {
        log.info("Fetching all notes for user: {}", user.getUsername());
        
        // Try to get from cache first
        String userNotesKey = "user:notes:" + user.getId();
        Optional<Object> cachedNotes = redisService.get(userNotesKey);
        if (cachedNotes.isPresent() && cachedNotes.get() instanceof List) {
            @SuppressWarnings("unchecked")
            List<NoteResponse> notes = (List<NoteResponse>) cachedNotes.get();
            log.debug("User notes found in cache for user: {}", user.getUsername());
            return notes;
        }
        
        List<Note> notes = noteRepository.findByUser(user);
        List<NoteResponse> noteResponses = notes.stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
        
        // Cache the user notes for 15 minutes
        redisService.set(userNotesKey, noteResponses, 15, TimeUnit.MINUTES);
        
        return noteResponses;
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getAllNotes(User user, Pageable pageable) {
        log.info("Fetching all notes with pagination for user: {} - Page: {}, Size: {}", 
                user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Note> notePage = noteRepository.findByUser(user, pageable);
        return buildPageResponse(notePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByStatus(Note.Status status, User user) {
        log.info("Fetching notes by status: {} for user: {}", status, user.getUsername());
        
        List<Note> notes = noteRepository.findByStatusAndUser(status, user);
        return notes.stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByPriority(Note.Priority priority, User user) {
        log.info("Fetching notes by priority: {} for user: {}", priority, user.getUsername());
        
        List<Note> notes = noteRepository.findByPriorityAndUser(priority, user);
        return notes.stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByCategory(String category, User user) {
        log.info("Fetching notes by category: {} for user: {}", category, user.getUsername());
        
        List<Note> notes = noteRepository.findByCategoryAndUser(category, user);
        return notes.stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getNotesByStatus(Note.Status status, User user, Pageable pageable) {
        log.info("Fetching notes by status: {} with pagination for user: {} - Page: {}, Size: {}", 
                status, user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Note> notePage = noteRepository.findByStatusAndUser(status, user, pageable);
        return buildPageResponse(notePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getNotesByPriority(Note.Priority priority, User user, Pageable pageable) {
        log.info("Fetching notes by priority: {} with pagination for user: {} - Page: {}, Size: {}", 
                priority, user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Note> notePage = noteRepository.findByPriorityAndUser(priority, user, pageable);
        return buildPageResponse(notePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getNotesByCategory(String category, User user, Pageable pageable) {
        log.info("Fetching notes by category: {} with pagination for user: {} - Page: {}, Size: {}", 
                category, user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Note> notePage = noteRepository.findByCategoryAndUser(category, user, pageable);
        return buildPageResponse(notePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String searchTerm, User user) {
        log.info("Searching notes with term: '{}' for user: {}", searchTerm, user.getUsername());
        
        List<Note> notes = noteRepository.searchByTitleOrContentAndUser(searchTerm, user);
        return notes.stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> searchNotes(String searchTerm, User user, Pageable pageable) {
        log.info("Searching notes with term: '{}' with pagination for user: {} - Page: {}, Size: {}", 
                searchTerm, user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Note> notePage = noteRepository.searchByTitleOrContentAndUser(searchTerm, user, pageable);
        return buildPageResponse(notePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, User user) {
        log.info("Fetching notes created between {} and {} for user: {}", 
                startDate, endDate, user.getUsername());
        
        List<Note> notes = noteRepository.findByCreatedAtBetweenAndUser(startDate, endDate, user);
        return notes.stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    public NoteResponse completeNote(Long id, User user) {
        log.info("Completing note with ID: {} for user: {}", id, user.getUsername());
        
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));
        
        note.setStatus(Note.Status.COMPLETED);
        note.setCompletedAt(LocalDateTime.now());
        
        Note updatedNote = noteRepository.save(note);
        log.info("Note completed successfully with ID: {} for user: {}", updatedNote.getId(), user.getUsername());
        
        return NoteResponse.from(updatedNote);
    }
    
    @Override
    public NoteResponse archiveNote(Long id, User user) {
        log.info("Archiving note with ID: {} for user: {}", id, user.getUsername());
        
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));
        
        note.setStatus(Note.Status.ARCHIVED);
        
        Note updatedNote = noteRepository.save(note);
        log.info("Note archived successfully with ID: {} for user: {}", updatedNote.getId(), user.getUsername());
        
        return NoteResponse.from(updatedNote);
    }
    
    @Override
    public NoteResponse activateNote(Long id, User user) {
        log.info("Activating note with ID: {} for user: {}", id, user.getUsername());
        
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));
        
        note.setStatus(Note.Status.ACTIVE);
        note.setCompletedAt(null); // Clear completed date when reactivating
        
        Note updatedNote = noteRepository.save(note);
        log.info("Note activated successfully with ID: {} for user: {}", updatedNote.getId(), user.getUsername());
        
        return NoteResponse.from(updatedNote);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "noteCategories", key = "#user.id")
    public List<String> getAllCategories(User user) {
        log.info("Fetching all categories for user: {}", user.getUsername());
        
        // Try to get from cache first
        String categoriesKey = "user:categories:" + user.getId();
        Optional<Object> cachedCategories = redisService.get(categoriesKey);
        if (cachedCategories.isPresent() && cachedCategories.get() instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) cachedCategories.get();
            log.debug("Categories found in cache for user: {}", user.getUsername());
            return categories;
        }
        
        List<String> categories = noteRepository.findDistinctCategoriesByUser(user);
        
        // Cache the categories for 30 minutes
        redisService.set(categoriesKey, categories, 30, TimeUnit.MINUTES);
        
        return categories;
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "noteCounts", key = "#user.id + ':total'")
    public long getTotalNotesCount(User user) {
        log.info("Getting total notes count for user: {}", user.getUsername());
        
        // Try to get from cache first
        String countKey = "user:count:total:" + user.getId();
        Optional<Long> cachedCount = redisService.get(countKey, Long.class);
        if (cachedCount.isPresent()) {
            log.debug("Total count found in cache for user: {}", user.getUsername());
            return cachedCount.get();
        }
        
        long count = noteRepository.countByUser(user);
        
        // Cache the count for 10 minutes
        redisService.set(countKey, count, 10, TimeUnit.MINUTES);
        
        return count;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getNotesCountByStatus(Note.Status status, User user) {
        log.info("Getting notes count by status: {} for user: {}", status, user.getUsername());
        
        return noteRepository.countByStatusAndUser(status, user);
    }
    
    // Admin-only methods
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotesAdmin() {
        log.info("Admin: Fetching all notes");
        
        List<Note> notes = noteRepository.findAll();
        return notes.stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getAllNotesAdmin(Pageable pageable) {
        log.info("Admin: Fetching all notes with pagination - Page: {}, Size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Note> notePage = noteRepository.findAll(pageable);
        return buildPageResponse(notePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteByIdAdmin(Long id) {
        log.info("Admin: Fetching note with ID: {}", id);
        
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));
        
        return NoteResponse.from(note);
    }
    
    @Override
    public void deleteNoteAdmin(Long id) {
        log.info("Admin: Deleting note with ID: {}", id);
        
        if (!noteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Note not found with ID: " + id);
        }
        
        noteRepository.deleteById(id);
        log.info("Admin: Note deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getNotesCountByPriority(Note.Priority priority) {
        log.info("Fetching notes count by priority: {}", priority);
        return noteRepository.countByPriority(priority);
    }
    
    private PageResponse<NoteResponse> buildPageResponse(Page<Note> notePage) {
        List<NoteResponse> noteResponses = notePage.getContent().stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
        
        return PageResponse.<NoteResponse>builder()
                .content(noteResponses)
                .page(notePage.getNumber())
                .size(notePage.getSize())
                .totalElements(notePage.getTotalElements())
                .totalPages(notePage.getTotalPages())
                .first(notePage.isFirst())
                .last(notePage.isLast())
                .hasNext(notePage.hasNext())
                .hasPrevious(notePage.hasPrevious())
                .build();
    }
}
