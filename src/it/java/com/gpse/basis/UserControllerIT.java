package com.gpse.basis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SesamApplication.class)
@AutoConfigureMockMvc
public class UserControllerIT {

    @Autowired
    private MockMvc mvc; //<4>

    @Test
    void showUsers() throws Exception {
        this.mvc.perform(get("/api/users")) //<5>
        .andExpect(status().isOk()) //<6>
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) //<7>
        .andExpect(content().string(containsString("admin")));
    }
}
