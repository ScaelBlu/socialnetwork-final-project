package socialnetwork.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import socialnetwork.services.RelationshipService;
import socialnetwork.dtos.RelationshipDto;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "Relationship", description = "Endpoints for the operations with relationships")
public class RelationshipController {

    private RelationshipService relationshipService;

    @Operation(
            summary = "Add new relationship",
            description = "An empty PUT request sent to this endpoint can connect two existing users by their ID."
    )
    @PutMapping(value = "/{userId}/{friendId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "201", description = "The new relationship has been added to both users.")
    @ApiResponse(responseCode = "404",
            description = "One or both of the users do not exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/users/1/42\"}"
                    )
            )
    )
    @ApiResponse(responseCode = "406",
            description = "The user can not create relationship with itself.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/same-user-relationship\",\"title\":\"Not Acceptable\",\"status\":406,\"detail\":\"Can not add a user to it's own friend list.\",\"instance\":\"/api/users/1/1\"}"
                    )
            )
    )
    public ResponseEntity<RelationshipDto> saveRelationship(@Parameter(description = "The ID of the user to whom the friend is to be added.") @PathVariable long userId, @Parameter(description = "The ID of the friend to whom the user is to be added.") @PathVariable long friendId, UriComponentsBuilder builder) {
        RelationshipDto dto = relationshipService.saveRelationship(userId, friendId);
        URI uri = builder.path("/api/users/{userId}/{friendId}").buildAndExpand(userId, friendId).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @Operation(
            summary = "Delete relationship",
            description = "The existing relationships can be removed as a result of DELETE requests sent to this endpoint with the ID's of the users included in the URL."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}/{friendId}")
    @ApiResponse(responseCode = "204", description = "The relationship has been successfully removed from both users' accounts.")
    @ApiResponse(responseCode = "404",
            description = "One or both of the users or the relationship does not exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"There is no relationship between users with ID 1 and 2.\",\"instance\":\"/api/users/1/2\"}"
                    )
            )
    )
    public void removeRelationship(@Parameter(description = "The ID of the user whose list the friend is to be removed from.") @PathVariable long userId, @Parameter(description = "The ID of the friend whose list the user is to be removed from.") @PathVariable long friendId) {
        relationshipService.removeRelationship(userId, friendId);
    }

    @Operation(
            summary = "Listing relationships",
            description = "It lists the relationships of a given user in the response."
    )
    @GetMapping(value = "/{userId}/friends", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "The requested relationships are listed successfully.")
    @ApiResponse(responseCode = "404",
            description = "The user doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/users/42/friends\"}"
                    )
            )
    )
    public RelationshipDto listFriendsOfUser(@Parameter(description = "The ID of the user whose relationships to be listed") @PathVariable long userId) {
        return relationshipService.listFriendsOfUser(userId);
    }
}
