package com.platformia.winkwide.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.platformia.winkwide.form.DisplayForm;

import lombok.Data;

@Data
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
    
    @Column(name = "address", length = 128, nullable = false, columnDefinition = "JSON")
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
    
    
    
    public Display() {

	}
    
    
	public Display(Long id, String name, String address, String brand, int size, BigDecimal shopCoverage, String mac, boolean smart) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
		this.brand = brand;
		this.size = size;
		this.shopCoverage = shopCoverage;
		this.mac = mac;
		this.smart = smart;
	}

	public Display(DisplayForm displayForm) {
		this.name = displayForm.getName();
		this.address = displayForm.getAddress();
		this.brand = displayForm.getBrand();
		this.size = displayForm.getSize();
		this.shopCoverage = displayForm.getShopCoverage();
		this.mac = displayForm.getMac();
		this.smart = displayForm.isSmart();
	}
	
	public void update(DisplayForm displayForm) {
		this.name = displayForm.getName();
		this.address = displayForm.getAddress();
		this.brand = displayForm.getBrand();
		this.size = displayForm.getSize();
		this.shopCoverage = displayForm.getShopCoverage();
		this.mac = displayForm.getMac();
		this.smart = displayForm.isSmart();
	}
	
}
