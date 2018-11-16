package sk.mimac.perun.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 *
 * @author Mimac
 */
@Order(50)
@Configuration
@EnableWebSecurity
public class PayloadWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${perun.payload.user}")
    private String payloadUser;
    
    @Value("${perun.payload.password}")
    private String payloadPassword;
    
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/payload/**").authorizeRequests()
                .anyRequest().authenticated()
                .and().httpBasic().realmName("hab-perun payload API")
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(payloadUser).password(payloadPassword).roles("PAYLOAD");
    }

}
