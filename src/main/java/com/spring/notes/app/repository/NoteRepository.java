package com.spring.notes.app.repository;

import com.spring.notes.app.entity.Note;
import com.spring.notes.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    // User-specific queries
    List<Note> findByUser(User user);
    Page<Note> findByUser(User user, Pageable pageable);
    Optional<Note> findByIdAndUser(Long id, User user);
    
    // Find notes by status and user
    List<Note> findByStatusAndUser(Note.Status status, User user);
    Page<Note> findByStatusAndUser(Note.Status status, User user, Pageable pageable);
    
    // Find notes by priority and user
    List<Note> findByPriorityAndUser(Note.Priority priority, User user);
    Page<Note> findByPriorityAndUser(Note.Priority priority, User user, Pageable pageable);
    
    // Find notes by category and user
    List<Note> findByCategoryAndUser(String category, User user);
    Page<Note> findByCategoryAndUser(String category, User user, Pageable pageable);
    
    // Find notes by title containing (case insensitive) and user
    List<Note> findByTitleContainingIgnoreCaseAndUser(String title, User user);
    
    // Find notes by content containing (case insensitive) and user
    List<Note> findByContentContainingIgnoreCaseAndUser(String content, User user);
    
    // Search notes by title or content for specific user
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (" +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Note> searchByTitleOrContentAndUser(@Param("searchTerm") String searchTerm, @Param("user") User user);
    
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (" +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Note> searchByTitleOrContentAndUser(@Param("searchTerm") String searchTerm, @Param("user") User user, Pageable pageable);
    
    // Find notes created between dates for specific user
    List<Note> findByCreatedAtBetweenAndUser(LocalDateTime startDate, LocalDateTime endDate, User user);
    
    // Count methods for user
    long countByUser(User user);
    long countByStatusAndUser(Note.Status status, User user);
    
    // Get distinct categories for user
    @Query("SELECT DISTINCT n.category FROM Note n WHERE n.user = :user AND n.category IS NOT NULL")
    List<String> findDistinctCategoriesByUser(@Param("user") User user);
    
    // Original methods (for admin access)
    List<Note> findByStatus(Note.Status status);
    List<Note> findByPriority(Note.Priority priority);
    List<Note> findByCategory(String category);
    List<Note> findByTitleContainingIgnoreCase(String title);
    List<Note> findByContentContainingIgnoreCase(String content);
    
    @Query("SELECT n FROM Note n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Note> searchByTitleOrContent(@Param("searchTerm") String searchTerm);
    
    List<Note> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    Page<Note> findByStatus(Note.Status status, Pageable pageable);
    Page<Note> findByPriority(Note.Priority priority, Pageable pageable);
    Page<Note> findByCategory(String category, Pageable pageable);
    
    // Search notes with pagination
    @Query("SELECT n FROM Note n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Note> searchByTitleOrContent(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Get all distinct categories
    @Query("SELECT DISTINCT n.category FROM Note n WHERE n.category IS NOT NULL ORDER BY n.category")
    List<String> findAllDistinctCategories();
    
    // Count notes by status
    long countByStatus(Note.Status status);
    
    // Count notes by priority
    long countByPriority(Note.Priority priority);
}
