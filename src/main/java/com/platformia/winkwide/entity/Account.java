package com.platformia.winkwide.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.platformia.winkwide.form.AccountForm;
import com.platformia.winkwide.utils.SecurityUtils;

import lombok.Data;

@Data
@Entity
@Table(name = "Accounts")
public class Account implements Serializable {

	private static final long serialVersionUID = 7377377702416850270L;

	public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SELLER = "ROLE_SELLER";
	public static final String ROLE_CLIENT = "ROLE_CLIENT";
	
    @Id
    @Column(name = "user_name", length = 20, nullable = false)
    private String userName;
    
    @Column(name = "first_name", length = 128, nullable = false)
    private String firstName;
    
    @Column(name = "last_name", length = 128, nullable = false)
    private String lastName;
 
    @Column(name = "encryted_password", length = 128, nullable = false)
    private String encrytedPassword;
     
    @Column(name = "active", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
    private boolean active;
    
    @Column(name = "conditions_accepted", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
    private boolean conditionsAccepted;
    
    @Column(name = "user_role", length = 20, nullable = false)
    private String userRole;
	
    public Account() {

	}
    
	public Account(String userName, String firstName, String lastName, String encrytedPassword, boolean active,
			boolean conditionsAccepted, String userRole) {
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.encrytedPassword = encrytedPassword;
		this.active = active;
		this.conditionsAccepted = conditionsAccepted;
		this.userRole = userRole;
	}

	public Account(AccountForm accountForm, String role) {
		this.userName = accountForm.getUserName();
		this.firstName = accountForm.getFirstName();
		this.lastName = accountForm.getLastName();
		this.encrytedPassword = SecurityUtils.encrytePassword(accountForm.getPassword());
		this.active = false;
		this.conditionsAccepted = accountForm.getConditionsAccepted();
		this.userRole = role;
	}
	
}
