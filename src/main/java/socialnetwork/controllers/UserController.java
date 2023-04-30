package socialnetwork.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import socialnetwork.models.RequestParameter;
import socialnetwork.services.UserService;
import socialnetwork.dtos.CreateUserCommand;
import socialnetwork.dtos.ModifyUserCommand;
import socialnetwork.dtos.ModifyPersonalDataCommand;
import socialnetwork.dtos.UserDto;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "User", description = "Endpoints for the operations with users")
public class UserController {

    private UserService userService;

    @Operation(
            summary = "Create user",
            description = "This endpoint is for creating a new account. A unique username, unique email address, and a password at least 8 characters are required. It initialize a new record with null values for personal data that can be added later."
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "201", description = "A new user has been created with valid data.")
    @ApiResponse(responseCode = "406",
            description = "The registration failed due to invalid data in the request.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/invalid-arguments\",\"title\":\"Not Acceptable\",\"status\":406,\"detail\":\"Password must be at least 8 characters long!\",\"instance\":\"/api/users\"}"
                    )
            )
    )
    public ResponseEntity<UserDto> registration(@Parameter(description = "The data of the new user") @Valid @RequestBody CreateUserCommand command, UriComponentsBuilder builder) {
        UserDto dto = userService.registration(command);
        URI uri = builder.path("/api/users/{id}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @Operation(
            summary = "Get a user by ID",
            description = "It returns the account data and the personal data of a registered user as a result of a GET request."
    )
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "The user is exist and returned in the response.")
    @ApiResponse(responseCode = "404",
            description = "The user doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/users/42\"}"
                    )
            )
    )
    public UserDto getUserById(@Parameter(description = "The ID of the user") @PathVariable long userId) {
        return userService.getUserById(userId);
    }

    @Operation(
            summary = "Search users",
            description = "This endpoint allows searching among the registered users by 5 parameters: username substring, email address substring, registration after a given time, real name substring, and exact city name."
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "The response has sent back correctly.")
    public List<UserDto> findUsersByParams(@Parameter(description = "Optional query string parameters for the search") RequestParameter params) {
        return userService.findUsersByParams(params);
    }

    @Operation(
            summary = "Delete user",
            description = "It allows to delete an existing user with posts and relationships completely."
    )
    @DeleteMapping(value = "/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", description = "The user has been removed.")
    @ApiResponse(responseCode = "404",
            description = "The user doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/users/42\"}"
                    )
            )
    )
    public void deleteUser(@Parameter(description = "The ID of the user to be removed") @PathVariable long userId) {
        userService.deleteUser(userId);
    }

    @Operation(
            summary = "Modify personal data",
            description = "The personal data (such as real name, date of birth, and city) can be modified with a PUT request."
    )
    @PutMapping(value = "/{userId}/personal", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "The modification of the user's personal data was successful.")
    @ApiResponse(responseCode = "404",
            description = "The user with the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/users/42/personal\"}"
                    )
            )
    )
    @ApiResponse(responseCode = "406",
            description = "The modification failed due to invalid data in the request.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/invalid-arguments\",\"title\":\"Not Acceptable\",\"status\":406,\"detail\":\"Password must be at least 8 characters long!\",\"instance\":\"/api/users/1/personal\"}"
                    )
            )
    )
    public UserDto modifyPersonalData(@Parameter(description = "The ID of the user whose personal data to be modified") @PathVariable long userId, @Parameter(description = "The new values of the user's personal data") @RequestBody @Valid ModifyPersonalDataCommand command) {
        return userService.modifyPersonalData(userId, command);
    }

    @Operation(
            summary = "Modify account data",
            description = "The email address and password can be modified with correct data in a PUT request."
    )
    @PutMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "The modification of the user's account data was successful.")
    @ApiResponse(responseCode = "404",
            description = "The user with the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/users/42\"}"
                    )
            )
    )
    @ApiResponse(responseCode = "406",
            description = "The modification failed due to invalid data in the request.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/invalid-arguments\",\"title\":\"Not Acceptable\",\"status\":406,\"detail\":\"(conn=87) Duplicate entry 'lifelover' for key 'username'\",\"instance\":\"/api/users/1\"}"
                    )
            )
    )
    public UserDto modifyUser(@Parameter(description = "The ID of the user whose account data to be modified") @PathVariable long userId, @Parameter(description = "The new values of the user's account data") @RequestBody ModifyUserCommand command) {
        return userService.modifyUser(userId, command);
    }
}
