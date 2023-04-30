package socialnetwork.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import socialnetwork.exceptions.NoSuchRelationshipException;
import socialnetwork.utils.DtoMapper;
import socialnetwork.models.User;
import socialnetwork.exceptions.EntityNotFoundException;
import socialnetwork.exceptions.SameUserRelationshipException;
import socialnetwork.repositories.UserRepository;
import socialnetwork.dtos.RelationshipDto;

@Service
@AllArgsConstructor
public class RelationshipService {

    private UserRepository userRepository;

    private DtoMapper mapper;


    @Transactional
    public RelationshipDto saveRelationship(long userId, long friendId) {
        if(userId == friendId) {
            throw new SameUserRelationshipException();
        }
        User user = userRepository.findUserWithFriendsById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        User friend = userRepository.findUserWithFriendsById(friendId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, friendId));
        user.addFriend(friend);
        return mapper.userToRelationship(user);
    }


    @Transactional
    public void removeRelationship(long userId, long friendId) {
        User user = userRepository.findUserWithFriendsById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        User friend = userRepository.findUserWithFriendsById(friendId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, friendId));
        if(user.getFriends().contains(friend)) {
            user.getFriends().remove(friend);
            friend.getFriends().remove(user);
        } else {
            throw new NoSuchRelationshipException(userId, friendId);
        }
    }

    public RelationshipDto listFriendsOfUser(long userId) {
        return mapper.userToRelationship(userRepository.findUserWithFriendsById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId)));
    }
}
