package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import socialnetwork.exceptions.ValidFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostCommand {

    @NotBlank(message = "The title must not be blank or null.")
    @Schema(description = "A short title of the new post", example = "My first photo")
    private String title;

    @Schema(description = "Longer description below the post", example = "This is my very first photo in this page. I hope you will enjoy it!")
    private String description;

    @NotNull(message = "A photo must be uploaded with the post.")
    @ValidFile
    @Schema(description = "Required image file for the post in JPEG or PNG format")
    private MultipartFile file;
}
