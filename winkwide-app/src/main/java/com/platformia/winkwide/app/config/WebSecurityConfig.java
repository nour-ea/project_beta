package com.platformia.winkwide.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.platformia.winkwide.core.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


				
		@Autowired
		public UserDetailsServiceImpl userDetailsService;

		@Bean
		public static BCryptPasswordEncoder passwordEncoder() {
			BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
			return bCryptPasswordEncoder;
		}

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {

			// Setting Service to find User in the database.
			// And Setting PassswordEncoder
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			//Protected Pages					
			http.authorizeRequests().antMatchers("/tv*")//
				.access("hasAnyRole('ROLE_MACHINE')");
			
			http.authorizeRequests().antMatchers("/sync*")//
			.access("hasAnyRole('ROLE_MACHINE')");

			
			// Configuration for Login.
			http.authorizeRequests()

			.and()
			.formLogin()
			.loginPage("/login")
			.loginProcessingUrl("/tv_login")
			.usernameParameter("userName")//
			.passwordParameter("password")
			.defaultSuccessUrl("/tv")
			.failureUrl("/login?error=true")//


			.and()
			.logout()
			.logoutUrl("/logout")
			.logoutSuccessUrl("/tv")
			.deleteCookies("JSESSIONID")

			.and()
			.exceptionHandling()
			.accessDeniedPage("/tv/403")

			.and()
			.csrf().disable();				    

		}

	}