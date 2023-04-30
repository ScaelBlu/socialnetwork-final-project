package socialnetwork.exceptions;


public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> entityClass, long id) {
        super(entityClass.getSimpleName() + " with id: " + id + " was not found.");
    }
}
