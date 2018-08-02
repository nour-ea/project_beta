package com.platformia.winkwide.entity;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Programs", uniqueConstraints={@UniqueConstraint(columnNames={"start_time","end_time","display_id"})})
public class Program implements Serializable {


	private static final long serialVersionUID = -7262692782376526578L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_time", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm a")
	private Date startTime;
	
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm a")
	private Date endTime;
	
    @ManyToOne
    @JoinColumn(name="display_id")
    private Display display;
    
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(
        name = "Programs_Medias", 
        joinColumns = { @JoinColumn(name = "program_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "media_id") })
    private List<Media> medias;
 
}