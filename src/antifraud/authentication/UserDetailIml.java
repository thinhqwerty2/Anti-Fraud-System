package antifraud.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailIml implements UserDetails {
    private final String username;
    private final String password;
    private final boolean active;
    private final List<GrantedAuthority> rolesAuthority;

    public UserDetailIml(antifraud.entity.UserDetails userDetails) {
        this.username = userDetails.getUsername();
        this.password = userDetails.getPassword();
        this.rolesAuthority = List.of(new SimpleGrantedAuthority(userDetails.getRole()));
        this.active = userDetails.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesAuthority;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (active==true){
            return true;
        }
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
