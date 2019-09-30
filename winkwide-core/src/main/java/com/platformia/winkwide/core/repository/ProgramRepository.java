package com.platformia.winkwide.core.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

import com.platformia.winkwide.core.entity.Program;

//@PreAuthorize("hasRole('ROLE_ADMIN')")
public interface ProgramRepository extends JpaRepository<Program, Long> {
	
	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :name is null or c.name like %:name% )"
			+ "and ( :displayId is null or (exists (select 1 from c.displays dp where ( dp.id = :displayId) ) ))"
			+ "and ( :startTimeMin is null or :startTimeMax is null or (c.startTime between :startTimeMin and :startTimeMax) )"
			+ "and ( :endTimeMin is null or :endTimeMax is null or (c.endTime between :endTimeMin and :endTimeMax) )")
	
	public Page<Program> findByCustomFilters(
			@Param("name") String name, 
			@Param("displayId") Long displayId,
			@Param("startTimeMin") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date startTimeMin,
			@Param("startTimeMax") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date startTimeMax,
			@Param("endTimeMin") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date endTimeMin,
			@Param("endTimeMax") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date endTimeMax,
			Pageable p);
	
	public Program findByName( String name);
	
	@Modifying
	@Transactional
	@Query(value="delete from programs_displays where program_id = :programId", nativeQuery = true)
	public void deleteProgramDisplayLinks(
			@Param("programId") Long programId);
	
	@Modifying
	@Transactional
	@Query(value="delete from programs_playlists where program_id = :programId", nativeQuery = true)
	public void deleteProgramPlaylistLinks(
			@Param("programId") Long programId);

}
