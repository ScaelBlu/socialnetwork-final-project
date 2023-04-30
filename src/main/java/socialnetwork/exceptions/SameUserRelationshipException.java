package socialnetwork.exceptions;

public class SameUserRelationshipException extends RuntimeException {

    public SameUserRelationshipException() {
        super("Can not add a user to it's own friend list.");
    }
}
