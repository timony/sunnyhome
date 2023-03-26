package cz.snyll.Sunny.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BasicConfiguration {
    @Autowired
    private MainConfiguration mainConfiguration;

    @Autowired
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        if (mainConfiguration.getUserName() != null) {
            PasswordEncoder encoder =
                    PasswordEncoderFactories.createDelegatingPasswordEncoder();
            auth
                    .inMemoryAuthentication()
                    .withUser(mainConfiguration.getUserName())
                    .password(encoder.encode(mainConfiguration.getPassword()))
                    .roles("USER", "ADMIN");
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (mainConfiguration.getUserName() != null) {
            http.authorizeHttpRequests()
                    .requestMatchers("/login*")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .formLogin()
                    .and().rememberMe()
                    .key("fr465fad3234f*")
                    .tokenValiditySeconds(86400 * 365);
        } else {
            System.out.println("No username setup in configuration file. No security being applied, access to everyone in the network is allowed.");
        }
        return http.build();
    }
}
