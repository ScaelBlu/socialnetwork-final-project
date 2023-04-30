package socialnetwork.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import socialnetwork.dtos.PostDataDto;
import socialnetwork.utils.TimeMachine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = {
        "INSERT INTO users (id, username, email, password, registered_on) VALUES (1, 'lifelover', 'springishere@gmail.com', '39d1da1f4f9fda75ac2c0b29b76c2149fe57256e3240ce35e1e74d6b6d898222', '2023-04-10 15:00:00')",
        "INSERT INTO users (id, username, email, password, registered_on) VALUES (2, 'muddyboots', 'putonyourspringboots@gmail.com', '39d1da1f4f9fda75ac2c0b29b76c2149fe57256e3240ce35e1e74d6b6d898222', '2023-04-10 16:00:00')"
})
@Sql(statements = {
        "DELETE FROM files",
        "DELETE FROM posts",
        "DELETE FROM personal_data",
        "DELETE FROM users_to_users",
        "DELETE FROM users"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PostControllerWebClientIT {

    private static MultiValueMap<String, HttpEntity<?>> formData;

    @Autowired
    private WebTestClient client;

    @LocalServerPort
    private String port;

    @BeforeAll
    public static void init() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("title", "Let,s rock!");
        builder.part("description", "This is the message for today");
        builder.part("file", new ClassPathResource("image.jpg"));
        formData = builder.build();
    }


    @Test
    public void testUploadPost() {
        TimeMachine.set(LocalDateTime.parse("2023-04-13T19:00:00"));

        WebTestClient.ResponseSpec response = client.post()
                .uri("/api/posts?userId=1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .exchange();

        PostDataDto dto = response
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isCreated()
                .expectBody(PostDataDto.class)
                .returnResult()
                .getResponseBody();

        assertAll(
                () -> assertEquals("Let,s rock!", dto.getTitle()),
                () -> assertEquals("This is the message for today", dto.getDescription()),
                () -> assertEquals("image.jpg", dto.getFilename()),
                () -> assertEquals(LocalDateTime.parse("2023-04-13T19:00:00"), dto.getPostedOn()),
                () -> assertEquals(1L, dto.getUserId())
        );

        response.expectHeader().location("http://localhost:" + port + "/api/posts/" + dto.getId());

        TimeMachine.clear();
    }

    @Test
    public void testUploadPostWithUnsupportedFile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("title", "Let,s rock!");
        builder.part("description", "This is the message for today");
        builder.part("file", new ClassPathResource("test.txt"));

        client.post()
                .uri("/api/posts?userId=1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                        .jsonPath("type").isEqualTo("socialnetwork/invalid-arguments")
                        .jsonPath("detail").isEqualTo("Only .jpg, .jpeg, .png extensions and image/jpeg, image/png content types are allowed.");
    }

    @Test
    public void testUploadPostWithInvalidArgument() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("image.jpg"));

        client.post()
                .uri("/api/posts?userId=1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                .jsonPath("type").isEqualTo("socialnetwork/invalid-arguments")
                .jsonPath("detail").isEqualTo("The title must not be blank or null.");

    }

    @Test
    public void testUploadPostWithTooBigImage() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("title", "Night sky");
        builder.part("description", "This image is too big");
        builder.part("file", new ClassPathResource("big_image.jpeg"));

        client.post()
                .uri("/api/posts?userId=1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectStatus().isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
                .expectBody()
                .jsonPath("type").isEqualTo("socialnetwork/image-too-large")
                .jsonPath("detail").isEqualTo("The field file exceeds its maximum permitted size of 2097152 bytes.");

    }

    @Test
    public void testLoadPostContent() throws IOException {
        byte[] testfile;
        try(InputStream stream = Files.newInputStream(Path.of("src/test/resources/image.jpg"))) {
            testfile = stream.readAllBytes();
        }

        long postId = client.post()
                .uri("/api/posts?userId=1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectBody(PostDataDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        client.get()
                .uri("/api/posts/" + postId + "/content")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG)
                .expectBody(byte[].class)
                .value(body -> assertArrayEquals(testfile, body));
    }

    @Test
    public void testLoadNonExistingPostContent() {
        client.get()
                .uri("/api/posts/42/content")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(ProblemDetail.class)
                .value(pd -> assertAll(
                        () -> assertEquals("Post with id: 42 was not found.", pd.getDetail()),
                        () -> assertEquals(URI.create("socialnetwork/not-found"), pd.getType())
                ));
    }

    @Test
    public void testListOrderedPostsOfFriends() {
        client.put()
                .uri("/api/users/1/2")
                .exchange();

        TimeMachine.set(LocalDateTime.parse("2023-04-10T10:15:00"));
        long id1 = client.post()
                .uri("/api/posts?userId=2")
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectBody(PostDataDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        TimeMachine.set(LocalDateTime.parse("2023-04-10T10:00:00"));
        long id2 = client.post()
                .uri("/api/posts?userId=2")
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectBody(PostDataDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        TimeMachine.set(LocalDateTime.parse("2023-04-10T10:30:00"));
        long id3 = client.post()
                .uri("/api/posts?userId=2")
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectBody(PostDataDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        client.get()
                .uri("api/posts?friendsOf=1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PostDataDto.class)
                .value(list -> assertThat(list)
                        .hasSize(3)
                        .extracting(PostDataDto::getId)
                        .containsExactly(id3, id1, id2));

        TimeMachine.clear();
    }

    @Test
    public void testGetPostById() {
        TimeMachine.set(LocalDateTime.parse("2023-04-16T20:00:00"));
        long id = client.post()
                .uri("/api/posts?userId=1")
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectBody(PostDataDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        client.get()
                .uri("/api/posts/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PostDataDto.class)
                .value(dto -> assertAll(
                        () -> assertEquals(1L, dto.getUserId()),
                        () -> assertEquals("image.jpg", dto.getFilename()),
                        () -> assertEquals(LocalDateTime.parse("2023-04-16T20:00:00"), dto.getPostedOn())
                ));

        TimeMachine.clear();
    }

    @Test
    public void testDeletePost() {
        long id = client.post()
                .uri("/api/posts?userId=1")
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectBody(PostDataDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        client.delete()
                .uri("/api/posts/" + id)
                .exchange();

        client.get()
                .uri("/api/posts/" + id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ProblemDetail.class)
                .value(pd -> assertEquals("Post with id: " + id + " was not found.", pd.getDetail()));
    }
}