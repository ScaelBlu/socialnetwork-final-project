package socialnetwork.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import socialnetwork.utils.TimeMachine;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts")
@SecondaryTable(name = "files", pkJoinColumns = @PrimaryKeyJoinColumn(name = "post_id"))
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Embedded
    private PostFile postFile;

    @Column(name = "posted_on")
    private LocalDateTime postedOn;

    @ManyToOne
    private User user;

    public Post(String title, String description, PostFile postFile) {
        this.title = title;
        this.description = description;
        this.postFile = postFile;
    }

    @PrePersist
    public void setPostTime() {
        if(TimeMachine.isSet()) {
            this.postedOn = TimeMachine.now();
        } else {
            this.postedOn = LocalDateTime.now();
        }
    }
}
