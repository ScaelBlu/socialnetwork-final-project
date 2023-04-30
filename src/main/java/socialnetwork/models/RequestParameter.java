package socialnetwork.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestParameter {

    @Schema(description = "Substring of username", example = "ohn3")
    private String username;

    @Schema(description = "Substring of email address", example = "ess@exa")
    private String email;

    @Schema(description = "Timestamp after which users were registered", example = "2023-01-01T00:00:00")
    private LocalDateTime registeredAfter;

    @Schema(description = "Substring of real name", example = "Doe")
    private String realName;

    @Schema(description = "Exact city name where users are actually located", example = "Budapest")
    private String city;
}
