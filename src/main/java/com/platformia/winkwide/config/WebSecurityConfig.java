package com.platformia.winkwide.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.platformia.winkwide.security.CustomAccessDeniedHandler;
import com.platformia.winkwide.security.CustomUrlAuthenticationSuccessHandler;
import com.platformia.winkwide.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		// Setting Service to find User in the database.
		// And Setting PassswordEncoder
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

	}

	@Bean
	public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
		return new CustomUrlAuthenticationSuccessHandler();
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler(){
	    return new CustomAccessDeniedHandler();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// Protect against Cross Site Request Forgery
		http.csrf().disable();

		// Requires login with role ROLE_CLIENT, ROLE_SELLER, or ROLE_ADMIN.
		// If not, it will redirect to /login.
		http.authorizeRequests().antMatchers("/")//
				.access("hasAnyRole('ROLE_MACHINE')");

		http.authorizeRequests().antMatchers("/portal", "/portal/accountInfo")//
				.access("hasAnyRole('ROLE_CLIENT', 'ROLE_PARTNER', 'ROLE_ADMIN')");

		// Pages only for ADMIN
		http.authorizeRequests().antMatchers("/portal/displays").access("hasAnyRole('ROLE_ADMIN')");
		http.authorizeRequests().antMatchers("/portal/medias").access("hasAnyRole('ROLE_ADMIN')");
		http.authorizeRequests().antMatchers("/portal/programs").access("hasAnyRole('ROLE_ADMIN')");
		http.authorizeRequests().antMatchers("/portal/reports").access("hasAnyRole('ROLE_ADMIN')");

		// When user login, role XX.
		// But access to the page requires the YY role,
		// An AccessDeniedException will be thrown.
		http.authorizeRequests().and().exceptionHandling().accessDeniedHandler(accessDeniedHandler());

		// Configuration for Login Form.
		http.authorizeRequests().and().formLogin()//

				//
				.loginProcessingUrl("/j_spring_security_check") // Submit URL
				.loginPage("/login")//
				.successHandler(myAuthenticationSuccessHandler())
				.failureUrl("/login?error=true")//
				.usernameParameter("userName")//
				.passwordParameter("password")

				// Configuration for the Logout page.
				// (After logout, go to home page)
				.and().logout().logoutUrl("/logout").logoutSuccessUrl("/");

	}

}
