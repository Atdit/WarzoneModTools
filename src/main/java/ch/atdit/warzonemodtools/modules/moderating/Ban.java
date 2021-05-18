package ch.atdit.warzonemodtools.modules.moderating;

public class Ban {
    private Quickban.Type type;
    private String reason;

    public Ban(Quickban.Type type, String reason) {
        this.type = type;
        this.reason = reason;
    }

    Quickban.Type getType() {
        return type;
    }
    String getReason() {
        return reason;
    }
}