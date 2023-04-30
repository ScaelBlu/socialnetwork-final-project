package socialnetwork.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonalData {

    @Column(table = "personal_data", name = "real_name")
    @Schema(description = "The real name of the user", example = "John Doe")
    private String realName;

    @Column(table = "personal_data", name = "date_of_birth")
    @Schema(description = "The date of birth of the user", example = "2002-03-04")
    private LocalDate dateOfBirth;

    @Column(table = "personal_data")
    @Schema(description = "The name of the city where the user is located", example = "Budapest")
    private String city;
}
