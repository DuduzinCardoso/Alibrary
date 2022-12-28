package dudu.anime.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Table("postgres.public.anime")
public class Anime {
    @Id
    private Integer id;

    @NotNull
    @NotEmpty
    private String animeName;
    @NotNull
    @NotEmpty
    private String description;
    @NotNull
    @NotEmpty
    private String creator;

    private LocalDate releaseDate;
}
