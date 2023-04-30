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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import socialnetwork.dtos.CreatePostCommand;
import socialnetwork.dtos.PostDataDto;
import socialnetwork.services.PostService;
import socialnetwork.dtos.ContentDto;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@Tag(name = "Post", description = "Endpoints for the operations with posts")
public class PostController {

    private PostService postService;

    @Operation(
            summary = "Create post",
            description = "This endpoint is for uploading new posts. The title and the file content are required. Max. 2 MB size .jpeg/.jpg or .png images are allowed.\""
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "201", description = "A new post has been created with valid data and successfully added to user.")
    @ApiResponse(responseCode = "404",
            description = "The user doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/posts\"}"
                    )
            )
    )
    @ApiResponse(responseCode = "406",
            description = "The creation of the new post failed due to invalid data or file type in the request.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/invalid-arguments\",\"title\":\"Not Acceptable\",\"status\":406,\"detail\":\"Only .jpg, .jpeg, .png extensions and image/jpeg, image/png content types are allowed.\",\"instance\":\"/api/posts\"}"
                    )
            )
    )
    @ApiResponse(responseCode = "413",
            description = "The request and/or file size has exceeded the 2 MB maximum limit.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/image-too-large\",\"title\":\"Payload Too Large\",\"status\":413,\"detail\":\"The field file exceeds its maximum permitted size of 2097152 bytes.\",\"instance\":\"/api/posts\"}"
                    )
            )
    )
    public ResponseEntity<PostDataDto> createPost(@Parameter(description = "The ID of the user who the new post belongs to") @RequestParam long userId, @Parameter(description = "The contents of the new post") @Valid @ModelAttribute CreatePostCommand command, UriComponentsBuilder builder) {
        PostDataDto dto =  postService.uploadPost(userId, command);
        URI uri = builder.path("/api/posts/{postId}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @Operation(
            summary = "Load post content",
            description = "It sends back the uploaded image of a given post with inline content disposition."
    )
    @GetMapping(value = "/{postId}/content", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @ApiResponse(responseCode = "200", description = "The content was successfully sent back in the response.")
    @ApiResponse(responseCode = "404",
            description = "The post with the given ID doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"Post with id: 42 was not found.\",\"instance\":\"/api/posts/42/content\"}"
                    )
            )
    )
    public ResponseEntity<byte[]> loadPostContent(@Parameter(description = "The ID of the post") @PathVariable long postId) {
        ContentDto dto = postService.downloadContent(postId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.inline().filename(dto.getFilename()).build());
        headers.setContentType(MediaType.valueOf(dto.getMimeType()));
        return ResponseEntity.ok().headers(headers).body(dto.getContent());
    }

    @Operation(
            summary = "Listing posts of friends",
            description = "The server lists the posts of a given user's friends as a result of a GET request. The posts are sent back in the response ordered by the upload time descending."
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "The requested posts were listed successfully.")
    @ApiResponse(responseCode = "404",
            description = "The user with the given ID doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User with id: 42 was not found.\",\"instance\":\"/api/posts\"}"
                    )
            )
    )
    public List<PostDataDto> listOrderedPostsOfFriends(@Parameter(description = "The ID of the user whose friend's posts are requested") @RequestParam long friendsOf) {
        return postService.listOrderedPostsOfFriends(friendsOf);
    }

    @Operation(
            summary = "Get post by ID",
            description = "A given post will be sent back in the response without the content."
    )
    @GetMapping(value = "/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "The post exists, and sent back in the response.")
    @ApiResponse(responseCode = "404",
            description = "The post with the given ID doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"Post with id: 42 was not found.\",\"instance\":\"/api/posts/42\"}"
                    )
            )
    )
    public PostDataDto getPostById(@Parameter(description = "The ID of the post") @PathVariable long postId) {
        return postService.getPostById(postId);
    }

    @Operation(
            summary = "Delete post",
            description = "It removes a post completely."
    )
    @DeleteMapping("/{postId}")
    @ApiResponse(responseCode = "204", description = "The post has been removed.")
    @ApiResponse(responseCode = "404",
            description = "The post with the given ID doesn't exist.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            value = "{\"type\":\"socialnetwork/not-found\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"Post with id: 42 was not found.\",\"instance\":\"/api/posts/42\"}"
                    )
            )
    )
    public void deletePost(@Parameter(description = "The ID of the post to be removed") @PathVariable long postId) {
        postService.deletePost(postId);
    }
}
