package com.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProblemDetailResponse {

    private String type;
    private String title;
    private Integer status;
    private String errorCode;
    private String detail;
    private String instance;
    private LocalDateTime timestamp;
    private String requestId;
    private List<InvalidItemDetail> invalidItems;
    private List<FieldErrorDetail> fieldErrors;
    private String suggestedAction;

    public ProblemDetailResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static ProblemDetailResponse of(String type, String title, Integer status, String errorCode, String detail, String instance, String requestId) {
        ProblemDetailResponse response = new ProblemDetailResponse();
        response.setType(type);
        response.setTitle(title);
        response.setStatus(status);
        response.setErrorCode(errorCode);
        response.setDetail(detail);
        response.setInstance(instance);
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
