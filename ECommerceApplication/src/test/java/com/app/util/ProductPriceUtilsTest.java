package com.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProductPriceUtilsTest {

	@Test
	void calculateSpecialPriceAppliesDiscount() {
		double result = ProductPriceUtils.calculateSpecialPrice(100.0, 10.0);
		assertEquals(90.0, result, 0.001);
	}

	@Test
	void calculateSpecialPriceWithZeroDiscount() {
		double result = ProductPriceUtils.calculateSpecialPrice(50.0, 0.0);
		assertEquals(50.0, result, 0.001);
	}

}
