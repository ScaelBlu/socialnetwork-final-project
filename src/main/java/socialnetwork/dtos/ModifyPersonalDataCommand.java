package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModifyPersonalDataCommand {

    @Schema(description = "The real name of the user", example = "John Doe", nullable = true)
    private String realName;

    @Past(message = "The date of birth must be in the past!")
    @Schema(description = "The date of birth of the user", example = "2002-03-04", nullable = true)
    private LocalDate dateOfBirth;

    @Schema(description = "The name of the city where the user is located", example = "Budapest", nullable = true)
    private String city;
}
