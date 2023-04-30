package socialnetwork.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PostFile {

    @Column(table = "files")
    private String filename;

    @Column(table = "files", name = "mime_type")
    private String mimeType;

    @Column(table = "files")
    private byte[] content;
}
