package com.spring.notes.app.dto.response;

import com.spring.notes.app.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    
    private Long id;
    private String title;
    private String content;
    private Note.Priority priority;
    private Note.Status status;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    public static NoteResponse from(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .priority(note.getPriority())
                .status(note.getStatus())
                .category(note.getCategory())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .completedAt(note.getCompletedAt())
                .build();
    }
}
