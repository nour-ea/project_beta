package com.platformia.winkwide.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Displays")
public class Display implements Serializable {

	private static final long serialVersionUID = 5033990271444402965L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "name", length = 128, nullable = false, unique=true)
    private String name;
    
    @Column(name = "address", length = 128, nullable = false)
    private String address;
    
    @Column(name = "brand", length = 128, nullable = false)
    private String brand;
 
    @Column(name = "size", nullable = false)
    private int size;
    
    @Column(name = "shop_coverage", nullable = false, columnDefinition = "DECIMAL")
    private BigDecimal shopCoverage;
    
    @Column(name = "mac", length = 128, nullable = false, unique = true)
    private String mac;
	
    @Column(name = "smart", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
    private boolean smart;
    
    @OneToMany(mappedBy="display")
    private List<Program> programs;
    
    @OneToMany(mappedBy="display")
    private List<Report> reports;
	
}
