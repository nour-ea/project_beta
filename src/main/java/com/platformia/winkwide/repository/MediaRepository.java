package com.platformia.winkwide.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.platformia.winkwide.entity.Media;

@RepositoryRestResource(excerptProjection = NoMediaVerified.class)
public interface MediaRepository extends JpaRepository<Media, Long> {}