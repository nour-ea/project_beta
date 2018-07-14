package com.platformia.winkwide.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 
public class SecurityUtils {
 
    // Encryt Password with BCryptPasswordEncoder
    public static String encrytePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
     
}