package com.platformia.winkwide.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.platformia.winkwide.entity.Media;
import com.platformia.winkwide.projection.NoMediaVerified;

//@PreAuthorize("hasRole('ROLE_ADMIN')")
@RepositoryRestResource(excerptProjection = NoMediaVerified.class)
public interface MediaRepository extends JpaRepository<Media, Long> {}