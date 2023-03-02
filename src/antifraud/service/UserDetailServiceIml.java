package antifraud.service;

import antifraud.authentication.UserDetailIml;
import antifraud.entity.UserDetails;
import antifraud.entity.UserDetailsDTO;
import antifraud.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceIml implements UserDetailsService {
    private final UserRepository userRepository;
    public UserDetailServiceIml(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    public void saveUser(UserDetails userDetails) {
        userRepository.save(userDetails);
    }

    public int updateRoleByUserName(String role,String username){
        return userRepository.updateRoleByUsernameIgnoreCase(role,username);
    }

    public void deleteUser(String username){
        userRepository.deleteByUsernameIgnoreCase(username);
    }

    public long numOfUserDetail()
    {
        return userRepository.count();
    }

    public boolean isExistedUserName(String userName) {
        return userRepository.findByUsernameIgnoreCase(userName) != null;
    }

    public UserDetails findUserDetailsByUserName(String userName)  {
        return userRepository.findByUsernameIgnoreCase(userName);
    }

    public List<UserDetailsDTO> getListAuth() {
        return userRepository.getListAuth().stream()
                .map(userDetails -> new UserDetailsDTO(userDetails.getId(), userDetails.getName(), userDetails.getUsername(), userDetails.getRole()))
                .collect(Collectors.toList());
    }


    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = userRepository.findByUsernameIgnoreCase(username);

        if (user == null) {
            throw new UsernameNotFoundException("Not found: " + username);
        }
        return new UserDetailIml(user);

    }
}
