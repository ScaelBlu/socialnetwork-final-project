package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateUserCommand {

    @NotBlank(message = "Username must not be null, empty, or blank!")
    @Size(min = 5, max = 31, message = "Username length must be between 5 and 32 characters!")
    @Schema(description = "A unique username", example = "lifelover", minLength = 5, maxLength = 31)
    private String username;

    @NotBlank(message = "Email address must not be null, empty, or blank!")
    @Email(message = "Email address is not valid.")
    @Schema(description = "A unique email address with the right format", example = "springishere@example.com")
    private String email;

    @NotBlank(message = "Password must not be null, empty, or blank!")
    @Size(min = 8, message = "Password must be at least 8 characters long!")
    @Schema(description = "Password for the account", example = "12345678", minLength = 8)
    private String password;
}
