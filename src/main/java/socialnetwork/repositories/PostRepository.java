package socialnetwork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import socialnetwork.models.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user u LEFT JOIN FETCH u.friends f WHERE f.id = :friendsOf ORDER BY p.postedOn DESC")
    List<Post> listPostsOfFriends(long friendsOf);
}
