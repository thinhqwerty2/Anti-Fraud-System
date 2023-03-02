package antifraud.controller;

import antifraud.entity.Transaction;
import antifraud.service.UserDetailServiceIml;
import antifraud.entity.UserDetails;
import antifraud.entity.UserDetailsDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserDetailServiceIml userDetailServiceIml;

    private final PasswordEncoder encoder;

    public ApiController(UserDetailServiceIml userDetailServiceIml, PasswordEncoder encoder) {
        this.userDetailServiceIml = userDetailServiceIml;
        this.encoder = encoder;
    }

    @PostMapping(value = "/antifraud/transaction", produces = "application/json")
    public ResponseEntity<Map<String, String>> checkValidation(@RequestBody Transaction transaction) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");
        if (0 < transaction.getAmount() && transaction.getAmount() <= 200) {
            return ResponseEntity.ok().headers(httpHeaders).body(Map.of("result", "ALLOWED"));
        }
        if (200 < transaction.getAmount() && transaction.getAmount() <= 1500) {
            return ResponseEntity.ok().headers(httpHeaders).body(Map.of("result", "MANUAL_PROCESSING"));
        }
        if (transaction.getAmount() > 1500) {
            return ResponseEntity.ok().headers(httpHeaders).body(Map.of("result", "PROHIBITED"));
        }

        return ResponseEntity.badRequest().build();


    }

    @PostMapping(value = "/auth/user", produces = "application/json")
    public ResponseEntity<UserDetailsDTO> createUser(@Valid @RequestBody UserDetails userDetails) {
        try {
            if (userDetailServiceIml.numOfUserDetail() == 0) {
//                userDetails.setPassword(encoder.encode(userDetails.getPassword()));
                userDetails.setRole("ROLE_ADMINISTRATOR");
                userDetails.setActive(true);
                userDetailServiceIml.saveUser(userDetails);
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(new UserDetailsDTO(userDetails));

            } else {

                if (!userDetailServiceIml.isExistedUserName(userDetails.getUsername())) {
//                    userDetails.setPassword(encoder.encode(userDetails.getPassword()));
                    userDetails.setRole("ROLE_MERCHANT");
                    userDetails.setActive(false);
                    userDetailServiceIml.saveUser(userDetails);
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(new UserDetailsDTO(userDetails));
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
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("username", username, "status", "Deleted successfully!"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @PutMapping(value = "auth/role", produces = "application/json",consumes = "application/json")
    public ResponseEntity<UserDetailsDTO> updateRole(@RequestBody Map<String, String> userNameAndRole) {
        if (userNameAndRole.get("role").equals("SUPPORT") || userNameAndRole.get("role").equals("MERCHANT")) {
            UserDetails temp = userDetailServiceIml.findUserDetailsByUserName(userNameAndRole.get("username"));
            if (temp != null) {
                if (!temp.getSimpleRole().equals(userNameAndRole.get("role"))) {
                    temp.setRole("ROLE_"+userNameAndRole.get("role"));
                    userDetailServiceIml.saveUser(temp);
                    return ResponseEntity.status(HttpStatus.OK).body(new UserDetailsDTO(temp));
                } else return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
                return ResponseEntity
                        .status(200)
                        .body(Map.of("status", "User " + temp.getUsername() + (temp.isActive() ? " unlocked!" : " locked!")));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
