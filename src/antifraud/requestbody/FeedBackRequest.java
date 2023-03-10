package antifraud.requestbody;

import antifraud.entity.Feedback;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class FeedBackRequest {
    @NotNull
    Long transactionId;
    @Pattern(regexp = "^(ALLOWED|MANUAL_PROCESSING|PROHIBITED)?$|^$")
    String feedback;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
