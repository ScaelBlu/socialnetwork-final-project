package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifyUserCommand {

    @NotBlank(message = "Email address must not be null, empty, or blank!")
    @Email(message = "Email address is not valid.")
    @Schema(description = "The new email address for the account", example = "myaddress@example.com")
    private String email;

    @NotBlank(message = "Password must not be null, empty, or blank!")
    @Size(min = 8, message = "Password must be at least 8 characters long!")
    @Schema(description = "The new password for the account", example = "Abcd1234")
    private String password;
}
