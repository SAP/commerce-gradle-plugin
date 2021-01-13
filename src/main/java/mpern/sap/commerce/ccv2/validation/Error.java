package mpern.sap.commerce.ccv2.validation;

import java.util.StringJoiner;

public class Error {
    public final Level level;
    public final String location;
    public final String message;
    public final String link;

    private Error(Level level, String location, String message, String link) {
        this.level = level;
        this.location = location;
        this.message = message;
        this.link = link;
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

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Error.class.getSimpleName() + "[", "]").add("level=" + level)
                .add("location='" + location + "'").add("message='" + message + "'").add("link='" + link + "'")
                .toString();
    }

    public static class Builder {
        private Level level = Level.ERROR;
        private String location;
        private String message;
        private String link;

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

        public Builder setLink(String link) {
            this.link = link;
            return this;
        }

        public Error createError() {
            return new Error(level, location, message, link);
        }
    }
}
