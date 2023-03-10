package antifraud.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Feedback {
    EMPTY(""), ALLOWED("ALLOWED"), MANUAL_PROCESSING("MANUAL_PROCESSING"), PROHIBITED("PROHIBITED");

    private String feedback;

    Feedback(String s) {
    }

    @JsonValue
    public String getInfo(){
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
