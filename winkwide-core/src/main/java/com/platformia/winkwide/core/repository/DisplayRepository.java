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
			+ "and ( :category is null or c.category like %:category% )"
			+ "and ( :area is null or c.area like %:area% )"
			+ "and ( :phone is null or c.phone like %:phone% )"
			+ "and ( :address is null or c.address like %:address% )"
			+ "and ( :brand is null or c.brand like %:brand% )"
			+ "and ( :mac is null or c.mac like %:mac% )"
			+ "and ( :smart is null or c.smart = :smart )"
			+ "and ( :sizeMin is null or :sizeMax is null or (c.size between :sizeMin and :sizeMax) )"
			+ "and ( :longitudeMin is null or :longitudeMax is null or (c.longitude between :longitudeMin and :longitudeMax) )"
			+ "and ( :latitudeMin is null or :latitudeMax is null or (c.latitude between :latitudeMin and :latitudeMax) )"
			+ "and ( :weekdayAudienceMin is null or :weekdayAudienceMax is null or (c.weekdayAudience between :weekdayAudienceMin and :weekdayAudienceMax) )"
			+ "and ( :holidayAudienceMin is null or :holidayAudienceMax is null or (c.holidayAudience between :holidayAudienceMin and :holidayAudienceMax) )"
			+ "and ( :weekdayVisitorsMin is null or :weekdayVisitorsMax is null or (c.weekdayVisitors between :weekdayVisitorsMin and :weekdayVisitorsMax) )"
			+ "and ( :holidayVisitorsMin is null or :holidayVisitorsMax is null or (c.holidayVisitors between :holidayVisitorsMin and :holidayVisitorsMax) )"
			+ "and ( :lastSyncTimeMin is null or :lastSyncTimeMax is null or (c.lastSyncTime between :lastSyncTimeMin and :lastSyncTimeMax) )")
	
	public Page<Display> findByCustomFilters(
			@Param("name") String name,
			@Param("category") String category,
			@Param("area") String area,
			@Param("phone") String phone,
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
			@Param("weekdayAudienceMin") Integer weekdayAudienceMin,
			@Param("weekdayAudienceMax") Integer weekdayAudienceMax,
			@Param("holidayAudienceMin") Integer holidayAudienceMin,
			@Param("holidayAudienceMax") Integer holidayAudienceMax,
			@Param("weekdayVisitorsMin") Integer weekdayVisitorsMin,
			@Param("weekdayVisitorsMax") Integer weekdayVisitorsMax,
			@Param("holidayVisitorsMin") Integer holidayVisitorsMin,
			@Param("holidayVisitorsMax") Integer holidayVisitorsMax,
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
	@Query(value="update records set display_id = null where display_id = :displayId", nativeQuery = true)
	public void deleteDisplayRecordLinks(
			@Param("displayId") Long displayId);
	
	public Display findByName(@Param("name") String name);	
	
	public Display findByMac(@Param("mac") String mac);	
	
}
