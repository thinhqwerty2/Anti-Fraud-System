package antifraud.entity;

import javax.validation.constraints.NotNull;

public class Transaction {
    @NotNull
    private long amount;
    @NotNull
    private String ip;
    @NotNull
    private String number;
    public long getAmount(){
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

}
