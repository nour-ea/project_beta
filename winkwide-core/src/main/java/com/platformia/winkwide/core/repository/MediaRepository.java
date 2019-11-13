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
			+ "    ( :id is null or c.id = :id )"
			+ "and ( :name is null or c.name like %:name% )"
			+ "and ( :category is null or c.category like %:category% )"
			+ "and ( :type is null or c.type like %:type% )"
			+ "and ( :format is null or c.format like %:format% )"
			+ "and ( :url is null or c.url like %:url% )"
			+ "and ( :thumbUrl is null or c.thumbUrl like %:thumbUrl% )"
			+ "and ( :verified is null or c.verified = :verified )"
			+ "and ( :sizeMin is null or :sizeMax is null or (c.size between :sizeMin and :sizeMax) )")
	
	public Page<Media> findByCustomFilters(
			@Param("id") Long id,
			@Param("name") String name, 
			@Param("category") String category,
			@Param("type") String type,
			@Param("format") String format,
			@Param("url") String url,
			@Param("thumbUrl") String thumbUrl,
			@Param("verified") Boolean verified,
			@Param("sizeMin") Long sizeMin,
			@Param("sizeMax") Long sizeMax,
			Pageable p);
	
	public Media findByName( String name);

	@Modifying
	@Transactional
	@Query(value="delete from spots where media_id = :mediaId", nativeQuery = true)
	public void deleteMediaSpotLinks(
			@Param("mediaId") Long mediaId);
	
	@Modifying
	@Transactional
	@Query(value="update records set media_id = null where media_id = :mediaId", nativeQuery = true)
	public void deleteMediaRecordLinks(
			@Param("mediaId") Long mediaId);
	
}