package com.platformia.winkwide.form;

import lombok.Data;

@Data
public class AccountForm {

    private String userName;
    
    private String firstName;
    
    private String lastName;
    
    private String password;
    
    private String confirmPassword;
    
    private Boolean conditionsAccepted;

}
