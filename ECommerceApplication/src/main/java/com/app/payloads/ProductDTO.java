package com.app.payloads;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

	private Long productId;

	@NotBlank
	@Size(min = 3, message = "Product name must contain atleast 3 characters")
	private String productName;

	private String image;

	@NotBlank
	@Size(min = 6, message = "Product description must contain atleast 6 characters")
	private String description;

	@Min(0)
	private Integer quantity;

	@Min(0)
	private Double price;

	@Min(0)
	private Double discount;

	@Min(0)
	private Double specialPrice;

}
