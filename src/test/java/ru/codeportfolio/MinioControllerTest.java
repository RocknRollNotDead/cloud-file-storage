package ru.codeportfolio;

import io.minio.MinioClient;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.model.Role;
import ru.codeportfolio.model.User;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Testcontainers
class MinioControllerTest extends IntegrationTestBase {

    public static final String API_DIRECTORY = "/api/directory";
    public static final String API_RESOURCE_SEARCH = "/api/resource/search";
    public static final String API_RESOURCE = "/api/resource";
    public static final String API_RESOURCE_MOVE = "/api/resource/move";
    static MinioClient minioClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    MockMvc mockMvc;


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

    @BeforeAll
    static void setUp() {
        minioClient = MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials(minio.getUserName(), minio.getPassword())
                .build();
    }

    // скачать

    @Test
    void shouldDownload() throws Exception {
        makePostRequestWithPath(API_DIRECTORY, "docs0/")
                .andExpect(status().isCreated());

        MockMultipartFile file = getMockMultipartFile();

        uploadTestFile(file, "docs0/");

        makeGetRequestWithPath("/api/resource/download", "docs0/test.txt")
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"));
    } // для теста скачивания папки слишком много гемороя.

    // скачать ресурс не найден
    @Test
    void shouldNotDownload_() throws Exception {


        makePostRequestWithPath(API_DIRECTORY, "docs00/")
                .andExpect(status().isCreated());

        makeGetRequestWithPath("/api/resource/download", "docs00/test.txt")
                .andExpect(status().isNotFound());
    }


    // инфо о ресурсе папка
    // инфо о ресурсе файл
    // инфо о ресурсе не найдено
    @Test
    void getInfoFolder() throws Exception {
        makePostRequestWithPath(API_DIRECTORY, "docs11/")
                .andExpect(status().isCreated());

        makeGetRequestWithPath(API_RESOURCE, "docs11/")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("docs11"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"))
        ;
    }

    @Test
    void getInfoFile_() throws Exception {
        makePostRequestWithPath(API_DIRECTORY, "docs12/")
                .andExpect(status().isCreated());

        MockMultipartFile file = getMockMultipartFile();

        uploadTestFile(file, "docs12/");


        makeGetRequestWithPath(API_RESOURCE, "docs12/test.txt")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test.txt"))
                .andExpect(jsonPath("$.path").value("/docs12/"))
                .andExpect(jsonPath("$.type").value("FILE"))
        ;
    }

    @Test
    void notGetInfoFolder() throws Exception {
        makePostRequestWithPath(API_DIRECTORY, "docs13/")
                .andExpect(status().isCreated());


        makeGetRequestWithPath(API_RESOURCE, "docs13/1/")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
        ;
    }


    @Test
    void notGetInfoFile() throws Exception {
        makePostRequestWithPath(API_DIRECTORY, "docs14/")
                .andExpect(status().isCreated());


        makeGetRequestWithPath(API_RESOURCE, "docs14/test.txt")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
        ;
    }


    // DELETE /resource
    @Test
    void deleteResource() throws Exception {
        makePostRequestWithPath(API_DIRECTORY, "docs15/")
                .andExpect(status().isCreated());

        MockMultipartFile file = getMockMultipartFile();

        uploadTestFile(file, "docs15/");

        mockMvc.perform(delete("/api/resource")
                        .with(user("test-user").roles("USER"))
                        .param("path", "docs15/test.txt"))
                .andExpect(status().isNoContent())
        ;


    }


    @Test
    void notDeleteResourceNotFound() throws Exception {
        makePostRequestWithPath(API_DIRECTORY, "docs16/")
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/resource")
                        .with(user("test-user").roles("USER"))
                        .param("path", "docs16/test.txt"))
                .andExpect(status().isNotFound());
    }


    // переименовать

    @Test
    void shouldBeRename() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs1/")

                .andExpect(status().isCreated());


        makePostRequestWithPath(API_DIRECTORY, "docs1/1/")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/docs1/"))
                .andExpect(jsonPath("$.name").value("1"));

        mockMvc.perform(makePostRequestWithUser(API_RESOURCE_MOVE)
                        .param("from", "docs1/1/")
                        .param("to", "docs1/2/")
                )

                .andExpect(status().isOk());

    }

    @Test
    void shouldBeRenameWithFiles() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs3/")
                .andExpect(status().isCreated());


        makePostRequestWithPath(API_DIRECTORY, "docs3/1/")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/docs3/"))
                .andExpect(jsonPath("$.name").value("1"));


        MockMultipartFile file = getMockMultipartFile();

        uploadTestFile(file, "docs3/1/");


        mockMvc.perform(makePostRequestWithUser(API_RESOURCE_MOVE)
                        .param("from", "docs3/1/")
                        .param("to", "docs3/2/")
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("2"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }

    @Test
    void shouldBeMoveWithFiles() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs4/")
                .andExpect(status().isCreated());


        makePostRequestWithPath(API_DIRECTORY, "docs4/1/")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/docs4/"))
                .andExpect(jsonPath("$.name").value("1"));

        makePostRequestWithPath(API_DIRECTORY, "docs4/2/")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/docs4/"))
                .andExpect(jsonPath("$.name").value("2"));


        MockMultipartFile file = getMockMultipartFile();

        uploadTestFile(file, "docs4/1/");


        mockMvc.perform(makePostRequestWithUser(API_RESOURCE_MOVE)
                        .param("from", "docs4/1/")
                        .param("to", "docs4/2/1/")
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("1"))
                .andExpect(jsonPath("$.path").value("/docs4/2/"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }


    // переименовать ресурс не найден

    @Test
    void shouldBeNotRename() throws Exception {
        mockMvc.perform(makePostRequestWithUser(API_RESOURCE_MOVE)
                        .param("from", "docs5/1/")
                        .param("to", "docs5/2/")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }


    // переименовать в уже существующую

    @Test
    void shouldBeNotRenameAlreadyExist() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs6/")

                .andExpect(status().isCreated());


        makePostRequestWithPath(API_DIRECTORY, "docs6/1/")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/docs6/"))
                .andExpect(jsonPath("$.name").value("1"));

        mockMvc.perform(makePostRequestWithUser("/api/resource/move")
                        .param("from", "docs6/1/")
                        .param("to", "docs6/")
                )

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    // поиск удачный

    @Test
    void shouldBeSearch() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs7/")

                .andExpect(status().isCreated());

        makePostRequestWithPath(API_DIRECTORY, "docs7/1/")

                .andExpect(status().isCreated());

        makePostRequestWithPath(API_DIRECTORY, "docs7/2/")

                .andExpect(status().isCreated());

        makeGetRequestWithQuery(API_RESOURCE_SEARCH, "docs7/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("1"))
                .andExpect(jsonPath("$[0].path").value("/docs7/"))
                .andExpect(jsonPath("$[0].type").value("DIRECTORY"));

    }

    // поиск неудачный

    @Test
    void shouldBeNotSearch() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs7_1/")

                .andExpect(status().isCreated());

        makePostRequestWithPath(API_DIRECTORY, "docs7_1/1/")

                .andExpect(status().isCreated());

        makePostRequestWithPath(API_DIRECTORY, "docs7_1/2/")

                .andExpect(status().isCreated());

        makeGetRequestWithQuery(API_RESOURCE_SEARCH, "3")
                .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void shouldUploadFile() throws Exception {
        MockMultipartFile file = getMockMultipartFile();

        uploadTestFile(file, "");
    }


    @Test
    void uploadExistFile() throws Exception {
        byte[] resource = "test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("object", "test-1.txt", "text/plain", resource);

        makeMultipartRequest(file, "")

                .andExpect(status().isCreated());

        makeMultipartRequest(file, "")
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUploadFileWithNotExistFolder() throws Exception {
        byte[] original = "test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("object", "test folder/.txt", "text/plain", original);


        makeMultipartRequest(file, "")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value(".txt"));
    }

    // папки

    @Test
    void createFolder() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs/")

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/"))
                .andExpect(jsonPath("$.name").value("docs"));
    }

    @Test
    void createFolderWithSymbol() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "do/cs/")

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    void notCreateFolderNotFound() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs2/docs3/")

                .andExpect(status().isNotFound());
    }

    @Test
    void notCreateFolderAlreadyExist() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs21/")

                .andExpect(status().isCreated());

        makePostRequestWithPath(API_DIRECTORY, "docs21/")

                .andExpect(status().isConflict());

    }


    @Test
    void getFolder() throws Exception {

        makePostRequestWithPath(API_DIRECTORY, "docs9/")

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/"))
                .andExpect(jsonPath("$.name").value("docs9"));

        makePostRequestWithPath(API_DIRECTORY, "docs9/1/")

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/docs9/"))
                .andExpect(jsonPath("$.name").value("1"));

        makeGetRequestWithPath(API_DIRECTORY, "docs9/")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value("/docs9/"))
                .andExpect(jsonPath("$[0].name").value("1"));
    }

    @Test
    void notGetFolder() throws Exception {
        makeGetRequestWithPath(API_DIRECTORY, "docs91/")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }


    private @NonNull ResultActions makePostRequestWithPath(String request, String path) throws Exception {
        return mockMvc.perform(makePostRequestWithUser(request)
                .param("path", path)
        );
    }


    private void uploadTestFile(MockMultipartFile file, String path) throws Exception {
        makeMultipartRequest(file, path)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("test.txt"))
                .andExpect(jsonPath("$[0].type").value("FILE"));
    }

    private static @NonNull MockMultipartFile getMockMultipartFile() {
        byte[] original = "test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("object", "test.txt", "text/plain", original);
        return file;
    }

    private @NonNull ResultActions makeGetRequestWithPath(String request, String path) throws Exception {
        return mockMvc.perform(makeGetRequestWithUser(request)
                .param("path", path)
        );
    }

    private @NonNull ResultActions makeGetRequestWithQuery(String request, String query) throws Exception {
        return mockMvc.perform(makeGetRequestWithUser(request)
                .param("query", query)
        );
    }

    private @NonNull ResultActions makeMultipartRequest(MockMultipartFile file, String path) throws Exception {
        return mockMvc.perform(multipart(API_RESOURCE)
                .with(user("test-user").roles("USER"))
                .file(file)
                .param("path", path)
        );
    }


    private static @NonNull MockHttpServletRequestBuilder makePostRequestWithUser(String request) {
        return post(request)
                .with(user("test-user").roles("USER"));
    }

    private static @NonNull MockHttpServletRequestBuilder makeGetRequestWithUser(String request) {
        return get(request)
                .with(user("test-user").roles("USER"));
    }


}