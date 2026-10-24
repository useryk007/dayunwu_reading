package org.example.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoteEnrichedVO extends NoteVO {
    private Integer likeCount;
    private Boolean liked;
    private Integer commentCount;
    private Boolean following;
    private Boolean isOwner;
}