package com.spring.notes.app.dto.request;

import com.spring.notes.app.entity.Note;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoteRequest {
    
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;
    
    private Note.Priority priority;
    
    private Note.Status status;
    
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
}
