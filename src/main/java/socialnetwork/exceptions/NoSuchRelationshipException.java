package socialnetwork.exceptions;

public class NoSuchRelationshipException extends RuntimeException {
    public NoSuchRelationshipException(long userId, long friendId) {
        super("There is no relationship between users with ID " + userId + " and " + friendId + ".");
    }
}
