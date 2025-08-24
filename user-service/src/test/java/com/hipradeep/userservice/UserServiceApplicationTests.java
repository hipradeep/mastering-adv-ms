package com.hipradeep.userservice;

import com.hipradeep.common.dto.ApiResponse;
import com.hipradeep.common.util.JsonUtils;
import com.hipradeep.userservice.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceApplicationTests {

	@Test
	void testCommonLibraryIntegration() {
		// Test ApiResponse from common lib
		ApiResponse<String> response = ApiResponse.success("Test message");
		assertTrue(response.isSuccess());
		assertEquals("Test message", response.getData());

		// Test JsonUtils from common lib
		UserResponse user = new UserResponse(1L, "testuser", "test@example.com", "Test", "User");
		String json = JsonUtils.toJson(user);
		assertNotNull(json);
		assertTrue(json.contains("testuser"));
	}

	@Test
	void testApiResponseStructure() {
		ApiResponse<UserResponse> response = ApiResponse.success(
				new UserResponse(1L, "john", "john@email.com", "John", "Doe")
		);

		assertTrue(response.isSuccess());
		assertEquals("Success", response.getMessage());
		assertNull(response.getErrorCode());
		assertNotNull(response.getData());
		assertEquals("john", response.getData().getUsername());
	}
}