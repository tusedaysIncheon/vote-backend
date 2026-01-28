package com.vote.votebackend.domain.vote.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class VoteRequestDTO {

    @NotBlank(message="내용은 필수 입니다.")
    private String content;
    private String imageUrl;
    @NotNull(message="투표 마감시간은 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endDate;

    @Size(min = 2, message = "최소 2개 이상 항목이 필요합니다.")
    @Valid
    private List<VoteOptionRequestDTO> options;

}
