package com.temperomineiro.erp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.temperomineiro.erp.dto.AuthDto;
import com.temperomineiro.erp.model.DomainEnums.RoleName;
import com.temperomineiro.erp.model.Role;
import com.temperomineiro.erp.repository.RoleRepository;
import com.temperomineiro.erp.service.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService.resetAll();

        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(Role.builder()
                    .name(roleName)
                    .description("Perfil " + roleName.name())
                    .build()));
        }
    }

    @Test
    void shouldRegisterAndLoginRestaurantAdmin() throws Exception {
        AuthDto.RegisterRestaurantRequest registerRequest = new AuthDto.RegisterRestaurantRequest(
                "Tempero Teste",
                "tempero-teste",
                "Admin Teste",
                "admin.teste@temperomineiro.com",
                "Senha@2026"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("admin.teste@temperomineiro.com"));

        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest("admin.teste@temperomineiro.com", "Senha@2026");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.nome").value("Admin Teste"));
    }

    @Test
    void shouldRejectWeakPasswordsOnRestaurantRegistration() throws Exception {
        AuthDto.RegisterRestaurantRequest registerRequest = new AuthDto.RegisterRestaurantRequest(
                "Tempero Fraco",
                "tempero-fraco",
                "Admin Fraco",
                "admin.fraco@temperomineiro.com",
                "12345678"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha deve ter entre 8 e 72 caracteres e incluir letra maiúscula, letra minúscula, número e símbolo."));
    }

    @Test
    void shouldTemporarilyBlockLoginAfterTooManyFailures() throws Exception {
        AuthDto.RegisterRestaurantRequest registerRequest = new AuthDto.RegisterRestaurantRequest(
                "Tempero Seguro",
                "tempero-seguro",
                "Admin Seguro",
                "admin.seguro@temperomineiro.com",
                "Senha@2026"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        AuthDto.LoginRequest invalidLoginRequest = new AuthDto.LoginRequest("admin.seguro@temperomineiro.com", "SenhaErrada@2026");
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                    .andExpect(status().isBadRequest());
        }

        AuthDto.LoginRequest validLoginRequest = new AuthDto.LoginRequest("admin.seguro@temperomineiro.com", "Senha@2026");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Muitas tentativas de login. Aguarde 15 minutos e tente novamente."));
    }
}
