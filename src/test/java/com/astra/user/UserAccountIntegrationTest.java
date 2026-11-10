package com.astra.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.shared.crypto.JwtService;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class UserAccountIntegrationTest {

    private static final String PASSWORD = "Xk9$mQ2vN8pL4wR7";
    private static final String NEW_PASSWORD = "Qz7#nB4tW1sK9pL2";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private record Account(UUID id, String email, String token) {
    }

    private Account register(String name) throws Exception {
        String email = "account-" + UUID.randomUUID() + "@astra.local";
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD
                                + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(JsonPath.read(body, "$.id"));
        return new Account(id, email, jwtService.generateToken(id));
    }

    private ResultActions call(Account account, MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header("Authorization", "Bearer " + account.token());
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private ResultActions upload(Account account, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart("/me/avatar").file(file).header("Authorization", "Bearer " + account.token()));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .with(request -> {
                    request.setRemoteAddr("10.11." + ThreadLocalRandom.current().nextInt(256) + "."
                            + ThreadLocalRandom.current().nextInt(1, 255));
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private void setRole(UUID userId, String role) {
        entityManager.flush();
        jdbcTemplate.update("update users set role = ? where id = ?", role, userId);
        entityManager.clear();
    }

    @Test
    void cadastroDevolveUsuarioComTagECampos() throws Exception {
        String email = "novo-" + UUID.randomUUID() + "@astra.local";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nova\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nova"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.tag").value(org.hamcrest.Matchers.matchesPattern("\\d{4}")))
                .andExpect(jsonPath("$.hasPassword").value(true))
                .andExpect(jsonPath("$.termsAccepted").value(false))
                .andExpect(jsonPath("$.bio").doesNotExist());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Outra\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroValidaNomeEmailESenha() throws Exception {
        for (String body : List.of(
                "{\"name\":\"Com#\",\"email\":\"a@astra.local\",\"password\":\"" + PASSWORD + "\"}",
                "{\"name\":\" \",\"email\":\"a@astra.local\",\"password\":\"" + PASSWORD + "\"}",
                "{\"name\":\"Ok\",\"email\":\"nao-email\",\"password\":\"" + PASSWORD + "\"}",
                "{\"name\":\"Ok\",\"email\":\"a@astra.local\",\"password\":\"curta\"}")) {
            mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void loginComSenhaCertaEErrada() throws Exception {
        Account account = register("Login");

        login(account.email(), PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(account.id().toString()));
        login(account.email(), "senha-errada").andExpect(status().isUnauthorized());
        login("ninguem-" + UUID.randomUUID() + "@astra.local", PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void meDevolveOProprioUsuario() throws Exception {
        Account account = register("Eu");

        call(account, get("/me"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.id().toString()))
                .andExpect(jsonPath("$.email").value(account.email()))
                .andExpect(jsonPath("$.hasPassword").value(true));
    }

    @Test
    void editarPerfilMudaNomeEBioEMantemATagQuandoDa() throws Exception {
        Account account = register("Antes");
        String tag = JsonPath.read(call(account, get("/me"), null).andReturn().getResponse().getContentAsString(),
                "$.tag");

        call(account, put("/me"), "{\"name\":\"Depois\",\"bio\":\"estudando\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Depois"))
                .andExpect(jsonPath("$.bio").value("estudando"))
                .andExpect(jsonPath("$.tag").value(tag));

        call(account, put("/me"), "{\"name\":\"Com#\"}").andExpect(status().isBadRequest());
        call(account, put("/me"), "{\"name\":\"Ok\",\"bio\":\"" + "b".repeat(81) + "\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void editarPerfilTrocaATagSeONovoNomeJaTemEla() throws Exception {
        Account taken = register("Ocupado");
        String takenTag = JsonPath.read(call(taken, get("/me"), null).andReturn().getResponse().getContentAsString(),
                "$.tag");
        Account account = register("Livre");
        entityManager.flush();
        jdbcTemplate.update("update users set tag = ? where id = ?", takenTag, account.id());
        entityManager.clear();

        String body = call(account, put("/me"), "{\"name\":\"Ocupado\",\"bio\":null}")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat((String) JsonPath.read(body, "$.tag")).isNotEqualTo(takenTag).matches("\\d{4}");
    }

    @Test
    void trocarSenhaConfereAAtualEPassaAValerANova() throws Exception {
        Account account = register("Senha");

        call(account, put("/me/password"), "{\"currentPassword\":\"errada\",\"newPassword\":\"" + NEW_PASSWORD + "\"}")
                .andExpect(status().isConflict());
        call(account, put("/me/password"), "{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"curta\"}")
                .andExpect(status().isBadRequest());
        call(account, put("/me/password"),
                "{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"" + NEW_PASSWORD + "\"}")
                .andExpect(status().isNoContent());

        entityManager.flush();
        login(account.email(), NEW_PASSWORD).andExpect(status().isOk());
        login(account.email(), PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void avatarSobeEDesceSoSeForImagemDeAte2MB() throws Exception {
        Account account = register("Foto");
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

        upload(account, new MockMultipartFile("file", "a.txt", "text/plain", png))
                .andExpect(status().isConflict());
        upload(account, new MockMultipartFile("file", "a.png", "image/png",
                new byte[2 * 1024 * 1024 + 1]))
                .andExpect(status().isConflict());
        upload(account, new MockMultipartFile("file", "a.png", "image/png", png))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}/avatar", account.id()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=86400")))
                .andExpect(content().bytes(png));
        mockMvc.perform(get("/users/{id}/avatar", UUID.randomUUID())).andExpect(status().isNoContent());
    }

    @Test
    void adminListaUsuariosEUsuarioComumRecebe403() throws Exception {
        Account admin = register("Admin");
        Account user = register("Comum");
        setRole(admin.id(), "ADMIN");

        call(admin, get("/admin/users"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + user.id() + "')].email").value(user.email()))
                .andExpect(jsonPath("$[?(@.id == '" + user.id() + "')].banned").value(false))
                .andExpect(jsonPath("$[?(@.id == '" + user.id() + "')].createdAt").exists());
        call(user, get("/admin/users"), null).andExpect(status().isForbidden());
        call(user, post("/admin/users/{id}/ban", admin.id()), null).andExpect(status().isForbidden());
    }

    @Test
    void adminBaneEDesbaneQuemEstaAbaixoMasNaoOutroAdminNemASiMesmo() throws Exception {
        Account admin = register("Admin");
        Account otherAdmin = register("Admin2");
        Account user = register("Comum");
        setRole(admin.id(), "ADMIN");
        setRole(otherAdmin.id(), "ADMIN");

        call(admin, post("/admin/users/{id}/ban", user.id()), null).andExpect(status().isNoContent());
        call(user, get("/me"), null).andExpect(status().isUnauthorized());
        call(admin, get("/admin/users"), null)
                .andExpect(jsonPath("$[?(@.id == '" + user.id() + "')].banned").value(true));
        call(admin, post("/admin/users/{id}/unban", user.id()), null).andExpect(status().isNoContent());
        call(user, get("/me"), null).andExpect(status().isOk());

        call(admin, post("/admin/users/{id}/ban", otherAdmin.id()), null).andExpect(status().isConflict());
        call(admin, post("/admin/users/{id}/ban", admin.id()), null).andExpect(status().isConflict());
        call(admin, post("/admin/users/{id}/ban", UUID.randomUUID()), null).andExpect(status().isNotFound());
    }

    @Test
    void soODonoPromoveERebaixa() throws Exception {
        Account owner = register("Dono");
        Account admin = register("Admin");
        Account user = register("Comum");
        setRole(owner.id(), "OWNER");
        setRole(admin.id(), "ADMIN");

        call(admin, post("/admin/users/{id}/promote", user.id()), null).andExpect(status().isConflict());
        call(owner, post("/admin/users/{id}/promote", user.id()), null).andExpect(status().isNoContent());
        call(user, get("/me"), null).andExpect(jsonPath("$.role").value("ADMIN"));
        call(owner, post("/admin/users/{id}/promote", user.id()), null).andExpect(status().isConflict());

        call(admin, post("/admin/users/{id}/demote", user.id()), null).andExpect(status().isConflict());
        call(owner, post("/admin/users/{id}/demote", user.id()), null).andExpect(status().isNoContent());
        call(user, get("/me"), null).andExpect(jsonPath("$.role").value("USER"));
        call(owner, post("/admin/users/{id}/demote", user.id()), null).andExpect(status().isConflict());

        call(owner, post("/admin/users/{id}/ban", admin.id()), null).andExpect(status().isNoContent());
    }

    @Test
    void adminExcluiQuemEstaAbaixo() throws Exception {
        Account admin = register("Admin");
        Account owner = register("Dono");
        Account user = register("Comum");
        setRole(admin.id(), "ADMIN");
        setRole(owner.id(), "OWNER");

        call(admin, delete("/admin/users/{id}", owner.id()), null).andExpect(status().isConflict());
        call(admin, delete("/admin/users/{id}", admin.id()), null).andExpect(status().isConflict());
        call(admin, delete("/admin/users/{id}", user.id()), null).andExpect(status().isNoContent());
        call(user, get("/me"), null).andExpect(status().isUnauthorized());
        login(user.email(), PASSWORD).andExpect(status().isUnauthorized());
    }
}
