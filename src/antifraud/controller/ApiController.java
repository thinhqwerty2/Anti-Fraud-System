package antifraud.controller;

import antifraud.entity.*;
import antifraud.repository.TransactionRepository;
import antifraud.requestbody.FeedBackRequest;
import antifraud.service.CardNumberService;
import antifraud.service.IpService;
import antifraud.service.TransactionService;
import antifraud.service.UserDetailServiceIml;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserDetailServiceIml userDetailServiceIml;
    private final IpService ipService;
    private final CardNumberService cardNumberService;
    private final TransactionService transactionService;
//    private final TransactionRepository transactionRepository;


    public ApiController(UserDetailServiceIml userDetailServiceIml, IpService ipService, CardNumberService cardNumberService, TransactionService transactionService, TransactionRepository transactionRepository) {
        this.userDetailServiceIml = userDetailServiceIml;
        this.ipService = ipService;
        this.cardNumberService = cardNumberService;
        this.transactionService = transactionService;
//        this.transactionRepository = transactionRepository;
    }

    @PostMapping(value = "/antifraud/transaction", produces = "application/json")
    public ResponseEntity<Map<String, String>> checkValidation(@Valid @RequestBody Transaction transaction) {
//        try {
        if (transactionService.isBadRequest(transaction)) {
            System.out.println("BadRequest");
            return ResponseEntity.status(400).build();
        }
//        List<Transaction> transactionBefore = transactionService.findLatestByNumberCard(transaction.getNumber());
        List<Transaction> transactionBefore = transactionService.findOneHourBefore(transaction);

        String[] rs = transactionService.reasonReject(transaction, transactionBefore);
        String result = rs[1];
        String info = rs[0];
        transaction.setResult(result);
        transaction.setFeedback("");
        transactionService.saveTransaction(transaction);
        return ResponseEntity.ok().body(Map.of("result", result, "info", info));

    }

    @PostMapping(value = "/auth/user", produces = "application/json")
    public ResponseEntity<UserDetailsDTO> createUser(@Valid @RequestBody UserDetails userDetails) {
        try {
            if (userDetailServiceIml.numOfUserDetail() == 0) {
//                userDetails.setPassword(encoder.encode(userDetails.getPassword()));
                userDetails.setRole("ROLE_ADMINISTRATOR");
                userDetails.setActive(true);
                userDetailServiceIml.saveUser(userDetails);
                return ResponseEntity.status(HttpStatus.CREATED).body(new UserDetailsDTO(userDetails));

            } else {

                if (!userDetailServiceIml.isExistedUserName(userDetails.getUsername())) {
//                    userDetails.setPassword(encoder.encode(userDetails.getPassword()));
                    userDetails.setRole("ROLE_MERCHANT");
                    userDetails.setActive(false);
                    userDetailServiceIml.saveUser(userDetails);
                    return ResponseEntity.status(HttpStatus.CREATED).body(new UserDetailsDTO(userDetails));
                } else return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }


    }

    @GetMapping(value = "/auth/list", produces = "application/json")
    public ResponseEntity<List<UserDetailsDTO>> getListAuth() {
        return ResponseEntity.status(HttpStatus.OK).body(userDetailServiceIml.getListAuth());
    }

    @DeleteMapping(value = "auth/user/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        if (userDetailServiceIml.isExistedUserName(username)) {
            userDetailServiceIml.deleteUser(username);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("username", username, "status", "Deleted successfully!"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @PutMapping(value = "auth/role", produces = "application/json", consumes = "application/json")
    public ResponseEntity<UserDetailsDTO> updateRole(@RequestBody Map<String, String> userNameAndRole) {
        if (userNameAndRole.get("role").equals("SUPPORT") || userNameAndRole.get("role").equals("MERCHANT")) {
            UserDetails temp = userDetailServiceIml.findUserDetailsByUserName(userNameAndRole.get("username"));
            if (temp != null) {
                if (!temp.getSimpleRole().equals(userNameAndRole.get("role"))) {
                    temp.setRole("ROLE_" + userNameAndRole.get("role"));
                    userDetailServiceIml.saveUser(temp);
                    return ResponseEntity.status(HttpStatus.OK).body(new UserDetailsDTO(temp));
                } else return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PutMapping(value = "auth/access", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, String>> giveAccess(@RequestBody Map<String, String> userNameAndAccess) {
        UserDetails temp = userDetailServiceIml.findUserDetailsByUserName(userNameAndAccess.get("username"));
        if (temp != null) {
            if (!temp.getSimpleRole().equals("ADMINISTRATOR")) {
                switch (userNameAndAccess.get("operation")) {
                    case "LOCK":
                        temp.setActive(false);
                        break;
                    case "UNLOCK":
                        temp.setActive(true);
                        break;
                }
                userDetailServiceIml.saveUser(temp);
                return ResponseEntity.status(200).body(Map.of("status", "User " + temp.getUsername() + (temp.isActive() ? " unlocked!" : " locked!")));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //////////////////////////////////////////////IpAndCardNumber///////////////////////////

    /////////////////////////IP///////////////////////
    @PostMapping(value = "/antifraud/suspicious-ip", consumes = "application/json", produces = "application/json")
    public ResponseEntity<IpSuspicious> saveSuspiciousIp(@Valid @RequestBody IpSuspicious ipSuspicious) {
        if (ipService.isCorrectFormat(ipSuspicious.getIp())) {
            IpSuspicious ipTemp = ipService.findByIp(ipSuspicious.getIp());
            if (ipTemp == null) {
                ipService.saveIp(ipSuspicious);
                return ResponseEntity.status(200).body(ipSuspicious);
            }
            return ResponseEntity.status(409).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @DeleteMapping(value = "/antifraud/suspicious-ip/{ip}", produces = "application/json")
    public ResponseEntity<Map<String, String>> deleteSuspiciousIp(@PathVariable String ip) {
        if (ipService.isCorrectFormat(ip)) {
            IpSuspicious ipTemp = ipService.findByIp(ip);
            if (ipTemp != null) {
                ipService.deleteByIp(ip);
                return ResponseEntity.status(200).body(Map.of("status", "IP " + ipTemp.getIp() + " successfully removed!"));
            }
            return ResponseEntity.status(404).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping(value = "/antifraud/suspicious-ip", produces = "application/json")
    public ResponseEntity<List<IpSuspicious>> getListIp() {
        return ResponseEntity.status(200).body(ipService.findAll());
    }

    //////////////////////////////////////CardNumber/////////////////////
    @PostMapping(value = "/antifraud/stolencard", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StolenCard> saveStolenNumber(@Valid @RequestBody StolenCard stolenCard) {
        if (cardNumberService.isCorrectFormat(stolenCard.getNumber())) {
            StolenCard numberTemp = cardNumberService.findByNumber(stolenCard.getNumber());
            if (numberTemp == null) {
                cardNumberService.save(stolenCard);
                return ResponseEntity.status(200).body(stolenCard);
            }
            return ResponseEntity.status(409).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @DeleteMapping(value = "/antifraud/stolencard/{number}", produces = "application/json")
    public ResponseEntity<Map<String, String>> deleteStolenNumber(@PathVariable String number) {
        if (cardNumberService.isCorrectFormat(number)) {
            StolenCard numberTemp = cardNumberService.findByNumber(number);
            if (numberTemp != null) {
                cardNumberService.deleteByNumber(number);
                return ResponseEntity.status(200).body(Map.of("status", "Card " + numberTemp.getNumber() + " successfully removed!"));
            }
            return ResponseEntity.status(404).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping(value = "/antifraud/stolencard", produces = "application/json")
    public ResponseEntity<List<StolenCard>> getListStolenNumber() {
        return ResponseEntity.status(200).body(cardNumberService.findAll());
    }

    ////////////////////////Feedback////////////////
    @PutMapping(value = "/antifraud/transaction")
    public ResponseEntity<Transaction> feedBack(@RequestBody @Valid FeedBackRequest idAndFeedback) {
        System.out.println(idAndFeedback.getFeedback());

        Transaction transaction = transactionService.findById(idAndFeedback.getTransactionId());
        if (transaction != null) {
            if (Objects.equals(transaction.getFeedback(), "")) {
                if (!transaction.getResult().equals(idAndFeedback.getFeedback())) {
                    transaction.setFeedback(idAndFeedback.getFeedback());
                    //Save feedback
                    transactionService.saveTransaction(transaction);
                    //Save currentAmount to latest transaction
                    List<Long> temp = transactionService.updateLimit(transaction, idAndFeedback.getFeedback());
                    Transaction tempTransaction = transactionService.findLatestTransaction(transaction.getNumber());
                    if (tempTransaction != null) {
                        tempTransaction.setAmountAllow(temp.get(0));
                        tempTransaction.setAmountManual(temp.get(1));
                        transactionService.saveTransaction(tempTransaction);
                    }
                    System.out.println(transaction);
                    return ResponseEntity.status(200).body(transaction);
                } else {
                    return ResponseEntity.status(422).build();
                }
            } else {
                return ResponseEntity.status(409).build();
            }
        } else return ResponseEntity.status(404).build();
    }

    @GetMapping(value = "/antifraud/history")
    public ResponseEntity<List<Transaction>> getListFeedBack() throws JsonProcessingException {
        List<Transaction> transactions = transactionService.getListFeedBack();
        return ResponseEntity.ok().body(transactions);
    }

    @GetMapping(value = "/antifraud/history/{number}")
    public ResponseEntity<List<Transaction>> getListFeedBackByNumber(@PathVariable String number) {
        if (cardNumberService.isCorrectFormat(number)) {
            if (transactionService.getListFeeBackByNumber(number).size() != 0) {
                return ResponseEntity.status(200).body(transactionService.getListFeeBackByNumber(number));
            } else

                return ResponseEntity.status(404).build();

        } else return ResponseEntity.status(400).build();
    }

}
