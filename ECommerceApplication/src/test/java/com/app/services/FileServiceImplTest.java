package com.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FileServiceImplTest {

	private final FileServiceImpl fileService = new FileServiceImpl();

	@Test
	void uploadImageCreatesFile(@TempDir Path tempDir) throws Exception {
		MockMultipartFile file = new MockMultipartFile("image", "photo.png", "image/png",
				"test-image-content".getBytes());

		String fileName = fileService.uploadImage(tempDir.toString(), file);

		assertNotNull(fileName);
		assertTrue(Files.exists(tempDir.resolve(fileName)));
	}

	@Test
	void getResourceReturnsInputStream(@TempDir Path tempDir) throws Exception {
		Path imagePath = tempDir.resolve("sample.png");
		Files.writeString(imagePath, "content");

		InputStream inputStream = fileService.getResource(tempDir.toString(), "sample.png");

		assertNotNull(inputStream);
		assertEquals('c', (char) inputStream.read());
		inputStream.close();
	}

}
