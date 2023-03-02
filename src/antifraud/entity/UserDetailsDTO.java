package antifraud.entity;


import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

public class UserDetailsDTO {
    final long id;
    final String name;
    final String username;

    final String role;

    public String getRole() {
        return role.substring(5);
    }

    @Override
    public String toString() {
        return "UserDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public UserDetailsDTO(long id, String name, String username, String role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
    }

    public UserDetailsDTO(UserDetails userDetails) {
        this.id = userDetails.getId();
        this.name = userDetails.getName();
        this.username = userDetails.getUsername();
        this.role = userDetails.getRole();
    }

    public long getId() {
        return id;
    }

//    public void setId(long id) {
//        this.id = id;
//    }

    public String getName() {
        return name;
    }

//    public void setName(String name) {
//        this.name = name;
//    }

    public String getUsername() {
        return username;
    }

//    public void setUsername(String username) {
//        this.username = username;
//    }


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
