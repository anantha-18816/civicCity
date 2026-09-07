package com.civicAI.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComplaintWorkflowIntegrationTests {

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
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String officerToken() throws Exception {
        return login(OFFICER_EMAIL, OFFICER_PASSWORD);
    }

    private Citizen registerCitizen() throws Exception {
        String email = "wf-" + System.nanoTime() + "@example.com";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"WF Citizen\",\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Citizen(body.get("token").asText(), body.get("userId").asLong(), email);
    }

    private long createComplaint(Citizen citizen) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + citizen.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + citizen.userId
                                + ",\"issueType\":\"POTHOLE\",\"title\":\"Workflow complaint\","
                                + "\"description\":\"integration flow\",\"latitude\":17.385,\"longitude\":78.4867}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @Rollback
    void createComplaintPersistsSpatialAndTimestamps() throws Exception {
        Citizen citizen = registerCitizen();
        mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + citizen.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + citizen.userId
                                + ",\"issueType\":\"POTHOLE\",\"title\":\"Persistence\","
                                + "\"latitude\":17.385,\"longitude\":78.4867}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.issueType").value("POTHOLE"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.latitude").value(17.385))
                .andExpect(jsonPath("$.longitude").value(78.4867))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @Rollback
    void createComplaintRejectsInvalidPayload() throws Exception {
        Citizen citizen = registerCitizen();
        mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + citizen.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"no coordinates\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Rollback
    void getComplaintByIdReturnsRecord() throws Exception {
        Citizen citizen = registerCitizen();
        long id = createComplaint(citizen);
        mockMvc.perform(get("/api/complaints/" + id)
                        .header("Authorization", "Bearer " + citizen.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @Rollback
    void getMissingComplaintReturnsNotFound() throws Exception {
        Citizen citizen = registerCitizen();
        mockMvc.perform(get("/api/complaints/999999")
                        .header("Authorization", "Bearer " + citizen.token))
                .andExpect(status().isNotFound());
    }

    @Test
    @Rollback
    void complaintEndpointsRejectAnonymous() throws Exception {
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"issueType\":\"POTHOLE\",\"title\":\"anon\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/complaints/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Rollback
    void invalidStatusTransitionRejectedWithConflict() throws Exception {
        Citizen citizen = registerCitizen();
        long id = createComplaint(citizen);
        String token = officerToken();
        mockMvc.perform(patch("/api/complaints/" + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @Rollback
    void validStatusTransitionAndAssignAndResolve() throws Exception {
        Citizen citizen = registerCitizen();
        long id = createComplaint(citizen);
        String token = officerToken();

        mockMvc.perform(patch("/api/complaints/" + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"AI_ANALYZED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AI_ANALYZED"));

        mockMvc.perform(post("/api/complaints/" + id + "/assign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentId").value(1));

        mockMvc.perform(patch("/api/complaints/" + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/complaints/" + id + "/resolution")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"streets department patched it\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.complaintId").value(id));

        MvcResult resolved = mockMvc.perform(get("/api/complaints/" + id)
                        .header("Authorization", "Bearer " + citizen.token))
                .andExpect(status().isOk()).andReturn();
        JsonNode body = objectMapper.readTree(resolved.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals("RESOLVED", body.get("status").asText());
    }

    private record Citizen(String token, long userId, String email) {
    }
}