package socialnetwork.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import socialnetwork.dtos.CreateUserCommand;
import socialnetwork.dtos.ModifyUserCommand;
import socialnetwork.dtos.ModifyPersonalDataCommand;
import socialnetwork.dtos.UserDto;
import socialnetwork.models.RequestParameter;
import socialnetwork.utils.TimeMachine;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = {
        "DELETE FROM personal_data",
        "DELETE FROM users_to_users",
        "DELETE FROM users"
        }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserControllerWebClientIT {

    @Autowired
    private WebTestClient client;

    @LocalServerPort
    private String port;

    @Test
    public void testRegistrationAndDelete() {
        TimeMachine.set(LocalDateTime.parse("2023-04-09T16:45:00"));

        WebTestClient.ResponseSpec response = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "springishere@gmail.com", "12345678"))
                .exchange();

        UserDto dto = response
                .expectStatus().isCreated()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();

        assertAll(
                () -> assertEquals("lifelover", dto.getUsername()),
                () -> assertEquals(LocalDateTime.parse("2023-04-09T16:45:00"), dto.getRegistrationTime()),
                () -> assertNotNull(dto.getId())
        );

        response.expectHeader().location("http://localhost:" + port + "/api/users/" + dto.getId());

        TimeMachine.clear();
    }

    @Test
    public void testGetUserById() {
        long id = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "springishere@gmail.com", "12345678"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        UserDto dto = client.get()
                .uri("/api/users/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("lifelover", dto.getUsername());
    }

    @Test
    public void testGetNonExistingUser() {
        ProblemDetail problem = client.get()
                .uri("/api/users/42")
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertEquals(404, problem.getStatus());
        assertEquals(URI.create("socialnetwork/not-found"), problem.getType());
        assertEquals("User with id: 42 was not found.", problem.getDetail());
    }

    @Test
    public void testDeleteExistingUser() {
        long id = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "springishere@gmail.com", "12345678"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        client.delete()
                .uri("/api/users/{id}", id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        ProblemDetail problem = client.get()
                .uri("/api/users/{id}", id)
                .exchange()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertEquals(404, problem.getStatus());
        assertEquals(URI.create("socialnetwork/not-found"), problem.getType());
        assertEquals("User with id: " + id + " was not found.", problem.getDetail());
    }

    @Test
    public void testDeleteNonExistingUser() {
        ProblemDetail problem = client.delete()
                .uri("/api/users/42")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertEquals(404, problem.getStatus());
        assertEquals(URI.create("socialnetwork/not-found"), problem.getType());
        assertEquals("User with id: 42 was not found.", problem.getDetail());
    }

    @Test
    public void testRegistrationWithDuplicatedData() {
        client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "springishere@gmail.com", "12345678"))
                .exchange();

        ProblemDetail problem = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "othermail@gmail.com", "12345678"))
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertEquals(406, problem.getStatus());
        assertEquals(URI.create("socialnetwork/invalid-arguments"), problem.getType());
    }

    @Test
    public void testRegistrationWithInvalidData() {
        ProblemDetail problem = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "othermail@gmail.com", "12345"))
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertEquals(406, problem.getStatus());
        assertEquals(URI.create("socialnetwork/invalid-arguments"), problem.getType());
        assertEquals("Password must be at least 8 characters long!", problem.getDetail());
    }

    @Test
    public void testModifyingPersonalData() {
        long id = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "springishere@gmail.com", "12345678"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        UserDto dto = client.put()
                .uri("/api/users/{id}/personal", id)
                .bodyValue(new ModifyPersonalDataCommand("Gipsz Jakab", LocalDate.parse("1992-03-23"), null))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();

        assertEquals(id, dto.getId());
        assertEquals("Gipsz Jakab", dto.getPersonalData().getRealName());
        assertNull(dto.getPersonalData().getCity());
    }

    @Test
    public void testModifyingUser() {
        long id = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "springishere@gmail.com", "12345678"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        UserDto dto = client.put()
                .uri("/api/users/{id}", id)
                .bodyValue(new ModifyUserCommand("putonyourspringboots@gmail.com", "12345678"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();

        assertEquals(id, dto.getId());
        assertEquals("putonyourspringboots@gmail.com", dto.getEmail());
    }

    @Test
    public void testListingUsersByParams() {
        //user1
        TimeMachine.set(LocalDateTime.parse("2023-04-11T15:30:00"));
        client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("lifelover", "springishere@gmail.com", "12345678"))
                .exchange();
        //user2
        TimeMachine.set(LocalDateTime.parse("2023-04-11T16:00:00"));
        client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("muddyboots", "putonyourspringboots@gmail.com", "12345678"))
                .exchange();
        //user3
        TimeMachine.set(LocalDateTime.parse("2023-04-11T16:30:00"));
        client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("springboots", "codingisfun@gmail.com", "12345678"))
                .exchange();

        client.get()
                .uri("/api/users")
                .exchange()
                .expectBodyList(UserDto.class)
                .value(l -> assertThat(l)
                        .hasSize(3)
                        .extracting(UserDto::getUsername)
                        .containsOnly("lifelover", "muddyboots", "springboots"));

        client.get()
                .uri("/api/users?username=boot")
                .exchange()
                .expectBodyList(UserDto.class)
                .value(l -> assertThat(l)
                        .hasSize(2)
                        .extracting(UserDto::getUsername)
                        .containsOnly("muddyboots", "springboots"));

        client.get()
                .uri("/api/users?username=boot&registeredAfter=2023-04-11T16:10:00")
                .exchange()
                .expectBodyList(UserDto.class)
                .value(l -> assertThat(l)
                        .hasSize(1)
                        .extracting(UserDto::getUsername)
                        .containsOnly("springboots"));

        TimeMachine.clear();
    }
}
