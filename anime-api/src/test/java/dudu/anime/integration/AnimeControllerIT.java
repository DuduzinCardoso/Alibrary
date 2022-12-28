package dudu.anime.integration;

import dudu.anime.domain.Anime;
import dudu.anime.exception.CustomAttributes;
import dudu.anime.repository.AnimeRepository;
import dudu.anime.service.AnimeService;
import dudu.anime.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
@Import({AnimeService.class, CustomAttributes.class})
public class AnimeControllerIT {

    @MockBean
    private AnimeRepository animeRepositoryMock;

    @Autowired
    private WebTestClient testClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install(
                builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID")
        );
    }

    @BeforeEach
    public void setUp() {
        BDDMockito.when(animeRepositoryMock.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock.saveAll(List.of(
                        AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeRepositoryMock.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("Should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSucessful() {
        testClient
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findById_ReturnMonoAnime_WhenSucessful() {
        testClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns Mono error anime does not exist")
    public void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        testClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("save creates an anime when sucessful")
    public void save_CreatesAnime_WhenSucessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClient
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when sucessful")
    public void saveBatch_CreatesListOfAnime_WhenSucessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClient
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("saveBatch returns Mono Error when one of the objects in the list contains null or empty name")
    public void saveBatch_ReturnsMonoError_WhenContainsInvalidName(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withAnimeName("");

        BDDMockito.when(animeRepositoryMock
                .saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(anime, anime.withAnimeName("")));

        testClient
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes the anime when sucessful")
    public void delete_RemovesAnime_WhenSucessful() {
        testClient
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        testClient
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when sucessful")
    public void update_SaveUpdatedAnime_WhenSucessful() {
        testClient
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exist")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        testClient
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }
}
