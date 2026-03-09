package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.Player;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPlayerRepository implements PlayerRepository {

    private final ConcurrentHashMap<String, Player> store = new ConcurrentHashMap<>();

    @Override
    public void save(Player player) {
        store.put(player.id(), player);
    }

    @Override
    public Optional<Player> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Player> findByGameHandle(String gameHandle) {
        return store.values().stream()
                .filter(p -> p.gameHandle().equals(gameHandle))
                .toList();
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
