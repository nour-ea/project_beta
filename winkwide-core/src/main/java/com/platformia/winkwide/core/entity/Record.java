package com.platformia.winkwide.core.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.platformia.winkwide.core.model.Auditable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Records", uniqueConstraints={@UniqueConstraint(columnNames={"start_time","end_time","display_id"})})
public class Record extends Auditable implements Serializable {


	private static final long serialVersionUID = -4747357433130949274L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
	
    @Column(name = "display_id", nullable = false)
    private Long displayId;

    @Column(name = "display_name", nullable = false)
    private String displayName;
           
	@Basic
	@Column(name = "start_time", nullable = false, columnDefinition = "TIMESTAMP")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;
	
	@Basic
	@Column(name = "end_time", nullable = false, columnDefinition = "TIMESTAMP")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;
    
	@Column(name = "display_time")
	private Long displayTime;
	
    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "media_name", nullable = false)
    private String mediaName;
        
}