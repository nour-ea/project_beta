package com.platformia.winkwide.controller;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.dao.GenericHibernateDao;
import com.platformia.winkwide.entity.Product;
import com.platformia.winkwide.form.ProductForm;

@RestController
public class ProductRESTController {

	@Autowired
	private GenericHibernateDao<Product> productDAO;
	
	
	//initiate DAO Classes
	@PostConstruct
	public void init() {
		productDAO.setEntityClass(Product.class);
	}
	
	//VIEW All URL : /products or /products.xml or /products.json
	@RequestMapping(value = "/products", //
			method = RequestMethod.GET, //	
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public List<Product> getProducts(){
				
		List<Product> list = productDAO.findAll();
		return list;
	}
	
	
	//VIEW ONE URL : /product/{productId} or /product/{productId}.xml or /product/{productId}.json
	@RequestMapping(value = "/product/{productId}", //
			method = RequestMethod.GET, //
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public Product getProduct(@PathVariable("productId") Long productId){
		
		return productDAO.findOne(productId);
	}
	
	
	//CREATE URL : /product or /product.xml or /product.json
	@RequestMapping(value = "/product", //
			method = RequestMethod.POST, //
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public void addProduct(@RequestBody ProductForm productForm ){
		
		System.out.println("(Service Side) Creating product with productId: " + productForm.getId());
		productDAO.create(new Product(productForm));
	}

	
	//EDIT URL : /product or /product.xml or /product.json
	@RequestMapping(value = "/product", //
			method = RequestMethod.PUT, //
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public void updateProduct(@RequestBody ProductForm productForm ){
		
		System.out.println("(Service Side) Editing product with productId: " + productForm.getId());
		productDAO.update(new Product(productForm));
	}
	
	
	//DELETE URL : /product/{productId}
	@RequestMapping(value = "/product/{productId}", //
			method = RequestMethod.DELETE, //
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public void deleteProduct(@PathVariable("productId") Long productId ){
		
		System.out.println("(Service Side) Deleting product with productId: " + productId);
		productDAO.deleteById(productId);
	}	
	
}
