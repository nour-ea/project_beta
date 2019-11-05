package com.platformia.winkwide.core.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "winkwide.app.settings")
public class AppSettingsProperties {

	//WinkWide App Settings
	private int syncPeriod;
	private int refreshPeriod;
	private boolean recordingActive;
	private boolean remoteLoggingActive;
	private boolean offlineModeActive;
	private boolean autoSleepActive;
	private String autoOnTime;
	private String autoOffTime;
	private boolean alternateMediaCategories;

}
