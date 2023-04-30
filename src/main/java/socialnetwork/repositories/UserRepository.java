package socialnetwork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import socialnetwork.models.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
            SELECT u FROM User u LEFT JOIN FETCH u.friends
            WHERE (:username IS NULL OR u.username LIKE %:username%)
            AND (:email IS NULL OR u.email LIKE %:email%) 
            AND (u.registrationTime >= COALESCE(:registeredAfter, u.registrationTime)) 
            AND (:realName IS NULL OR u.personalData.realName LIKE %:realName%) 
            AND (:city IS NULL OR u.personalData.city = :city)
            """)
    List<User> findUsersByParams(String username, String email, LocalDateTime registeredAfter, String realName, String city);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.friends f LEFT JOIN FETCH f.friends WHERE u.id = :userId")
    Optional<User> findUserWithFriendsById(long userId);
}
