package com.platformia.winkwide.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 
public class SecurityUtils {
 
    // Encryt Password with BCryptPasswordEncoder
    public static String encrytPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
     
}