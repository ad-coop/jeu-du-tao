package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.Game;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {

    private final ConcurrentHashMap<String, Game> store = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) {
        store.put(game.handle(), game);
    }

    @Override
    public Optional<Game> findByHandle(String handle) {
        return Optional.ofNullable(store.get(handle));
    }

    @Override
    public boolean existsByHandle(String handle) {
        return store.containsKey(handle);
    }
}
