package antifraud.authentication;

import antifraud.service.UserDetailServiceIml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {

    @Autowired
    final UserDetailServiceIml userDetailServiceIml;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public WebSecurityConfigurerImpl(UserDetailServiceIml userDetailServiceiml, RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.userDetailServiceIml = userDetailServiceiml;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailServiceIml).passwordEncoder(NoOpPasswordEncoder.getInstance());
//        auth.inMemoryAuthentication()
//                .withUser("admin").password("admin").roles("ADMIN")
//                .and()
//                .passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        AuthenticationEntryPoint restAuthenticationEntryPoint = new RestAuthenticationEntryPoint();
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests() // manage access
                .mvcMatchers("/actuator/shutdown").permitAll() // needs to run test
                .mvcMatchers(HttpMethod.PUT, "/api/auth/access","/api/auth/role").hasRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.DELETE,"/api/auth/user/*").hasRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR","SUPPORT")
                .mvcMatchers(HttpMethod.POST,"/api/antifraud/transaction").hasRole("MERCHANT")
                .mvcMatchers("/api/antifraud/suspicious-ip/**","/api/antifraud/stolencard/**").hasRole("SUPPORT")
                .mvcMatchers(HttpMethod.PUT,"/api/antifraud/transaction").hasRole("SUPPORT")
//                .mvcMatchers(HttpMethod.GET,"/api/antifraud/history").hasRole("")
                .mvcMatchers(HttpMethod.GET,"/api/antifraud/history").hasRole("SUPPORT")
                .mvcMatchers(HttpMethod.GET,"/api/antifraud/history/*").hasRole("SUPPORT")
                .mvcMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
//                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }


    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}