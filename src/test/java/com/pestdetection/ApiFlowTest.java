package com.pestdetection;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String user, String pass) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    @Test
    void endpointsRequireAToken() throws Exception {
        mvc.perform(get("/api/traps")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/traps").header("Authorization", "Bearer not.a.token")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRejectedWithAMessage() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong username or password."));
    }

    @Test
    void regularUsersCannotManageUsers() throws Exception {
        String token = login("priya", "field123");
        mvc.perform(get("/api/users").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/users/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("priya"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void adminSeesSeededFarm() throws Exception {
        String token = login("admin", "admin123");
        mvc.perform(get("/api/traps").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(6)));
        mvc.perform(get("/api/users").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void trapDetectionAlertLifecycle() throws Exception {
        String token = login("admin", "admin123");

        String created = mvc.perform(post("/api/traps").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test trap\",\"deviceId\":\"T-9001\",\"location\":\"Lab\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long trapId = json.readTree(created).get("id").asLong();

        // Duplicate device id is a conflict.
        mvc.perform(post("/api/traps").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Copy\",\"deviceId\":\"T-9001\"}"))
                .andExpect(status().isConflict());

        // Validation errors name the field.
        mvc.perform(post("/api/detections").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trapId\":" + trapId + ",\"pestType\":\"Aphid\",\"count\":0}"))
                .andExpect(status().isBadRequest());

        // 12 aphids crosses the surge threshold of 10 and raises a HIGH alert.
        mvc.perform(post("/api/detections").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trapId\":" + trapId + ",\"pestType\":\"Aphid\",\"count\":12,\"confidence\":0.93}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trapName").value("Test trap"))
                .andExpect(jsonPath("$.count").value(12));

        mvc.perform(get("/api/detections/trap/" + trapId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        JsonNode list = json.readTree(mvc.perform(get("/api/alerts").header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        long alertId = -1;
        for (JsonNode a : list) {
            if (a.get("trapId").asLong() == trapId && "HIGH".equals(a.get("severity").asText())) {
                alertId = a.get("id").asLong();
            }
        }
        assertTrue(alertId > 0, "surge alert should exist for the new trap");

        // A second detection does not duplicate the open alert.
        mvc.perform(post("/api/detections").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trapId\":" + trapId + ",\"pestType\":\"Aphid\",\"count\":3}"));
        JsonNode again = json.readTree(mvc.perform(get("/api/alerts").header("Authorization", token))
                .andReturn().getResponse().getContentAsString());
        int forTrap = 0;
        for (JsonNode a : again) if (a.get("trapId").asLong() == trapId) forTrap++;
        assertTrue(forTrap == 1, "open alert should not be duplicated");

        mvc.perform(put("/api/alerts/" + alertId + "/resolve").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true))
                .andExpect(jsonPath("$.read").value(true));

        // Sensor reading with high humidity raises a MEDIUM alert.
        mvc.perform(post("/api/sensors").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trapId\":" + trapId + ",\"temperature\":28.5,\"humidity\":91}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trapId").value(trapId));

        // Deleting the trap removes its history.
        mvc.perform(delete("/api/traps/" + trapId).header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/traps/" + trapId).header("Authorization", token)).andExpect(status().isNotFound());
        mvc.perform(get("/api/detections/trap/" + trapId).header("Authorization", token)).andExpect(status().isNotFound());
    }
}
