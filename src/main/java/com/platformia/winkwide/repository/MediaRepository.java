package com.platformia.winkwide.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import com.platformia.winkwide.entity.Media;
import com.platformia.winkwide.projection.NoMediaVerified;

//@PreAuthorize("hasRole('ROLE_ADMIN')")
@RepositoryRestResource(excerptProjection = NoMediaVerified.class)
public interface MediaRepository extends JpaRepository<Media, Long> {

	
	
	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :name is null or c.name like %:name% )"
			+ "and ( :mediaType is null or c.mediaType like %:mediaType% )"
			+ "and ( :format is null or c.format like %:format% )"
			+ "and ( :url is null or c.url like %:url% )"
			+ "and ( :sizeMin is null or :sizeMax is null or (c.size between :sizeMin and :sizeMax) )")
	
	public Page<Media> findByCustomFilters(
			@Param("name") String name, 
			@Param("mediaType") String mediaType,
			@Param("format") String format,
			@Param("url") String url,
			@Param("sizeMin") Integer sizeMin,
			@Param("sizeMax") Integer sizeMax,
			Pageable p);
	
}