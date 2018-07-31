package com.platformia.winkwide.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.platformia.winkwide.entity.Display;

public interface DisplayRepository extends JpaRepository<Display, Long> {

	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :name is null or c.name like %:name% )"
			+ "and ( :address is null or c.address like %:address% )"
			+ "and ( :brand is null or c.brand like %:brand% )"
			+ "and ( :mac is null or c.mac like %:mac% )"
			+ "and ( :smart is null or c.smart like %:smart% )"
			+ "and ( :sizeMin is null or :sizeMax is null or (c.size between :sizeMin and :sizeMax) )"
			+ "and ( :shopCoverageMin is null or :shopCoverageMax is null or (c.shopCoverage between :shopCoverageMin and :shopCoverageMax) )")
	
	public Page<Display> findByCustomFilters(
			@Param("name") String name, 
			@Param("address") String address,
			@Param("brand") String brand,
			@Param("mac") String mac,
			@Param("smart") String smart,
			@Param("sizeMin") Integer sizeMin,
			@Param("sizeMax") Integer sizeMax,
			@Param("shopCoverageMin") BigDecimal shopCoverageMin,
			@Param("shopCoverageMax") BigDecimal shopCoverageMax,
			Pageable p);
	
	
}
