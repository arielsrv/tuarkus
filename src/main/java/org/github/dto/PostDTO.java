package org.github.dto;

import java.util.List;

public record PostDTO(Long id, String title, List<CommentDTO> comments) {
}
