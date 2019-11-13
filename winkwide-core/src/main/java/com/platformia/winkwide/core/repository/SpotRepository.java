package com.platformia.winkwide.core.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.platformia.winkwide.core.entity.Spot;

//@RepositoryRestResource(excerptProjection = SpotProjection.class)
public interface SpotRepository extends JpaRepository<Spot, Long> {

	
	
	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :id is null or c.id = :id )"
			+ "and ( :playlistId is null or c.playlist.id = :playlistId )"
			+ "and ( :mediaId is null or c.media.id = :mediaId )"
			+ "and ( :durationMin is null or :durationMax is null or (c.duration between :durationMin and :durationMax) )")
	
	public Page<Spot> findByCustomFilters(
			@Param("id") Long id,
			@Param("playlistId") Long playlistId,
			@Param("mediaId") Long mediaId, 
			@Param("durationMin") Long durationMin,
			@Param("durationMax") Long durationMax,
			Pageable p);

	public List<Spot> findByPlaylistId(@Param("playListId") Long playListId);				
			
}