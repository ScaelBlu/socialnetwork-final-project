package socialnetwork.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import socialnetwork.dtos.ContentDto;
import socialnetwork.dtos.CreatePostCommand;
import socialnetwork.dtos.PostDataDto;
import socialnetwork.exceptions.EntityNotFoundException;
import socialnetwork.models.Post;
import socialnetwork.models.PostFile;
import socialnetwork.models.User;
import socialnetwork.repositories.PostRepository;
import socialnetwork.repositories.UserRepository;
import socialnetwork.utils.DtoMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@AllArgsConstructor
public class PostService {

    private PostRepository postRepository;

    private UserRepository userRepository;

    private DtoMapper mapper;

    public PostDataDto uploadPost(long userId, CreatePostCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        Post post = new Post(command.getTitle(), command.getDescription(), getPostFile(command));
        post.setUser(user);
        postRepository.save(post);
        return mapper.postToDto(post);
    }

    public ContentDto downloadContent(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
        return mapper.postToContent(post);
    }

    public List<PostDataDto> listOrderedPostsOfFriends(long friendsOf) {
        if(userRepository.existsById(friendsOf)) {
            return mapper.postsToDtoList(postRepository.listPostsOfFriends(friendsOf));
        }
        throw new EntityNotFoundException(User.class, friendsOf);
    }

    public PostDataDto getPostById(long postId) {
        return mapper.postToDto(postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId)));
    }

    public void deletePost(long postId) {
        if(postRepository.existsById(postId)) {
            postRepository.deleteById(postId);
            return;
        }
        throw new EntityNotFoundException(Post.class, postId);
    }

    private PostFile getPostFile(CreatePostCommand command) {
        MultipartFile file = command.getFile();
        try (InputStream stream = file.getInputStream()) {
            return new PostFile(file.getOriginalFilename(), file.getContentType(), stream.readAllBytes());
        } catch (IOException ioe) {
            throw new IllegalStateException("I/O error occurred, when the file was reading.", ioe);
        }
    }
}
