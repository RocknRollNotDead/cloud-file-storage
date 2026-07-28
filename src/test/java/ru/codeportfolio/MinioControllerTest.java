package ru.codeportfolio;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.model.Role;
import ru.codeportfolio.model.User;
import ru.codeportfolio.service.FilesService;

import java.util.List;

//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
class MinioControllerTest extends IntegrationTestBase {

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z")
            .withUserName("minioadmin")
            .withPassword("minioadmin");

    static MinioClient minioClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private FilesService fileService;

    @BeforeEach
    void createTestUser() {
        if (userRepository.findUsersByLogin("test-user").isEmpty()) {
            User user = new User(
                    "test-user",
                    passwordEncoder.encode("password"),
                    Role.USER);
            userRepository.save(user);
        }
    }

    @BeforeEach
    void authenticate() {
        var auth = new UsernamePasswordAuthenticationToken(
                "test-user", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeAll
    static void setUp(){
        minioClient = MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials(minio.getUserName(), minio.getPassword())
                .build();
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }





    @Test
    void shouldReturnFileInfoAfterUpload(){
        byte[] original = "test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", original);

        var result = fileService.upload("", "test-user", List.of(file));

        ru.codeportfolio.dto.ResourceResponseDto dto = fileService.getInfo("test.txt", "test-user");

        assertThat(dto.name()).isEqualTo("test.txt");

        assertThat(result.getFirst().name()).isEqualTo("test.txt");
    }



    @Test
    void shouldReturnListCreatedFolders() {
        fileService.createFolder("docs/", "test-user");
        fileService.createFolder("docs/1/", "test-user");
        fileService.createFolder("docs/2/", "test-user");


        var items = fileService.getFolder("docs/", "test-user");

        assertThat(items).extracting(ResourceResponseDto::name).contains("1", "2");

    }

    @Test
    void shouldBeRename(){
        fileService.createFolder("docs/", "test-user");
        fileService.createFolder("docs/1/", "test-user");
        fileService.createFolder("docs/2/", "test-user");

        fileService.move("docs/1/", "docs/3/", "test-user");

        var items = fileService.getFolder("docs/", "test-user");

        assertThat(items).extracting(ResourceResponseDto::name).contains("3", "2");

    }
}