package socialnetwork.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import socialnetwork.dtos.CreateUserCommand;
import socialnetwork.dtos.ModifyUserCommand;
import socialnetwork.dtos.ModifyPersonalDataCommand;
import socialnetwork.dtos.UserDto;
import socialnetwork.exceptions.EntityNotFoundException;
import socialnetwork.models.PersonalData;
import socialnetwork.models.RequestParameter;
import socialnetwork.models.User;
import socialnetwork.repositories.UserRepository;
import socialnetwork.utils.DtoMapper;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;

    private DtoMapper mapper;


    public UserDto registration(CreateUserCommand command) {
        String hashedPassword = new DigestUtils("SHA3-256").digestAsHex(command.getPassword());
        User user = new User(command.getUsername(), command.getEmail(), hashedPassword);
        user.setPersonalData(new PersonalData());
        userRepository.save(user);
        return mapper.userToDto(user);
    }

    public UserDto getUserById(long userId) {
        return mapper.userToDto(userRepository.findUserWithFriendsById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(User.class, userId))
        );
    }

    @Transactional
    public void deleteUser(long userId) {
        User user = userRepository.findUserWithFriendsById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        user.getFriends()
                .forEach(f -> f.getFriends().remove(user));
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserDto modifyPersonalData(long userId, ModifyPersonalDataCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        PersonalData pd = user.getPersonalData();
        pd.setRealName(command.getRealName());
        pd.setDateOfBirth(command.getDateOfBirth());
        pd.setCity(command.getCity());
        return mapper.userToDto(user);
    }

    @Transactional
    public UserDto modifyUser(long userId, ModifyUserCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        user.setEmail(command.getEmail());
        user.setPassword(command.getPassword());
        return mapper.userToDto(user);
    }

    public List<UserDto> findUsersByParams(RequestParameter params) {
        return mapper.usersToDtoList(
                userRepository.findUsersByParams(params.getUsername(), params.getEmail(), params.getRegisteredAfter(), params.getRealName(), params.getCity())
                );
    }
}
