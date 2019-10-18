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


	private final FileStorageProperties fileStorageProperties;
	private final Path uploadsStorageLocation;
	private final Path logsStorageLocation;

	@Autowired
	public FileStorageService(FileStorageProperties fsp) {
		this.fileStorageProperties = fsp;
		this.uploadsStorageLocation = Paths.get(this.fileStorageProperties.getRootDir(), this.fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		this.logsStorageLocation = Paths.get(this.fileStorageProperties.getRootDir(), this.fileStorageProperties.getLogDir()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.uploadsStorageLocation);
			Files.createDirectories(this.logsStorageLocation);
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
            Path targetLocation = Paths.get(this.uploadsStorageLocation.toString(), relativeTargetLocation.toString());
            
            // Create target Folder
            FileUtils.forceMkdirParent(new File(targetLocation.toString()) );
            
            // Create File
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String url = Paths.get(fileStorageProperties.getUploadDir(), relativeTargetLocation.toString()).toString();
            System.out.println("stored file :" + url);
            
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
        	Path targetLocation = Paths.get(this.uploadsStorageLocation.toString(), url);
            
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
        	Path targetLocation = Paths.get(this.logsStorageLocation.toString(), fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(targetLocation.toString(), true));
            writer.append('\n');
            writer.append(text);           
            writer.close();
            
        } catch (Exception ex) {
            throw new FileStorageException("Could not write in text in log file : " + fileName + ". Please try again or contact your Admin!", ex);
        }
    }
	
	public long getUploadsDirectorySize() {

        try {
        	Path targetFolder = Paths.get(this.uploadsStorageLocation.toString());
            long size = Files.walk(targetFolder)
            	      .filter(p -> p.toFile().isFile())
            	      .mapToLong(p -> p.toFile().length())
            	      .sum();
            return size;
            
        } catch (Exception ex) {
            throw new FileStorageException("Could not get Upload Directory size fo", ex);
        }
    }

}