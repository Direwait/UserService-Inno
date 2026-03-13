package org.inno.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDto implements Serializable {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull(message = "User id is required")
    private UUID userId;

    @NotBlank(message = "Number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    private String number;

    @NotBlank(message = "Holder is not empty")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    @NotNull(message = "expirationDate is required")
    private LocalDate expirationDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

}
