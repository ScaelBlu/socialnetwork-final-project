package socialnetwork.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import socialnetwork.utils.TimeMachine;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@SecondaryTable(name = "personal_data", pkJoinColumns = @PrimaryKeyJoinColumn(name = "user_id"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    @Embedded
    private PersonalData personalData;

    @Column(name = "registered_on")
    private LocalDateTime registrationTime;

    @ManyToMany
    @JoinTable(name = "users_to_users",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id"))
    private Set<User> friends = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<Post> posts = new HashSet<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @PrePersist
    private void setRegistrationTime() {
        if(TimeMachine.isSet()) {
            this.registrationTime = TimeMachine.now();
        } else {
            this.registrationTime = LocalDateTime.now();
        }
    }

    public void addFriend(User user) {
        this.friends.add(user);
        user.friends.add(this);
    }
}
