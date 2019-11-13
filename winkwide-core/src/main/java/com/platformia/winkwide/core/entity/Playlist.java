package com.platformia.winkwide.core.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.platformia.winkwide.core.model.Auditable;
import com.platformia.winkwide.core.utils.MyRandomNumericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Playlists")
public class Playlist extends Auditable implements Serializable {

	private static final long serialVersionUID = -8175908452018272123L;

	@Id
	@GeneratedValue(generator = MyRandomNumericGenerator.generatorName)
    @GenericGenerator(name = MyRandomNumericGenerator.generatorName, strategy = "com.platformia.winkwide.core.utils.MyRandomNumericGenerator")
    @Column(name = "id", nullable = false)
    private Long id;
	
    @Column(name = "name", length = 128, nullable = false, unique=true)
    private String name;
    
	@Column(name = "duration")
	private Long duration;
    
    @OneToMany(mappedBy="playlist", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OnDelete(action=OnDeleteAction.CASCADE)
	private List<Spot> spots;
    
    @ManyToMany(mappedBy = "playlists", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private List<Program> programs;
    
}