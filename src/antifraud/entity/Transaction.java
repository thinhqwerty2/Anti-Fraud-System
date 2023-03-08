package antifraud.entity;


import antifraud.responsebody.FeedbackSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.Hibernate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transaction")
public class Transaction {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    @JsonProperty(value = "transactionId")
    private Long id;

    @NotNull
    private long amount;
    @NotNull
    private String ip;
    @NotNull
    private String number;


    private String result;

    @NotNull
    private Region region;
    @JsonIgnore
    private long amountAllow = 200;
    @JsonIgnore
    private long amountManual = 1500;

    @JsonSerialize(using = FeedbackSerializer.class)
    private Feedback feedback;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    public long getAmountManual() {
        return amountManual;
    }

    public void setAmountManual(long amountManual) {
        this.amountManual = amountManual;
    }

    public long getAmountAllow() {
        return amountAllow;
    }

    public void setAmountAllow(long amountAllow) {
        this.amountAllow = amountAllow;
    }

    public Feedback getFeedback() {
        return feedback;
    }


    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }


    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Transaction that = (Transaction) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
