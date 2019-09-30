package com.platformia.winkwide.core.repository;

import java.math.BigDecimal;
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

import com.platformia.winkwide.core.entity.Display;

public interface DisplayRepository extends JpaRepository<Display, Long> {

	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :name is null or c.name like %:name% )"
			+ "and ( :area is null or c.area like %:area% )"
			+ "and ( :address is null or c.address like %:address% )"
			+ "and ( :brand is null or c.brand like %:brand% )"
			+ "and ( :mac is null or c.mac like %:mac% )"
			+ "and ( :smart is null or c.smart = :smart )"
			+ "and ( :sizeMin is null or :sizeMax is null or (c.size between :sizeMin and :sizeMax) )"
			+ "and ( :longitudeMin is null or :longitudeMax is null or (c.longitude between :longitudeMin and :longitudeMax) )"
			+ "and ( :latitudeMin is null or :latitudeMax is null or (c.latitude between :latitudeMin and :latitudeMax) )"
			+ "and ( :averageAudienceMin is null or :averageAudienceMax is null or (c.averageAudience between :averageAudienceMin and :averageAudienceMax) )"
			+ "and ( :lastSyncTimeMin is null or :lastSyncTimeMax is null or (c.lastSyncTime between :lastSyncTimeMin and :lastSyncTimeMax) )")
	
	public Page<Display> findByCustomFilters(
			@Param("name") String name, 
			@Param("area") String area,
			@Param("address") String address,
			@Param("brand") String brand,
			@Param("mac") String mac,
			@Param("smart") Boolean smart,
			@Param("sizeMin") Integer sizeMin,
			@Param("sizeMax") Integer sizeMax,
			@Param("longitudeMin") BigDecimal longitudeMin,
			@Param("longitudeMax") BigDecimal longitudeMax,
			@Param("latitudeMin") BigDecimal latitudeMin,
			@Param("latitudeMax") BigDecimal latitudeMax,
			@Param("averageAudienceMin") Integer averageAudienceMin,
			@Param("averageAudienceMax") Integer averageAudienceMax,
			@Param("lastSyncTimeMin") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date lastSyncTimeMin,
			@Param("lastSyncTimeMax") @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm a") Date lastSyncTimeMax,
			Pageable p);

	@Modifying
	@Transactional
	@Query(value="delete from programs_displays where display_id = :displayId", nativeQuery = true)
	public void deleteDisplayProgramLinks(
			@Param("displayId") Long displayId);
	
	@Modifying
	@Transactional
	@Query(value="update reports set display_id = null where display_id = :displayId", nativeQuery = true)
	public void deleteDisplayReportLinks(
			@Param("displayId") Long displayId);
	
	
}
