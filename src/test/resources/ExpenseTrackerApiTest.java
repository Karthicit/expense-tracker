package com.karthi.expense_tracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseTrackerApiTest {

	@Autowired
	private MockMvc mvc;

	private String registerAndGetToken(String email) throws Exception {
		String body = mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	@Test
	void registerThenLogin() throws Exception {
		registerAndGetToken("login@test.com");

		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"login@test.com\",\"password\":\"secret123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());

		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"login@test.com\",\"password\":\"wrong-password\"}"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void protectedRouteNeedsValidToken() throws Exception {
		mvc.perform(get("/api/categories"))
				.andExpect(status().isUnauthorized());

		mvc.perform(get("/api/categories").header("Authorization", "Bearer garbage.token.value"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void usersOnlySeeTheirOwnCategories() throws Exception {
		String tokenA = registerAndGetToken("usera@test.com");
		String tokenB = registerAndGetToken("userb@test.com");

		mvc.perform(post("/api/categories")
				.header("Authorization", "Bearer " + tokenA)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Private\"}"))
				.andExpect(status().is2xxSuccessful());

		mvc.perform(get("/api/categories").header("Authorization", "Bearer " + tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Private"));

		mvc.perform(get("/api/categories").header("Authorization", "Bearer " + tokenB))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}
}