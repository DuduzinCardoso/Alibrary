package dudu.anime.repository;

import dudu.anime.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findByUsername(String username);
}
