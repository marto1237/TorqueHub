package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseQuestion {

    private Long id;
    private String title;
    private String description;
    private Long bestAnswerId;
    private int views = 0;
    private int votes = 0;
    private int totalAnswers = 0;
    private int totalComments = 0;
    private JpaUser jpaUser;
    private Set<JpaTag> jpaTags = new HashSet<>();
    private List<JpaAnswer> jpaAnswers = new ArrayList<>();
    private LocalDateTime askedTime;
    private LocalDateTime lastActivityTime = LocalDateTime.now();
}
