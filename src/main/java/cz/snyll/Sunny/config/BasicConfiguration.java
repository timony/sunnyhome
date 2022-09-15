package cz.snyll.Sunny.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class BasicConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private MainConfiguration mainConfiguration;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth
                .inMemoryAuthentication()
                .withUser("user")
                .password(encoder.encode(mainConfiguration.getPassword()))
                .roles("USER")
                .and()
                .withUser(mainConfiguration.getUserName())
                .password(encoder.encode(mainConfiguration.getPassword()))
                .roles("USER", "ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/login*").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .and()
                .formLogin()
                .and().rememberMe().key("PUTYOURCERTPASSHERE");
    }
}
