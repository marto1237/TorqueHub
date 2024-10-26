package torquehub.torquehub.domain;

public class ReputationConstants {

    private ReputationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final int POINTS_NEW_QUESTION = 5;
    public static final int POINTS_QUESTION_WHEN_DELETED = -5;
    public static final int POINTS_NEW_ANSWER = 5;
    public  static final int POINTS_ANSWER_WHEN_DELETED = -5;
    public static final int POINTS_UPVOTE_RECEIVED = 2;
    public static final int POINTS_DOWNVOTE_RECEIVED = -2;
    public static final int POINTS_UPVOTE_GIVEN = 1;
    public static final int POINTS_DOWNVOTE_GIVEN = -1;
    public static final int POINTS_BEST_ANSWER = 10;
    public static final int POINTS_BEST_ANSWER_WHEN_DELETED = -10;
    public static final int POINTS_CONSECUTIVE_ACTIVITY = 10;
    public static final int POINTS_NEW_COMMENT = 1;
    public static final int POINTS_COMMENT_WHEN_DELETED = -1;
    public static final int POINTS_UPVOTE_COMMENT = 1;
    public static final int POINTS_DOWNVOTE_COMMENT = -1;
}
