package com.platformia.winkwide.core.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.platformia.winkwide.core.model.Auditable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Programs")
public class Program extends Auditable implements Serializable {


	private static final long serialVersionUID = -7262692782376526578L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
	@Column(name = "name", length = 128, nullable = false, unique=true)
    private String name;
    
	@Basic
	@Column(name = "start_time", nullable = false, columnDefinition = "TIMESTAMP")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime startTime;
	
	@Basic
	@Column(name = "end_time", nullable = false, columnDefinition = "TIMESTAMP")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime endTime;
        
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(
        name = "Programs_Displays", 
        joinColumns = { @JoinColumn(name = "program_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "display_id") })
    private List<Display> displays;
    
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(
        name = "Programs_Playlists", 
        joinColumns = { @JoinColumn(name = "program_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "playlist_id") })
    private List<Playlist> playlists;
 
}