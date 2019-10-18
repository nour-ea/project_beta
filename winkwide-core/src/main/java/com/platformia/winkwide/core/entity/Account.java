package com.platformia.winkwide.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platformia.winkwide.core.model.Auditable;
import com.platformia.winkwide.core.utils.MyRandomNumericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Accounts")
public class Account extends Auditable implements Serializable {

	private static final long serialVersionUID = 7377377702416850270L;

	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_PARTNER = "ROLE_PARTNER";
	public static final String ROLE_CLIENT = "ROLE_CLIENT";
	public static final String ROLE_MACHINE = "ROLE_MACHINE";

	@Id
	@GeneratedValue(generator = MyRandomNumericGenerator.generatorName)
    @GenericGenerator(name = MyRandomNumericGenerator.generatorName, strategy = "com.platformia.winkwide.core.utils.MyRandomNumericGenerator")
    @Column(name = "id", nullable = false)
    private Long id;
	
	@Column(name = "user_name", length = 20, nullable = false, unique = true)
	private String userName;

	@Column(name = "first_name", length = 128, nullable = false)
	private String firstName;

	@Column(name = "last_name", length = 128, nullable = false)
	private String lastName;

	@Column(name = "password", length = 128)
	private String password;

	@Column(name = "confirmPassword", length = 128)
	private String confirmPassword;

	@JsonIgnore
	@Column(name = "encryted_password", length = 128, nullable = false)
	private String encrytedPassword;
	
	@Column(name = "user_role", length = 20, nullable = false)
	private String userRole;

	@Column(name = "active", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
	private boolean active;

	@Column(name = "conditions_accepted", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
	private boolean conditionsAccepted;

	public Account(String userName, String firstName, String lastName, String password, String confirmPassword,
			String encrytedPassword, String userRole, boolean active, boolean conditionsAccepted) {
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = null;
		this.confirmPassword = null;
		this.encrytedPassword = encrytedPassword;
		this.userRole = userRole;
		this.active = active;
		this.conditionsAccepted = conditionsAccepted;
	}

}
