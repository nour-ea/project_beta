package com.platformia.winkwide.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;

import com.platformia.winkwide.core.entity.Playlist;

//@RepositoryRestResource(excerptProjection = PlaylistProjection.class)
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

	
	
	@RestResource(path = "customFilters", rel = "customFilters")
	@Query("select c from #{#entityName} c where"
			+ "    ( :id is null or c.id = :id )"
			+ "and ( :name is null or c.name like %:name% )"
			+ "and ( :mediaId is null or (exists (select 1 from Spot sp where ( sp.playlist.id = c.id and sp.media.id = :mediaId) ) ))"
			+ "and ( :durationMin is null or :durationMax is null or (c.duration between :durationMin and :durationMax) )")
	
	public Page<Playlist> findByCustomFilters(
			@Param("id") Long id,
			@Param("name") String name, 
			@Param("mediaId") Long mediaId,
			@Param("durationMin") Long durationMin,
			@Param("durationMax") Long durationMax,
			Pageable p);
	
	public Playlist findByName( String name);
	
	@Modifying
	@Transactional
	@Query(value="delete from programs_playlists where playlist_id = :playlistId", nativeQuery = true)
	public void deletePlaylistProgramLinks(
			@Param("playlistId") Long playlistId);	
	
	@Modifying
	@Transactional
	@Query(value="delete from spots where playlist_id = :playlistId", nativeQuery = true)
	public void deletePlaylistSpotLinks(
			@Param("playlistId") Long playlistId);
		
}