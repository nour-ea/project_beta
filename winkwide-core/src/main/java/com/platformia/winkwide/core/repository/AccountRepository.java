package com.platformia.winkwide.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.platformia.winkwide.core.entity.Account;

//@PreAuthorize("hasRole('ROLE_ADMIN')")
//@RepositoryRestResource(excerptProjection = AccountProjection.class)
public interface AccountRepository extends JpaRepository<Account, Long> {

	@Query("select c from #{#entityName} c where c.userName = :userName")
	public Account findByUserName(@Param("userName") String userName);	
	
}
