package socialnetwork.utils;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import socialnetwork.dtos.PostDataDto;
import socialnetwork.models.Post;
import socialnetwork.models.User;
import socialnetwork.dtos.RelationshipDto;
import socialnetwork.dtos.UserDto;
import socialnetwork.dtos.ContentDto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DtoMapper {

    UserDto userToDto(User user);

    List<UserDto> usersToDtoList(List<User> users);

    @Mapping(source = "postFile.mimeType", target = "mimeType")
    @Mapping(source = "postFile.content", target = "content")
    @Mapping(source = "postFile.filename", target = "filename")
    ContentDto postToContent(Post post);

    List<PostDataDto> postsToDtoList(List<Post> posts);

    @Mapping(source = "id", target = "userId")
    RelationshipDto userToRelationship(User user);

    @Mapping(source = "postFile.filename", target = "filename")
    @Mapping(source = "user.id", target = "userId")
    PostDataDto postToDto(Post post);

    default Set<Long> extractId(Set<User> friends) {
        return friends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }
}
