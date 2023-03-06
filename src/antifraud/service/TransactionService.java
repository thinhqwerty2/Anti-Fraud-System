package antifraud.service;

import antifraud.entity.Region;
import antifraud.entity.Transaction;
import antifraud.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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

    public List<Transaction> findLatestByNumberCard(String number) {
        return transactionRepository.findByNumberOrderByDateDesc(number);
    }

    public String[] reasonReject(Transaction transaction, List<Transaction> listTransactionBefore) {
        String rs = "ALLOWED";
        List<String> info = new ArrayList<>();
        List<String> indexManual = new ArrayList<>();
        Set<String> setDistinctIP = new HashSet<>();
        Set<String> setDistinctRegion = new HashSet<>();
        int countRegionDistinct = 0;
        int countIpDistinct = 0;
        String transactionIP = transaction.getIp();
        Region transactionRegion = transaction.getRegion();
        LocalDateTime transactionDateTime = transaction.getDate();
        rs = checkAmount(transaction);
        if (rs.equals("MANUAL_PROCESSING")) {
            indexManual.add("amount");
        }
        if (transaction.getAmount() > 200) {
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
        System.out.println(transaction);
        if (listTransactionBefore.size() != 0) {
            for (Transaction item : listTransactionBefore) {
                System.out.println(item);
                System.out.println(Duration.between(item.getDate(),transactionDateTime).toSeconds());
                if (Math.abs(Duration.between(item.getDate(),transactionDateTime).toSeconds())<= 3600) {
                    if (!item.getRegion().equals(transactionRegion)) {
                        if (setDistinctRegion.add(item.getIp())) {
                            countRegionDistinct++;
                        }
                    }
                    if (!item.getIp().equals(transactionIP)) {
                        if (setDistinctIP.add(item.getIp())) {
                            countIpDistinct++;
                        }
                    }
                } else {
                    break;
                }
                if (countIpDistinct >= 2 && countRegionDistinct >= 2) {
                    break;
                }
            }

        if (countIpDistinct >= 2) {
            info.add("ip-correlation");
            if (countIpDistinct == 2) {
                System.out.println("Ip = 2");
                if (!rs.equals("PROHIBITED")) {
                    rs = "MANUAL_PROCESSING";
                } else {
                    rs = "PROHIBITED";

                }
                indexManual.add("ip-correlation");
            }

        }
        if (countRegionDistinct >= 2) {
            info.add("region-correlation");
            if (countRegionDistinct == 2) {
                System.out.println("Region = 2");
                if (!rs.equals("PROHIBITED")) {
                    rs = "MANUAL_PROCESSING";
                } else {
                    rs = "PROHIBITED";

                }
                indexManual.add("region-correlation");
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
        if (0 < transaction.getAmount() && transaction.getAmount() <= 200) {
            return "ALLOWED";
        }
        if (200 < transaction.getAmount() && transaction.getAmount() <= 1500) {
            return "MANUAL_PROCESSING";
        }
        if (transaction.getAmount() > 1500) {
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


}
