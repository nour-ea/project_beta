package com.platformia.winkwide.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platformia.winkwide.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long> {}
