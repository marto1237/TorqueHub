package torquehub.torquehub.domain.model.plainModels;

import torquehub.torquehub.domain.model.Comment;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.model.Vote;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseEntity {
    private Long id;
    private String text;
    private Question question;
    private List<Comment> comments = new ArrayList<>();
    private User user;
    private int votes = 0;
    private Set<Vote> votesList = new HashSet<>();
    private LocalDateTime answeredTime;
    private boolean isEdited = false;
}
