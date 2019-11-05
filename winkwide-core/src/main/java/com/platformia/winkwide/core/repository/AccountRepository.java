package com.platformia.winkwide.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.platformia.winkwide.core.entity.Account;

//@PreAuthorize("hasRole('ROLE_ADMIN')")
//@RepositoryRestResource(excerptProjection = AccountProjection.class)
public interface AccountRepository extends JpaRepository<Account, Long> {

	
	
	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :userName is null or c.userName like %:userName% )"
			+ "and ( :firstName is null or c.firstName like %:firstName% )"
			+ "and ( :lastName is null or c.lastName like %:lastName% )"
			+ "and ( :userRole is null or c.userRole like %:userRole% )"
			+ "and ( :active is null or c.active = :active )"
			+ "and ( :conditionsAccepted is null or c.conditionsAccepted = :conditionsAccepted )")
	
	public Page<Account> findByCustomFilters(
			@Param("userName") String userName, 
			@Param("firstName") String firstName,
			@Param("lastName") String lastName,
			@Param("userRole") String userRole,
			@Param("active") Boolean active,
			@Param("conditionsAccepted") Boolean conditionsAccepted,
			Pageable p);	
	
	
	public Account findByUserName(@Param("userName") String userName);	
	
}
