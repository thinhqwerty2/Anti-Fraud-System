package antifraud.service;

import antifraud.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    private final IpService ipService;
    private final CardNumberService cardNumberService;

    @Autowired
    public TransactionService(IpService ipService, CardNumberService cardNumberService) {
        this.ipService = ipService;
        this.cardNumberService = cardNumberService;
    }

    public String reasonReject(Transaction transaction) {
        List<String> rs = new ArrayList<>();
        if (transaction.getAmount() > 1500) {
            rs.add("amount");
        }
        if (cardNumberService.findByNumber(transaction.getNumber()) != null) {
            rs.add("card-number");
        }
        if (ipService.findByIp(transaction.getIp()) != null) {
            rs.add("ip");
        }
        return String.join(", ", rs);
    }

    public boolean isBadRequest(Transaction transaction) {
        if (transaction.getAmount() <= 0) {
            System.out.println("Amount");

            return true;
        }
        if (!ipService.isCorrectFormat(transaction.getIp())) {
            System.out.println("IP");
            System.out.println(transaction.getIp());
            return true;
        }
        if (!cardNumberService.isCorrectFormat(transaction.getNumber()))
        {
            System.out.println("Number");

            return true;
        }

        return false;
    }


}
