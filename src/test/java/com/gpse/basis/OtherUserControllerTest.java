package com.gpse.basis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpse.basis.domain.SesamUser;
import com.gpse.basis.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Using MockMvc to test the controller, the other tests use the controller directly
@SpringBootTest
@AutoConfigureMockMvc
public class OtherUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void testShowUsers() throws Exception {
        SesamUser user1 = new SesamUser();
        SesamUser user2 = new SesamUser();
        when(userService.showUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(user1, user2))));
    }

    @Test
    void testRegisterUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("forename", "Jack");
        request.setParameter("surname", "Reacher");
        request.setParameter("email", "jack@reach.com");
        request.setParameter("password", "test_password");

        when(userService.createUnregisteredUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn("unique_token");

        mockMvc.perform(post("/api/user/registration")
                        .requestAttr("email", "jack@reach.com")
                        .requestAttr("password", "test_password"))
                .andExpect(status().isOk());
    }
}