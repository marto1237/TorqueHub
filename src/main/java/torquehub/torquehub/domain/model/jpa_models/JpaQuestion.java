package torquehub.torquehub.domain.model.jpa_models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.plain_models.BaseQuestion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "questions")
public class JpaQuestion extends BaseQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Lob
    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @NotBlank
    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    private Long bestAnswerId;

    private int views = 0;
    private int votes = 0;
    private int totalAnswers = 0;
    private int totalComments = 0;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private JpaUser jpaUser;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<JpaTag> jpaTags = new HashSet<>();

    @OneToMany(mappedBy = "jpaQuestion", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<JpaAnswer> jpaAnswers = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime askedTime;

    @Column(nullable = false)
    private LocalDateTime lastActivityTime = LocalDateTime.now();
}
