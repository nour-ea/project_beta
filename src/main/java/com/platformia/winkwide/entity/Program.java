package com.platformia.winkwide.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.platformia.winkwide.form.ProgramEntryForm;

import lombok.Data;

@Data
@Entity
@Table(name = "Programs", uniqueConstraints={@UniqueConstraint(columnNames={"start_time","end_time","display_id"})})
public class Program implements Serializable {


	private static final long serialVersionUID = -7262692782376526578L;

	//Id should be the combination of dates and display id
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

	@Column(name = "display_id", nullable = false)
	    private Long displayId;
	    
	@Column(name = "media_loop", length = 128, columnDefinition = "JSON")
	    private String mediaLoop;
	    
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_time", nullable = false)
	private java.util.Date startTime;
	
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", nullable = false)
	private java.util.Date endTime;
	
 
 
    @Column(name = "utilization", columnDefinition = "Decimal")
    private BigDecimal utilization;

    
    public Program() {
	
	}

	public Program(Date startTime, Date endTime, Long displayId, String mediaLoop, BigDecimal utilization) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.displayId = displayId;
		this.mediaLoop = mediaLoop;
		this.utilization = utilization;
	}    
    
	public Program(ProgramEntryForm programEntryForm) {
		this.startTime = programEntryForm.getStartTime();
		this.endTime = programEntryForm.getEndTime();
		this.displayId = programEntryForm.getDisplayId();
		this.mediaLoop = programEntryForm.getMediaLoop();
		this.utilization = programEntryForm.getUtilization();
	}

	public void update(ProgramEntryForm programEntryForm) {
		this.startTime = programEntryForm.getStartTime();
		this.endTime = programEntryForm.getEndTime();
		this.displayId = programEntryForm.getDisplayId();
		this.mediaLoop = programEntryForm.getMediaLoop();
		this.utilization = programEntryForm.getUtilization();
	}

    

}