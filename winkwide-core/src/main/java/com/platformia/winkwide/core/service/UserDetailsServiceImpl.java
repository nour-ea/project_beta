package com.platformia.winkwide.core.service;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.repository.AccountRepository;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
 
	@Autowired
	private AccountRepository repository;

	@Autowired 
	HttpSession session;
	
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = repository.findByUserName(username);
        System.out.println("Account= " + account);
 
        if (account == null) {
            throw new UsernameNotFoundException("User " //
                    + username + " was not found in the database");
        }
 
        // CLIENT, SELLER, ADMIN,..
        String role = account.getUserRole();
 
        List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
 
        // ROLE_CLIENT, ROLE_PARTNER, ROLE_ADMIN
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
 
        grantList.add(authority);
 
        boolean enabled = account.isActive();
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
 
        UserDetails userDetails = (UserDetails) new User( account.getUserName(), //
                account.getEncrytedPassword(), enabled, accountNonExpired, //
                credentialsNonExpired, accountNonLocked, grantList);
        
        //put account name and id in session
        session.setAttribute("accountId", account.getId());
        session.setAttribute("accountUserName", account.getUserName());
        session.setAttribute("accountFirstName", account.getFirstName());
        session.setAttribute("accountLastName", account.getLastName());
        session.setAttribute("accountUserRole", account.getUserRole());
         
        return userDetails;
    }
 
}