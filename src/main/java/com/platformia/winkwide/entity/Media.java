package com.platformia.winkwide.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor  
@Entity
@Table(name = "Medias")
public class Media implements Serializable {

	private static final long serialVersionUID = -5558760606017435981L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "name", length = 128, nullable = false, unique=true)
    private String name;
    
    @Column(name = "media_type", length = 128, nullable = false)
    private String mediaType;
    
    @Column(name = "format", length = 128, nullable = false)
    private String format;
    
    @Column(name = "url", length = 128, nullable = false)
    private String url;
 
    @Column(name = "size", nullable = false)
    private int size;
	
    @Column(name = "verified", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
    @JsonIgnore
    private boolean verified;
    
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(
        name = "Medias_Programs", 
        joinColumns = { @JoinColumn(name = "media_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "program_id") }
    )
    private List<Program> programs;
    
    @OneToMany(mappedBy="media", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private List<Report> reports;
    
    
}
