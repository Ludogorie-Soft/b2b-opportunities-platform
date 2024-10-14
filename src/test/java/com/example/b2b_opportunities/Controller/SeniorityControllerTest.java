package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.BaseTest;
import com.example.b2b_opportunities.Entity.Seniority;
import com.example.b2b_opportunities.Repository.SeniorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
public class SeniorityControllerTest extends BaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeniorityRepository seniorityRepository;

    @BeforeEach
    void setupSeniority() {
        seniorityRepository.deleteAll();

        seniorityRepository.save(Seniority.builder().id(1L).label("Primary").level((short) 1).build());
        seniorityRepository.save(Seniority.builder().id(2L).label("Secondary").level((short) 2).build());
        seniorityRepository.save(Seniority.builder().id(3L).label("Intermediate").level((short) 3).build());
    }

    private ResultActions performGetResult(String url) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllSenioritySuccessfullyTest() throws Exception {
        performGetResult("/seniorities")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].label").value("Primary"))
                .andExpect(jsonPath("$[0].level").value(1))
                .andExpect(jsonPath("$[1].label").value("Secondary"))
                .andExpect(jsonPath("$[1].level").value(2))
                .andExpect(jsonPath("$[2].label").value("Intermediate"))
                .andExpect(jsonPath("$[2].level").value(3));
    }

    @Test
    void getSeniorityByIdWithDifferentIdsSuccessfullyTest() throws Exception {
        performGetResult("/seniorities/2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Secondary"))
                .andExpect(jsonPath("$.level").value(2));
    }

    @Test
    void getSeniorityByIdShouldBeNotExistingSeniorityTest() throws Exception {
        performGetResult("/seniorities/619")
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllSeniorityWhenEmptyShouldReturnEmptyListTest() throws Exception {
        seniorityRepository.deleteAll();

        performGetResult("/seniorities")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
