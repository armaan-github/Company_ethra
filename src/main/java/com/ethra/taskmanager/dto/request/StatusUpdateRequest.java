package com.ethra.taskmanager.dto.request;

import com.ethra.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
