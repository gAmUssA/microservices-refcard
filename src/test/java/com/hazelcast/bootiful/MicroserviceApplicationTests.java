package com.hazelcast.bootiful;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MicroserviceApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final String CACHING_URI = "/caching/";

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(
                post(CACHING_URI + "fixture")
                        .param("value", "yay!")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getIsOk() throws Exception {
        mockMvc.perform(
                get(CACHING_URI + "fixture")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("yay!"));
    }

    @Test
    public void postIsOk() throws Exception {
        mockMvc.perform(
                post(CACHING_URI + "1")
                        .param("value", "test")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void putIsOk() throws Exception {
        mockMvc.perform(
                put(CACHING_URI + "fixture")
                        .param("value", "yay!")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("yay!"));
    }

    @Test
    public void patchIsOk() throws Exception {
        mockMvc.perform(
                patch(CACHING_URI + "fixture")
                        .param("oldValue", "yay!")
                        .param("newValue", "yay!")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("true"));
    }
}
