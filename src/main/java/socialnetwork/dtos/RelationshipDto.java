package socialnetwork.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RelationshipDto {

    @Schema(description = "The ID of the user who the relationships belong to", example = "1")
    private long userId;

    @Schema(description = "The list of related users")
    private Set<UserDto> friends;
}
