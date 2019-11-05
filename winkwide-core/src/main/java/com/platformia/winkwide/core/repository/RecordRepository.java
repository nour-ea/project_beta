package com.platformia.winkwide.core.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.platformia.winkwide.core.entity.Record;

public interface RecordRepository extends JpaRepository<Record, Long> {
	
	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :displayId is null or c.displayId = :displayId )"
			+ "and ( :mediaId is null or c.mediaId = :mediaId )"
			+ "and ( :displayName is null or c.displayName like %:displayName% )"
			+ "and ( :mediaName is null or c.mediaName like %:mediaName% )"
			+ "and ( :startTimeMin is null or :startTimeMax is null or (c.startTime between :startTimeMin and :startTimeMax) )"
			+ "and ( :endTimeMin is null or :endTimeMax is null or (c.endTime between :endTimeMin and :endTimeMax) )")
	
	public Page<Record> findByCustomFilters(
			@Param("displayId") Long displayId,
			@Param("mediaId") Long mediaId, 
			@Param("displayName") String displayName,
			@Param("mediaName") String mediaName,
			@Param("startTimeMin") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startTimeMin,
			@Param("startTimeMax") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startTimeMax,
			@Param("endTimeMin") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endTimeMin,
			@Param("endTimeMax") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endTimeMax,
			Pageable p);
	
	public List<Record> findByDisplayId(@Param("displayId") Long displayId);
	
	public List<Record> findByMediaId(@Param("mediaId") Long mediaId);
	
}
