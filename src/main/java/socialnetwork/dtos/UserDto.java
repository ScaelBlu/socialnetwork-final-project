package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import socialnetwork.models.PersonalData;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto {

    @Schema(description = "The ID of the user", example = "1")
    private long id;

    @Schema(description = "The username of the user", example = "littlejohn20")
    private String username;

    @Schema(description = "The email address of the user", example = "myemail@example.com")
    private String email;

    @Schema(description = "The hashed password of the user", example = "39d1da1f4f9fda75ac2c0b29b76c2149fe57256e3240ce35e1e74d6b6d898222")
    private String password;

    @Schema(description = "The registration time", example = "2023-02-03T15:30:00")
    private LocalDateTime registrationTime;

    @Schema(description = "The personal data of the user")
    private PersonalData personalData;

    @Schema(description = "The ID's of the related users", example = "[1, 3, 4]")
    private Set<Long> friends;
}
