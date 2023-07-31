package mpern.sap.commerce.ccv2.validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.Tuple2;

public class ValidationUtils {

    private static final Pattern VALID_PATH = Pattern.compile("[.a-zA-Z/_\\-0-9]+");
    public static final String ERROR = "E-009";

    public static Tuple2<Path, List<Error>> validateAndNormalizePath(Path projectRoot, String location, String input) {
        Matcher matcher = VALID_PATH.matcher(input);
        if (!matcher.matches()) {
            return new Tuple2<>(null, Collections.singletonList(new Error.Builder().setLocation(location).setMessage(
                    "Location `%s` is invalid. Must be a plain Unix-style path without any shell expansion, non-ASCII characters etc.",
                    input).setCode(ERROR).createError()));
        } else {
            List<Error> errors = new ArrayList<>();
            Path inputPath = Path.of(input);
            if (inputPath.isAbsolute() || inputPath.startsWith("/")) {
                errors.add(new Error.Builder().setLocation(location)
                        .setMessage("Location `%s` is absolute (starts with `/`).", input).setCode(ERROR)
                        .createError());
            }
            for (Path component : inputPath) {
                if (".".equals(component.toString()) || "..".equals(component.toString())) {
                    errors.add(new Error.Builder().setLocation(location)
                            .setMessage("Location `%s` is relative (uses `.` or `..`).", input).setCode(ERROR)
                            .createError());
                }
            }
            Path resolved = null;
            if (errors.isEmpty()) {
                resolved = projectRoot.resolve(inputPath);
                if (!Files.exists(resolved)) {
                    errors.add(new Error.Builder().setLocation(location).setMessage("Location `%s` not found", input)
                            .setCode(ERROR).createError());
                    resolved = null;
                }
            }
            return new Tuple2<>(resolved, errors);
        }
    }
}
