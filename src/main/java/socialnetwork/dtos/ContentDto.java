package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {

    @Schema(description = "The name of the file", example = "example.png")
    private String filename;

    @Schema(description = "The MIME type of the image", example = "image/png")
    private String mimeType;

    @Schema(description = "The binary content")
    private byte[] content;
}
