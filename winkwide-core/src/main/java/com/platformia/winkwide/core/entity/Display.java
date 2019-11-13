package com.platformia.winkwide.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

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
    
    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "category", length = 128, nullable = false)
    private String category;
    
    @Column(name = "area", length = 128, nullable = false)
    private String area;
    
	@Basic
	@Column(name = "last_sync_time", columnDefinition = "TIMESTAMP")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime lastSyncTime;
    
    @Column(name = "weekday_audience", nullable = false)
    private int weekdayAudience;
    
    @Column(name = "holiday_audience", nullable = false)
    private int holidayAudience;

    @Column(name = "weekday_visitors", nullable = false)
    private int weekdayVisitors;
    
    @Column(name = "holiday_Visitors", nullable = false)
    private int holidayVisitors;
    
    @Column(name = "phone", length = 128, nullable = false)
    private String phone;
	
    @Column(name = "address", length = 128, nullable = false)
    private String address;
    
    @Column(name = "longitude", columnDefinition = "DECIMAL(10,5)")
    private BigDecimal longitude;
    
    @Column(name = "latitude", columnDefinition = "DECIMAL(10,5)")
    private BigDecimal latitude;
    
    @Column(name = "brand", length = 128, nullable = false)
    private String brand;
 
    @Column(name = "size", nullable = false)
    private int size;
    
    @Column(name = "mac", length = 128, nullable = false, unique = true)
    private String mac;
	
    @Column(name = "smart", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
    private boolean smart;
 
    @ManyToMany(mappedBy = "displays", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private List<Program> programs;
	
}
