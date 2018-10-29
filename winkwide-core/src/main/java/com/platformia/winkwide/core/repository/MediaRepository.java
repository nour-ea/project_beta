package com.platformia.winkwide.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;

import com.platformia.winkwide.core.entity.Media;

//@RepositoryRestResource(excerptProjection = MediaProjection.class)
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
			@Param("sizeMin") Long sizeMin,
			@Param("sizeMax") Long sizeMax,
			Pageable p);
	
	public Media findByName( String name);

	@Modifying
	@Transactional
	@Query(value="delete from Programs_Medias where media_id = :mediaId", nativeQuery = true)
	public void deleteMediaProgramLinks(
			@Param("mediaId") Long mediaId);
	
	@Modifying
	@Transactional
	@Query(value="update Reports set media_id = null where media_id = :mediaId", nativeQuery = true)
	public void deleteMediaReportLinks(
			@Param("mediaId") Long mediaId);
	
}