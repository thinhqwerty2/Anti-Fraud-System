package antifraud.service;

import antifraud.entity.Region;
import antifraud.entity.Transaction;
import antifraud.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionService {
    private final IpService ipService;
    private final CardNumberService cardNumberService;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(IpService ipService, CardNumberService cardNumberService, TransactionRepository transactionRepository) {
        this.ipService = ipService;
        this.cardNumberService = cardNumberService;
        this.transactionRepository = transactionRepository;
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public List<Transaction> findLatestByNumberCard(String number) {
        return transactionRepository.findByNumberOrderByDateDesc(number);
    }

    public List<Transaction> findOneHourBefore(Transaction transaction) {
        return transactionRepository.findByDateBetweenOrderByDateDesc(transaction.getDate().minusHours(1), transaction.getDate());
    }

    public String[] reasonReject(Transaction transaction, List<Transaction> listTransactionBefore) {
        String rs = "ALLOWED";
        List<String> info = new ArrayList<>();
        List<String> indexManual = new ArrayList<>();//Use for delete manual while prohibited
        Set<String> setDistinctIP = new HashSet<>();
        Set<Region> setDistinctRegion = new HashSet<>();
        int countRegionDistinct = 0;
        int countIpDistinct = 0;
        String transactionIP = transaction.getIp();
        Region transactionRegion = transaction.getRegion();
        System.out.println("all"+transactionRepository.findByNumber(transaction.getNumber()));
        Transaction currentLimit = transactionRepository.findTop1ByNumberOrderByDateDesc(transaction.getNumber());
        System.out.println("limit"+currentLimit);
        System.out.println("list"+listTransactionBefore.toString());
        if (currentLimit != null) {

            transaction.setAmountAllow(currentLimit.getAmountAllow());
        }
        if (currentLimit != null) {

            transaction.setAmountManual(currentLimit.getAmountManual());
        }
        rs = checkAmount(transaction);
        if (rs.equals("MANUAL_PROCESSING")) {
            indexManual.add("amount");
        }
        if (transaction.getAmount() > transaction.getAmountAllow()) {
            info.add("amount");
        }
        if (cardNumberService.findByNumber(transaction.getNumber()) != null) {
            info.add("card-number");
            rs = "PROHIBITED";
        }
        if (ipService.findByIp(transaction.getIp()) != null) {
            info.add("ip");
            rs = "PROHIBITED";
        }
        if (listTransactionBefore.size() != 0) {
            for (Transaction item : listTransactionBefore) {
                if (!item.getRegion().equals(transactionRegion)) {
                    if (setDistinctRegion.add(item.getRegion())) {
                        countRegionDistinct++;
                    }
                }
                if (!item.getIp().equals(transactionIP)) {
                    if (setDistinctIP.add(item.getIp())) {
                        countIpDistinct++;
                    }
                }

                if (countIpDistinct >= 2 && countRegionDistinct >= 2) {
                    break;
                }
            }
            System.out.println(setDistinctRegion);

            if (countIpDistinct >= 2) {
                info.add("ip-correlation");
                if (countIpDistinct == 2) {
                    if (!rs.equals("PROHIBITED")) {
                        rs = "MANUAL_PROCESSING";
                    }
                    indexManual.add("ip-correlation");

                } else {
                    rs = "PROHIBITED";
                }

            }
            if (countRegionDistinct >= 2) {
                info.add("region-correlation");
                if (countRegionDistinct == 2) {
                    if (!rs.equals("PROHIBITED")) {
                        rs = "MANUAL_PROCESSING";

                    }
                    indexManual.add("region-correlation");
                } else {
                    rs = "PROHIBITED";
                }
            }
        }

        if (info.size() == 0) {
            info.add("none");
        }
        if (rs.equals("PROHIBITED")) {
            indexManual.forEach(info::remove);
        }
        return new String[]{String.join(", ", info), rs};
    }

    public String checkAmount(Transaction transaction) {
        if (0 < transaction.getAmount() && transaction.getAmount() <= transaction.getAmountAllow()) {
            return "ALLOWED";
        }
        if (transaction.getAmountAllow() < transaction.getAmount() && transaction.getAmount() <= transaction.getAmountManual()) {
            return "MANUAL_PROCESSING";
        }
        if (transaction.getAmount() > transaction.getAmountManual()) {
            return "PROHIBITED";
        }
        return null;
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
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
        if (!cardNumberService.isCorrectFormat(transaction.getNumber())) {
            System.out.println("Number");

            return true;
        }

        return false;
    }


    public List<Transaction> getListFeedBack() {
        return transactionRepository.findAll();
    }


    public List<Transaction> getListFeeBackByNumber(String number) {
        return transactionRepository.findByNumber(number);
    }
    public Transaction findLatestTransaction(String number){
        return transactionRepository.findByNumberOrderByDateDesc(number).get(0);
    }

    public List<Long> updateLimit(Transaction transaction, String feedback) {
        //Here is transaction is validated and wait to feedback
        switch (transaction.getResult() + " " + feedback) {
            case "ALLOWED MANUAL_PROCESSING" ->
                    transaction.setAmountAllow((long) Math.ceil(transaction.getAmountAllow() * 0.8 - 0.2 * transaction.getAmount()));
            case "ALLOWED PROHIBITED" -> {
                transaction.setAmountAllow((long) Math.ceil(transaction.getAmountAllow() * 0.8 - 0.2 * transaction.getAmount()));
                transaction.setAmountManual((long) Math.ceil(transaction.getAmountManual() * 0.8 - 0.2 * transaction.getAmount()));
            }
            case "MANUAL_PROCESSING ALLOWED" ->
                    transaction.setAmountAllow((long) Math.ceil(transaction.getAmountAllow() * 0.8 + 0.2 * transaction.getAmount()));
            case "MANUAL_PROCESSING PROHIBITED" ->
                    transaction.setAmountManual((long) Math.ceil(transaction.getAmountManual() * 0.8 - 0.2 * transaction.getAmount()));
            case "PROHIBITED ALLOWED" -> {
                transaction.setAmountAllow((long) Math.ceil(transaction.getAmountAllow() * 0.8 + 0.2 * transaction.getAmount()));
                transaction.setAmountManual((long) Math.ceil(transaction.getAmountManual() * 0.8 + 0.2 * transaction.getAmount()));
            }
            case "PROHIBITED MANUAL_PROCESSING" ->
                    transaction.setAmountManual((long) Math.ceil(transaction.getAmountManual() * 0.8 + 0.2 * transaction.getAmount()));
        }
        return List.of(transaction.getAmountAllow(),transaction.getAmountManual()  );
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }
}
