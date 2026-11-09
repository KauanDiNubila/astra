package com.astra.tracking.category.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.category.application.port.in.CategoryResponse;
import com.astra.tracking.category.application.port.in.CreateCategoryCommand;
import com.astra.tracking.category.application.port.out.CheckCategoryInUsePort;
import com.astra.tracking.category.application.port.out.CheckCategoryNameTakenPort;
import com.astra.tracking.category.application.port.out.CreateCategoryPort;
import com.astra.tracking.category.application.port.out.DeleteCategoryPort;
import com.astra.tracking.category.application.port.out.ListCategoriesPort;
import com.astra.tracking.category.application.port.out.LoadCategoryPort;
import com.astra.tracking.category.domain.Category;
import com.astra.tracking.category.domain.CategoryInUseException;
import com.astra.tracking.category.domain.CategoryNameTakenException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CategoryServicesTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID OTHER_USER = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    private final CheckCategoryNameTakenPort checkNameTakenPort = mock(CheckCategoryNameTakenPort.class);
    private final CreateCategoryPort createPort = mock(CreateCategoryPort.class);
    private final LoadCategoryPort loadPort = mock(LoadCategoryPort.class);
    private final ListCategoriesPort listPort = mock(ListCategoriesPort.class);
    private final CheckCategoryInUsePort inUsePort = mock(CheckCategoryInUsePort.class);
    private final DeleteCategoryPort deletePort = mock(DeleteCategoryPort.class);

    private final CreateCategoryService createService = new CreateCategoryService(checkNameTakenPort, createPort);
    private final DeleteCategoryService deleteService = new DeleteCategoryService(loadPort, inUsePort, deletePort);
    private final CreateDefaultCategoriesService defaultsService = new CreateDefaultCategoriesService(createPort);
    private final CategoryQueryService queryService = new CategoryQueryService(listPort, loadPort);

    private Category stored(UUID owner) {
        Category category = new Category(owner, "Estudo", "#fff");
        when(loadPort.load(CATEGORY_ID)).thenReturn(Optional.of(category));
        return category;
    }

    @Test
    void criaCategoriaComNomeLivre() {
        when(createPort.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = createService.create(new CreateCategoryCommand(USER, "Leitura", "#000"));

        assertThat(response.name()).isEqualTo("Leitura");
        assertThat(response.color()).isEqualTo("#000");
    }

    @Test
    void naoCriaComNomeRepetido() {
        when(checkNameTakenPort.isNameTaken(USER, "Leitura")).thenReturn(true);

        assertThatThrownBy(() -> createService.create(new CreateCategoryCommand(USER, "Leitura", null)))
                .isInstanceOf(CategoryNameTakenException.class);
        verify(createPort, never()).create(any());
    }

    @Test
    void apagaCategoriaDoUsuarioSemSessao() {
        Category category = stored(USER);

        deleteService.delete(USER, CATEGORY_ID);

        verify(deletePort).delete(category);
    }

    @Test
    void naoApagaCategoriaDeOutroUsuarioNemInexistente() {
        stored(OTHER_USER);
        assertThatThrownBy(() -> deleteService.delete(USER, CATEGORY_ID)).isInstanceOf(NotFoundException.class);

        when(loadPort.load(CATEGORY_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> deleteService.delete(USER, CATEGORY_ID)).isInstanceOf(NotFoundException.class);

        verify(deletePort, never()).delete(any());
    }

    @Test
    void naoApagaCategoriaEmUso() {
        stored(USER);
        when(inUsePort.isInUse(CATEGORY_ID)).thenReturn(true);

        assertThatThrownBy(() -> deleteService.delete(USER, CATEGORY_ID)).isInstanceOf(CategoryInUseException.class);
        verify(deletePort, never()).delete(any());
    }

    @Test
    void criaTrabalhoEEstudoParaUsuarioNovo() {
        ArgumentCaptor<Category> created = ArgumentCaptor.forClass(Category.class);

        defaultsService.createDefaults(USER);

        verify(createPort, times(2)).create(created.capture());
        assertThat(created.getAllValues()).extracting(Category::getName).containsExactly("Trabalho", "Estudo");
        assertThat(created.getAllValues()).allMatch(category -> category.belongsTo(USER));
    }

    @Test
    void listaEConfereDono() {
        when(listPort.listByUser(USER)).thenReturn(List.of(new Category(USER, "Estudo", null)));
        stored(USER);

        assertThat(queryService.list(USER)).extracting(CategoryResponse::name).containsExactly("Estudo");
        assertThat(queryService.isOwner(CATEGORY_ID, USER)).isTrue();
        assertThat(queryService.isOwner(CATEGORY_ID, OTHER_USER)).isFalse();
        assertThat(queryService.isOwner(UUID.randomUUID(), USER)).isFalse();
    }
}
