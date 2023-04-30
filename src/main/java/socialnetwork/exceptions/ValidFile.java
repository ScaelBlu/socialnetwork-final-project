package socialnetwork.exceptions;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = ContentValidator.class)
public @interface ValidFile {

    String message() default "Only .jpg, .jpeg, .png extensions and image/jpeg, image/png content types are allowed.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
