package socialnetwork.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import socialnetwork.dtos.CreateUserCommand;
import socialnetwork.dtos.RelationshipDto;
import socialnetwork.dtos.UserDto;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = {
        "INSERT INTO users (id, username, email, password, registered_on) VALUES (1, 'lifelover', 'springishere@gmail.com', '39d1da1f4f9fda75ac2c0b29b76c2149fe57256e3240ce35e1e74d6b6d898222', '2023-04-10 15:00:00')",
        "INSERT INTO users (id, username, email, password, registered_on) VALUES (2, 'muddyboots', 'putonyourspringboots@gmail.com', '39d1da1f4f9fda75ac2c0b29b76c2149fe57256e3240ce35e1e74d6b6d898222', '2023-04-10 16:00:00')"
})
@Sql(statements = {
        "DELETE FROM personal_data",
        "DELETE FROM users_to_users",
        "DELETE FROM users"
        }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RelationshipControllerWebClientIT {

    @Autowired
    private WebTestClient client;

    @LocalServerPort
    private String port;

    @Test
    public void testSaveRelationship() {
        client.put()
                .uri("/api/users/1/2")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("http://localhost:" + port + "/api/users/1/2")
                .expectBody(RelationshipDto.class)
                .value(dto -> assertEquals(1L, dto.getUserId()))
                .value(dto -> assertThat(dto.getFriends())
                        .hasSize(1)
                        .extracting(UserDto::getId, UserDto::getUsername)
                        .containsExactly(tuple(2L, "muddyboots"))
                );
    }

    @Test
    public void testSaveRelationWithBadId() {
        client.put()
                .uri("/api/users/1/42")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(ProblemDetail.class)
                .value(pd -> assertAll(
                        () -> assertEquals("User with id: 42 was not found.", pd.getDetail()),
                        () -> assertEquals(URI.create("socialnetwork/not-found"), pd.getType())
                ));
    }

    @Test
    public void testSaveSameUserRelationship() {
        client.put()
                .uri("/api/users/1/1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(ProblemDetail.class)
                .value(pd -> assertAll(
                        () -> assertEquals("Can not add a user to it's own friend list.", pd.getDetail()),
                        () -> assertEquals(URI.create("socialnetwork/same-user-relationship"), pd.getType())
                ));
    }

    @Test
    public void testRemoveRelationship() {
        client.put()
                .uri("/api/users/1/2")
                .exchange();

        client.delete()
                .uri("/api/users/1/2")
                .exchange();

        client.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(dto -> assertThat(dto.getFriends())
                        .isEmpty());
    }

    @Test
    public void testRemoveNonExistingRelationship() {
        client.delete()
                .uri("/api/users/1/2")
                .exchange()
                .expectBody(ProblemDetail.class)
                .value(pd -> assertAll(
                        () -> assertEquals("There is no relationship between users with ID 1 and 2.", pd.getDetail()),
                        () -> assertEquals(URI.create("socialnetwork/not-found"), pd.getType())
                ));
    }

    @Test
    public void testListFriendsOfUser() {
        long id = client.post()
                .uri("/api/users")
                .bodyValue(new CreateUserCommand("springboots", "codingisfun@gmail.com", "12345678"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody()
                .getId();

        client.put()
                .uri("/api/users/1/2")
                .exchange();

        client.put()
                .uri("/api/users/1/" + id)
                .exchange();

        client.get()
                .uri("/api/users/1/friends")
                .exchange()
                .expectStatus().isOk()
                .expectBody(RelationshipDto.class)
                .value(dto -> assertThat(dto.getFriends())
                        .hasSize(2)
                        .extracting(UserDto::getUsername)
                        .containsOnly("muddyboots", "springboots"));

    }
}