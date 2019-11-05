package com.platformia.winkwide.core.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "winkwide.admin.settings")
public class AdminSettingsProperties {

	//WinkWide Admin Settings
	private int maxStorageSize;
	private int maxDisplays;
	private int maxMedias;
	private int maxDisplaySpots;
	private int maxDisplayLoopTime;
	private int displaySyncWarningDelay;

}
