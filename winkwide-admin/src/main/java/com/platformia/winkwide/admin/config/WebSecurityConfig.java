package com.platformia.winkwide.admin.config;

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
			http.authorizeRequests().antMatchers("/portal*")//
				.access("hasAnyRole('ROLE_CLIENT', 'ROLE_PARTNER', 'ROLE_ADMIN')");

			// Pages only for ADMIN
			http.authorizeRequests().antMatchers("/portal/displays").access("hasAnyRole('ROLE_ADMIN')");
			http.authorizeRequests().antMatchers("/portal/playlists").access("hasAnyRole('ROLE_ADMIN')");
			http.authorizeRequests().antMatchers("/portal/medias").access("hasAnyRole('ROLE_ADMIN')");
			http.authorizeRequests().antMatchers("/portal/playlists").access("hasAnyRole('ROLE_ADMIN')");
			http.authorizeRequests().antMatchers("/portal/programs").access("hasAnyRole('ROLE_ADMIN')");
			http.authorizeRequests().antMatchers("/portal/reports").access("hasAnyRole('ROLE_ADMIN')");
			http.authorizeRequests().antMatchers("/portal/settings").access("hasAnyRole('ROLE_ADMIN')");


			// Configuration for Login.
			http.authorizeRequests()

			.and()
			.formLogin()
			.loginPage("/login")
			.loginProcessingUrl("/portal_login")
			.usernameParameter("userName")//
			.passwordParameter("password")
			.defaultSuccessUrl("/portal")
			.failureUrl("/login?error=true")//


			.and()
			.logout()
			.logoutUrl("/logout")
			.logoutSuccessUrl("/")
			.deleteCookies("JSESSIONID")

			.and()
			.exceptionHandling()
			.accessDeniedPage("/portal/403")

			.and()
			.csrf().disable();				    

		}

	}