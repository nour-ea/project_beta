package com.platformia.winkwide.entity;

import java.io.Serializable;
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

import com.platformia.winkwide.form.ReportEntryForm;

import lombok.Data;

@Data
@Entity
@Table(name = "Reports", uniqueConstraints={@UniqueConstraint(columnNames={"display_time","display_id", "media_id"})})
public class ReportEntry implements Serializable {


	private static final long serialVersionUID = -4747357433130949274L;

	//Id should be the combination of dates and display id
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "display_time", nullable = false)
	private java.util.Date displayTime;
	
    @Column(name = "display_id", nullable = false)
    private Long displayId;
    
    @Column(name = "media_id", length = 128, columnDefinition = "JSON")
    private String mediaId;


	public ReportEntry() {
		
	}

    public ReportEntry(Date displayTime, Long displayId, String mediaId) {
		this.displayTime = displayTime;
		this.displayId = displayId;
		this.mediaId = mediaId;
	}

    public ReportEntry(ReportEntryForm reportEntryForm ) {
		this.displayTime = reportEntryForm.getDisplayTime();
		this.displayId = reportEntryForm.getDisplayId();
		this.mediaId = reportEntryForm.getMediaId();
	}
    
    public void update(ReportEntryForm reportEntryForm ) {
		this.displayTime = reportEntryForm.getDisplayTime();
		this.displayId = reportEntryForm.getDisplayId();
		this.mediaId = reportEntryForm.getMediaId();
	}

}