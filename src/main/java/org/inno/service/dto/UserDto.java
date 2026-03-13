package org.inno.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Email is not empty")
    private String name;

    @NotBlank(message = "Surname is not empty")
    private String surname;

    @Past(message = "Birthdate must be in past")
    private LocalDate birthDate;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private List<CardDto> cardDtos = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active = true;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;
}
