package com.platformia.winkwide.core.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.platformia.winkwide.core.entity.Media;
import com.platformia.winkwide.core.exception.FileStorageException;
import com.platformia.winkwide.core.utils.FileStorageProperties;

@Service
@EnableConfigurationProperties({FileStorageProperties.class})
public class MediaFileStorageService {

	private final Path fileStorageLocation;
	private final String fileDownloadServer;

	@Autowired
	public MediaFileStorageService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		this.fileDownloadServer = fileStorageProperties.getDownloadServer();

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}

	public Media storeFile(String name, String mediaType, MultipartFile file) {

    	// Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String format = file.getContentType();
        long size = file.getSize()/1000;
        boolean verified = false;

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            //Generate random path and calendar folders
            String random = RandomStringUtils.randomAlphanumeric(10);
            String year = new SimpleDateFormat("YYYY").format(Calendar.getInstance().getTime());
            String month = new SimpleDateFormat("MMM").format(Calendar.getInstance().getTime());

            // Build Target Location (relative and absolute)
            Path relativeTargetLocation = Paths.get("/uploads/medias", mediaType, year, month, random +"_"+ fileName);
            Path targetLocation = Paths.get(this.fileStorageLocation.toString(), relativeTargetLocation.toString());
            
            // Create target Folder
            FileUtils.forceMkdirParent(new File(targetLocation.toString()) );
            
            // Create File
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            String url = this.fileDownloadServer + relativeTargetLocation.toString();

            //Create Media object
            Media media = new Media();
            media.setName(name);
            media.setMediaType(mediaType);
            media.setFormat(format);
            media.setUrl(url);
            media.setSize(size);
            media.setVerified(verified);
            
            return media;
        } catch (Exception ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

	public void deleteFile(String url) {


        try {
            // Build Target Location (absolute)
            Path targetLocation = Paths.get(this.fileStorageLocation.toString(), url.replaceFirst(this.fileDownloadServer.toString(), ""));
            
            // Delete File
            Files.delete(targetLocation);
            
        } catch (Exception ex) {
            throw new FileStorageException("Could not delete file at " + url + ". Please try again!", ex);
        }
    }

}