package dudu.anime.util;

import dudu.anime.domain.Anime;

import java.time.LocalDate;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
                .animeName("Tensei Shitara Slime Datta Ken")
                .description("olá mundo")
                .creator("Ciro Colares")
                .releaseDate(LocalDate.ofEpochDay(1996- 2 -14))
                .build();
    }
    public static Anime createValidAnime() {
        return Anime.builder()
                .id(1)
                .animeName("Tensei Shitara Slime Datta Ken")
                .description("olá mundo")
                .creator("Ciro Colares")
                .releaseDate(LocalDate.now())
                .build();
    }
    public static Anime createValidUpdatedAnime() {
        return Anime.builder()
                .id(1)
                .animeName("Tensei Shitara Slime Datta Ken 2")
                .build();
    }
}
