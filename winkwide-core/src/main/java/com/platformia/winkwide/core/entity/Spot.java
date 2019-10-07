package com.platformia.winkwide.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.platformia.winkwide.core.model.Auditable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Spots")
public class Spot extends Auditable implements Serializable {

	private static final long serialVersionUID = 1671308575518906659L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "duration", nullable=false)
	private Long duration;
	
	@Column(name = "play_order", nullable=false)
	private int playOrder;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="playlist_id")
    private Playlist playlist;
	
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="media_id")
    private Media media;

}