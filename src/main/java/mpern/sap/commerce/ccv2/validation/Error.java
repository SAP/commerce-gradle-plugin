package mpern.sap.commerce.ccv2.validation;

import java.util.StringJoiner;

public class Error {
    public final Level level;
    public final String location;
    public final String message;
    public final String code;

    private Error(Level level, String location, String message, String code) {
        this.level = level;
        this.location = location;
        this.message = message;
        this.code = code;
    }

    public Level getLevel() {
        return level;
    }

    public String getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Error.class.getSimpleName() + "[", "]").add("level=" + level)
                .add("location='" + location + "'").add("message='" + message + "'").add("code='" + code + "'")
                .toString();
    }

    public static class Builder {
        private Level level = Level.ERROR;
        private String location;
        private String message;
        private String code;

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder setLocation(String format, Object... args) {
            this.location = String.format(format, args);
            return this;
        }

        public Builder setMessage(String format, Object... args) {
            this.message = String.format(format, args);
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Error createError() {
            return new Error(level, location, message, code);
        }
    }
}
