package model;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Products")
public class Product {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer productId;

@Column(nullable=false)
private String productname;

@Column(nullable = false)
private Integer quantity;

@Column(nullable = false, precision = 10, scale = 2)
private BigDecimal price;

@Column(nullable = false)
private Integer stock;

@OneToMany(mappedBy = "product")
private List<OrderItem> orderItems;

public Product() {
}

public Product(Integer quantity, BigDecimal price, Integer stock,String productname) {
    this.quantity=quantity;
    this.price = price;
    this.stock = stock;
    this.productname=productname;
}

public Integer getProductId() {
	return productId;
}

public void setProductId(Integer productId) {
	this.productId = productId;
}

public Integer getQuantity() {
	return quantity;
}
public void setQuantity(Integer quantity) {
	this.quantity=quantity;
}
public BigDecimal getPrice() {
	return price;
}

public void setPrice(BigDecimal price) {
	this.price = price;
}

public Integer getStock() {
	return stock;
}

public void setStock(Integer stock) {
	this.stock=stock;
}



public List<OrderItem> getOrderItems() {
	return orderItems;
}

public void setOrderItems(List<OrderItem> orderItems) {
	this.orderItems = orderItems;
}

public String getProductname() {
	return productname;
}

public void setProductname(String productname) {
	this.productname = productname;
}


}

