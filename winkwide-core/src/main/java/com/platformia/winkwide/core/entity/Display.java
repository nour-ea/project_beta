package com.platformia.winkwide.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.platformia.winkwide.core.model.Auditable;
import com.platformia.winkwide.core.utils.MyRandomNumericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Displays")
public class Display extends Auditable implements Serializable {

	private static final long serialVersionUID = 5033990271444402965L;

	@Id
	@GeneratedValue(generator = MyRandomNumericGenerator.generatorName)
    @GenericGenerator(name = MyRandomNumericGenerator.generatorName, strategy = "com.platformia.winkwide.core.utils.MyRandomNumericGenerator")
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "name", length = 128, nullable = false, unique=true)
    private String name;
    
    @Column(name = "areas", length = 128, nullable = false)
    private String area;
    
    @Column(name = "address", length = 128, nullable = false)
    private String address;
    
    @Column(name = "longitude", columnDefinition = "DECIMAL")
    private BigDecimal longitude;
    
    @Column(name = "latitude", columnDefinition = "DECIMAL")
    private BigDecimal latitude;
    
    @Column(name = "brand", length = 128, nullable = false)
    private String brand;
 
    @Column(name = "size", nullable = false)
    private int size;
    
    @Column(name = "mac", length = 128, nullable = false, unique = true)
    private String mac;
	
    @Column(name = "smart", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
    private boolean smart;
    
    @Column(name = "average_audience", nullable = false)
    private int averageAudience;
    
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_sync_time")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm a")
	private Date lastSyncTime;
    
    @OneToMany(mappedBy="display")
    private List<Program> programs;
    
    @OneToMany(mappedBy="display")
    private List<Report> reports;
    
	
}
