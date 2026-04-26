package com.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMetadataDTO {
    private Long groupId;
    private Integer currentKeyVersion;
    private List<UserSummaryDTO> members;
    private Map<Long, Long> readCursors;
}