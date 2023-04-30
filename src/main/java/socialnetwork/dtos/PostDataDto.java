package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDataDto {

    @Schema(description = "The ID of the post", example = "1")
    private long id;

    @Schema(description = "Short title of the post", example = "My first photo")
    private String title;

    @Schema(description = "Longer description below the post", example = "This is my very first photo in this page and I hope you will enjoy it.")
    private String description;

    @Schema(description = "The name of the file", example = "example.png")
    private String filename;

    @Schema(description = "The posting time", example = "2023-03-23T15:30:00")
    private LocalDateTime postedOn;

    @Schema(description = "Thr ID of the user", example = "1")
    private Long userId;
}
