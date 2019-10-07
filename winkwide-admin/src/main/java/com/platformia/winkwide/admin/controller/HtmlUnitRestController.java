package com.platformia.winkwide.admin.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.platformia.winkwide.core.exception.FileStorageException;
import com.platformia.winkwide.core.utils.FileStorageProperties;

@RestController
public class HtmlUnitRestController {
/*
	private final Path fileStorageLocation;

	@Autowired
	public HtmlUnitRestController(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getDownloadDir()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}

	@PostMapping(value = "/api/storeWebsite")
	public String storeWebsite(@RequestBody String url) {

		String content = "not found";
		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		//webClient.getOptions().setTimeout(5000);

		try {
			HtmlPage page = (HtmlPage) webClient.getPage(url);
			//webClient.waitForBackgroundJavaScriptStartingBefore(3000);
			//webClient.waitForBackgroundJavaScript(3000);

			final List<HtmlScript> scripts = page.getByXPath("//script");
			final List<HtmlLink> styles = page.getByXPath("//link[@rel='stylesheet']");
			final List<HtmlImage> images = page.getByXPath("//img");

			String fileName = "";
			String domain = page.getUrl().getHost();
			String protocol = page.getUrl().getProtocol();
			ArrayList<String> cssImgUrls = new ArrayList<String>();

			for (HtmlScript script : scripts) {
				fileName = script.getAttribute("src");
				if (!fileName.contains("//")) {
					storeFile(protocol, domain, fileName);
				}
			}

			for (HtmlLink style : styles) {
				fileName = style.getAttribute("href");
				if (!fileName.contains("//")) {
					storeFile(protocol, domain, fileName);
					cssImgUrls = parseCssImageFile(page.getBaseURI() + fileName);
					if(!cssImgUrls.isEmpty())
						for (String cssImgUrl : cssImgUrls) {
							storeFile(protocol, domain, cssImgUrl);
						}
					cssImgUrls.clear();
				}
			}

			for (HtmlImage image : images) {
				fileName = image.getAttribute("src");
				if (!fileName.contains("//")) {
					storeFile(protocol, domain, fileName);
				}
			}

			storeFile(page.getUrl().toString(), "index.html", domain);

			// page.save(new File(Paths.get(fileStorageLocation.toString(), domain,
			// "index.html").toString()));

			content = page.getWebResponse().getContentAsString();

		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("Error :"+e.getMessage());
		}

		webClient.close();
		return content;
	}

	void storeFile(String protocol, String domain, String fileName) {

		InputStream in;
		Path sourcePath = Paths.get(domain, fileName);
		Path targetPath = Paths.get(fileStorageLocation.toString(), domain, fileName);

		try {
			in = new URL(protocol+"://"+ sourcePath.toString()).openStream();
			FileUtils.forceMkdirParent(new File(targetPath.toString()));
			Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("wrote file : " + fileName + " in folder : " + domain);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	ArrayList<String> parseCssImageFile(String fileUrl) {
		ArrayList<String> urls = new ArrayList<String>();
		String line = "";
		String url = "";

		try {
			InputStream in = new URL(fileUrl).openStream();
			InputStreamReader isr = new InputStreamReader(in, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);

			while ((line = br.readLine()) != null) {
				if (line.contains("url(")) {
					Pattern p1 = Pattern.compile("url[(]" + "[^)]*" + "[\\.](gif|png|jpg|jpeg)");
					Matcher m1 = p1.matcher(line);
					if (m1.find()) {
						url = line.substring(m1.start() + 4, m1.end()) ;
						Pattern p2 = Pattern.compile("[a-zA-Z].*");
						Matcher m2 = p2.matcher(url);
						if(m2.find())
							url = url.substring(m2.start(), m2.end()) ;
						urls.add(url);
					}
						
				}

			}
			
			System.out.println("Parsed CSS file: " + urls.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return urls;
	}
*/
}