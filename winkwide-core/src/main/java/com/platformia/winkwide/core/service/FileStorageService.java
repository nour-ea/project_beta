package com.platformia.winkwide.core.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import com.platformia.winkwide.core.exception.FileStorageException;
import com.platformia.winkwide.core.model.FileProperties;
import com.platformia.winkwide.core.utils.FileStorageProperties;

@Service
@EnableConfigurationProperties({FileStorageProperties.class})
public class FileStorageService {

	private final Path fileStorageLocation;
	private final Path logStorageLocation;

	@Autowired
	public FileStorageService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		this.logStorageLocation = Paths.get(fileStorageProperties.getLogDir()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.fileStorageLocation);
			Files.createDirectories(this.logStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files or downloaded logs will be stored.",
					ex);
		}
	}

	public FileProperties storeFile(String name, String location, MultipartFile file) {

		FileProperties fileProperties = new FileProperties();
		
    	// Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(!name.isEmpty()) fileName = name;
        String format = file.getContentType();
        long size = file.getSize()/1000;

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
            Path relativeTargetLocation = Paths.get(location, year, month, random +"_"+ fileName);
            Path targetLocation = Paths.get(this.fileStorageLocation.toString(), relativeTargetLocation.toString());
            
            // Create target Folder
            FileUtils.forceMkdirParent(new File(targetLocation.toString()) );
            
            // Create File
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("stored file :" + relativeTargetLocation);
            String url = relativeTargetLocation.toString();
            
            //Populate fileProperties
            fileProperties.setName(fileName);
            fileProperties.setFormat(format);
            fileProperties.setSize(size);
            fileProperties.setUrl(url);
            
            return fileProperties;
        } catch (Exception ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again or contact your Admin!", ex);
        }
    }

	public void deleteFile(String url) {


        try {
            // Build Target Location
        	Path targetLocation = Paths.get(this.fileStorageLocation.toString(), url);
            
            // Delete File
        	if(Files.exists(targetLocation)) {
        		Files.delete(targetLocation);
            	System.out.println("deleted file :" + url);        		
        	}
            
        } catch (Exception ex) {
            throw new FileStorageException("Could not delete file at " + url + ". Please try again or contact your Admin!", ex);
        }
    }
	
	public void writeTextInLogFile(String fileName, String text) {

        try {
        	Path targetLocation = Paths.get(this.logStorageLocation.toString(), fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(targetLocation.toString(), true));
            writer.append('\n');
            writer.append(text);           
            writer.close();
            
        } catch (Exception ex) {
            throw new FileStorageException("Could not write in text in log file : " + fileName + ". Please try again or contact your Admin!", ex);
        }
    }

}