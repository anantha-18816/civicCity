package com.civicAI.backend;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTests {

    private static final String OFFICER_EMAIL = "officer@civicai.test";
    private static final String OFFICER_PASSWORD = "officer123";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    @Test
    @Rollback
    void registerReturnsTokenAndCitizenRole() throws Exception {
        String email = "citizen-it-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"IT Citizen\",\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("CITIZEN"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$", hasKey("userId")))
                .andExpect(jsonPath("$", not(hasKey("passwordHash"))));
    }

    @Test
    @Rollback
    void registerDuplicateEmailReturnsConflict() throws Exception {
        String email = "dup-" + System.nanoTime() + "@example.com";
        String body = "{\"name\":\"A\",\"email\":\"" + email + "\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginSucceedsForSeededOfficer() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + OFFICER_EMAIL + "\",\"password\":\"" + OFFICER_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OFFICER"));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + OFFICER_EMAIL + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void publicHealthEndpointIsReachableWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/complaints")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/dashboard/map")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/departments")).andExpect(status().isUnauthorized());
    }

    @Test
    @Rollback
    void citizenWithoutOfficerRoleIsForbiddenFromAssignment() throws Exception {
        String email = "citizen-403-" + System.nanoTime() + "@example.com";
        Citizen citizen = registerCitizen(email);

        long complaintId = createComplaintAs(citizen.token, citizen.userId);

        mockMvc.perform(post("/api/complaints/" + complaintId + "/assign")
                        .header("Authorization", "Bearer " + citizen.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Rollback
    void officerCanAssignComplaint() throws Exception {
        String email = "citizen-assign-" + System.nanoTime() + "@example.com";
        Citizen citizen = registerCitizen(email);
        long complaintId = createComplaintAs(citizen.token, citizen.userId);

        String officerToken = login(OFFICER_EMAIL, OFFICER_PASSWORD);
        mockMvc.perform(post("/api/complaints/" + complaintId + "/assign")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    @Rollback
    void citizenCannotSubmitResolution() throws Exception {
        String email = "citizen-res-" + System.nanoTime() + "@example.com";
        Citizen citizen = registerCitizen(email);
        long complaintId = createComplaintAs(citizen.token, citizen.userId);

        String officerToken = login(OFFICER_EMAIL, OFFICER_PASSWORD);
        mockMvc.perform(post("/api/complaints/" + complaintId + "/assign")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/complaints/" + complaintId + "/resolution")
                        .header("Authorization", "Bearer " + citizen.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"sneaky\"}"))
                .andExpect(status().isForbidden());
    }

    private Citizen registerCitizen(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"IT Citizen\",\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Citizen(body.get("token").asText(), body.get("userId").asLong());
    }

    private long createComplaintAs(String token, long userId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + userId
                                + ",\"issueType\":\"POTHOLE\",\"title\":\"IT complaint\","
                                + "\"description\":\"integration test\",\"latitude\":17.385,\"longitude\":78.4867}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private record Citizen(String token, long userId) {
    }
}