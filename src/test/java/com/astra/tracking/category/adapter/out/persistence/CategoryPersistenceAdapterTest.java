package com.astra.tracking.category.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.TestcontainersConfiguration;
import com.astra.tracking.category.domain.Category;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, CategoryPersistenceAdapter.class})
class CategoryPersistenceAdapterTest {

    @Autowired
    private CategoryPersistenceAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    private UUID insertUser() {
        UUID id = UUID.randomUUID();
        entityManager.createNativeQuery(
                        "insert into users (id, name, email, password_hash, tag) values (?, ?, ?, 'x', '0001')")
                .setParameter(1, id)
                .setParameter(2, "user-" + id)
                .setParameter(3, id + "@astra.local")
                .executeUpdate();
        return id;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void criaCarregaListaEApaga() {
        UUID user = insertUser();
        UUID other = insertUser();
        Category estudo = adapter.create(new Category(user, "Estudo", "#fff"));
        adapter.create(new Category(user, "Trabalho", null));
        adapter.create(new Category(other, "Estudo", null));
        flushAndClear();

        assertThat(adapter.load(estudo.getId())).get().extracting(Category::getName, Category::getColor)
                .containsExactly("Estudo", "#fff");
        assertThat(adapter.listByUser(user)).extracting(Category::getName)
                .containsExactlyInAnyOrder("Estudo", "Trabalho");

        adapter.delete(adapter.load(estudo.getId()).orElseThrow());
        flushAndClear();

        assertThat(adapter.load(estudo.getId())).isEmpty();
        assertThat(adapter.listByUser(user)).extracting(Category::getName).containsExactly("Trabalho");
    }

    @Test
    void nomeOcupadoEPorUsuario() {
        UUID user = insertUser();
        adapter.create(new Category(user, "Estudo", null));
        flushAndClear();

        assertThat(adapter.isNameTaken(user, "Estudo")).isTrue();
        assertThat(adapter.isNameTaken(user, "Leitura")).isFalse();
        assertThat(adapter.isNameTaken(insertUser(), "Estudo")).isFalse();
    }
}
