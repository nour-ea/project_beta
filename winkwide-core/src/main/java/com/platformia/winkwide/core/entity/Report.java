package com.platformia.winkwide.core.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Table(name = "Reports", uniqueConstraints={@UniqueConstraint(columnNames={"start_time","end_time","display_id"})})
public class Report implements Serializable {


	private static final long serialVersionUID = -4747357433130949274L;

	//Id should be the combination of dates and display id
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
    
    @ManyToOne
    @JoinColumn(name="media_id")
    private Media media;

}