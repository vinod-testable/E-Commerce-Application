package com.app.util;

public final class ProductPriceUtils {

	private ProductPriceUtils() {
	}

	public static double calculateSpecialPrice(double price, double discount) {
		return price - ((discount * 0.01) * price);
	}

}
