package socialnetwork.exceptions;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ContentValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        String filename = value.getOriginalFilename().toLowerCase();
        String mimeType = value.getContentType();
        return (mimeType.equals("image/jpeg") && (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))) ||
                (mimeType.equals("image/png") && filename.endsWith(".png"));
    }

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
