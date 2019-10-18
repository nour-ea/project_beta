package com.platformia.winkwide.core.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.platformia.winkwide.core.model.Auditable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor  
@Entity
@Table(name = "Medias")
public class Media extends Auditable implements Serializable {

	private static final long serialVersionUID = -5558760606017435981L;
	public static final String[] allowedMediaFormats = {"image/jpeg","image/png","image/gif","video/mp4","audio/mp3","audio/mpeg","text/html"};
	public static final String[] allowedThumbnailFormats = {"image/jpeg","image/png", "image/gif"};
	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "name", length = 128, nullable = false, unique=true)
    private String name;
    
    @Column(name = "category", length = 128, nullable = false)
    private String category;
    
    @Column(name = "type", length = 128, nullable = false)
    private String type;
    
    @Column(name = "format", length = 128, nullable = false)
    private String format;
    
    @Column(name = "url", length = 256, nullable = false)
    private String url;
    
    @Column(name = "thumb_url", length = 256, nullable = false)
    private String thumbUrl;
 
    @Column(name = "size", nullable = false)
    private Long size;
	
    @Column(name = "verified", length = 1, nullable = false, columnDefinition = "TINYINT(1)")
    private boolean verified;

    @OneToMany(mappedBy="media", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OnDelete(action=OnDeleteAction.CASCADE)
    private List<Spot> spots;
    
    @OneToMany(mappedBy="media", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private List<Report> reports;
    
}
