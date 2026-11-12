package AfriFreelance.API.business.project;

import AfriFreelance.API.business.project.Project;
import AfriFreelance.API.business.user.Role;
import AfriFreelance.API.business.user.RoleRepository;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.enums.UserStatus;
import AfriFreelance.API.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectPublishingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ProjectRepository projectRepository;

    private String token;
    private User savedUser;

    @BeforeEach
    void setUpUser() {
        Role demandeur = roleRepository.findByCode("DEMANDEUR").orElseThrow();

        User user = User.builder()
                .username("publisher-" + System.nanoTime())
                .email("publisher-" + System.nanoTime() + "@test.sn")
                .fullName("Test Publisher")
                .password(passwordEncoder.encode("Password1!"))
                .status(UserStatus.ACTIF)
                .build();
        user.setRoles(new HashSet<>());
        user.getRoles().add(demandeur);
        user = userRepository.save(user);
        savedUser = user;

        token = jwtUtils.generateToken(user);
    }

    private String projectJson(String projectType, String budgetMin, String budgetMax, String deadline) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"title\":\"Mission test\",\"description\":\"Une description suffisamment longue\",\"projectType\":");
        sb.append(projectType == null ? "null" : "\"" + projectType + "\"");
        if (budgetMin != null) sb.append(",\"budgetMin\":").append(budgetMin);
        if (budgetMax != null) sb.append(",\"budgetMax\":").append(budgetMax);
        if (deadline != null) sb.append(",\"expectedStartDate\":\"").append(deadline).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private MvcResult createProject(String body) throws Exception {
        return mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
    }

    @Test
    void createProjectWithFixedBudgetIsSaved() throws Exception {
        MvcResult result = createProject(projectJson("fixed", "50000", "100000", null));
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(result.getResponse().getContentAsString()).contains("\"projectType\":\"FIXED_PRICE\"");

        assertThat(projectRepository.findByClientIdOrderByCreatedAtDesc(savedUser.getId())).hasSize(1);
    }

    @Test
    void createProjectWithMonthlyBudgetIsSaved() throws Exception {
        MvcResult result = createProject(projectJson("monthly", "50000", "100000", null));
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(result.getResponse().getContentAsString()).contains("\"projectType\":\"HOURLY\"");
    }

    @Test
    void pastDeadlineIsRejectedWith400() throws Exception {
        String past = LocalDate.now().minusDays(1).toString();
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson("fixed", "50000", "100000", past)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void futureDeadlineIsAccepted() throws Exception {
        String future = LocalDate.now().plusDays(30).toString();
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson("fixed", "50000", "100000", future)))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidBudgetIsRejectedWith400() throws Exception {
        MvcResult result = createProject(projectJson("fixed", "100000", "50000", null));
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(result.getResponse().getContentAsString()).contains("INVALID_BUDGET");
    }

    @Test
    void unknownProjectTypeReturns400() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson("premium", "50000", "100000", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unauthenticatedUserCannotCreateProject() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson("fixed", "50000", "100000", null)))
                .andExpect(status().isUnauthorized());
    }
}
