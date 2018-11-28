package com.platformia.winkwide.core.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.platformia.winkwide.core.entity.Program;

//@PreAuthorize("hasRole('ROLE_ADMIN')")
public interface ProgramRepository extends JpaRepository<Program, Long> {
	
	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :displayId is null or c.display.id = :displayId )"
			+ "and ( :startTimeMin is null or :startTimeMax is null or (c.startTime between :startTimeMin and :startTimeMax) )"
			+ "and ( :endTimeMin is null or :endTimeMax is null or (c.endTime between :endTimeMin and :endTimeMax) )")
	
	public Page<Program> findByCustomFilters(
			@Param("displayId") Long displayId,
			@Param("startTimeMin") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date startTimeMin,
			@Param("startTimeMax") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date startTimeMax,
			@Param("endTimeMin") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date endTimeMin,
			@Param("endTimeMax") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date endTimeMax,
			Pageable p);
		

}