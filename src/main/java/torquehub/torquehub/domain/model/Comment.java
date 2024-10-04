package torquehub.torquehub.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    @NotBlank
    @Lob
    @Column(name = "text", columnDefinition = "LONGTEXT")
    private String text;

    @Column(nullable = false)
    private int votes = 0;

    @Column(nullable = false)
    private LocalDateTime commentedTime;

    @Column(nullable = false)
    private boolean isEdited = false;

}
