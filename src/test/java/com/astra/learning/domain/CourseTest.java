package com.astra.learning.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseTest {

    private static final UUID USER = UUID.randomUUID();

    private static List<Integer> positions(CourseModule module) {
        return module.getLessons().stream().map(Lesson::getPosition).toList();
    }

    @Test
    void planejarCriaModulosNumeradosSemAulas() {
        Course course = Course.plan(USER, "Spring", "Udemy", 2);

        assertThat(course.getStatus()).isEqualTo(CourseStatus.PLANNED);
        assertThat(course.getModules()).extracting(CourseModule::getTitle).containsExactly("Módulo 1", "Módulo 2");
        assertThat(course.getModules()).extracting(CourseModule::getPosition).containsExactly(1, 2);
        assertThat(course.totalLessons()).isZero();
        assertThat(course.progress()).isZero();
        assertThat(course.belongsTo(USER)).isTrue();
        assertThat(course.belongsTo(UUID.randomUUID())).isFalse();
    }

    @Test
    void moduloNovoGanhaAulasNumeradasEFicaNaOrdem() {
        Course course = Course.plan(USER, "Spring", null, 2);

        CourseModule module = course.addModule("Intro", 0, 3);

        assertThat(module.getLessons()).extracting(Lesson::getTitle).containsExactly("Aula 1", "Aula 2", "Aula 3");
        assertThat(course.getModules().getFirst()).isSameAs(module);
        assertThat(course.totalLessons()).isEqualTo(3);
    }

    @Test
    void progressoContaAulasDeTodosOsModulos() {
        Course course = Course.plan(USER, "Spring", null, 0);
        CourseModule first = course.addModule("A", 1, 3);
        CourseModule second = course.addModule("B", 2, 1);

        course.markLessonCompleted(first.getId(), first.getLessons().getFirst().getId(), true);
        course.markLessonCompleted(second.getId(), second.getLessons().getFirst().getId(), true);

        assertThat(course.completedLessons()).isEqualTo(2);
        assertThat(course.progress()).isEqualTo(0.5);
        assertThat(first.isCompleted()).isFalse();
        assertThat(second.isCompleted()).isTrue();
    }

    @Test
    void moduloSemAulasSoFicaConcluidoSeForMarcado() {
        Course course = Course.plan(USER, "Spring", null, 1);
        UUID moduleId = course.getModules().getFirst().getId();

        assertThat(course.module(moduleId).isCompleted()).isFalse();
        assertThat(course.markModuleCompleted(moduleId, true).isCompleted()).isTrue();
    }

    @Test
    void novaAulaReabreModuloMarcado() {
        Course course = Course.plan(USER, "Spring", null, 1);
        UUID moduleId = course.getModules().getFirst().getId();
        course.markModuleCompleted(moduleId, true);

        Lesson lesson = course.addLesson(moduleId, "Nova", 1);

        assertThat(lesson.isCompleted()).isFalse();
        assertThat(course.module(moduleId).isMarkedCompleted()).isFalse();
        assertThat(course.module(moduleId).isCompleted()).isFalse();
    }

    @Test
    void aulasFicamOrdenadasPelaPosicao() {
        Course course = Course.plan(USER, "Spring", null, 1);
        UUID moduleId = course.getModules().getFirst().getId();

        course.addLesson(moduleId, "C", 3);
        course.addLesson(moduleId, "A", 1);
        course.addLesson(moduleId, "B", 2);

        assertThat(course.module(moduleId).getLessons()).extracting(Lesson::getTitle).containsExactly("A", "B", "C");
    }

    @Test
    void removerAulaRenumeraAsQueSobram() {
        Course course = Course.plan(USER, "Spring", null, 0);
        CourseModule module = course.addModule("A", 1, 4);

        course.removeLesson(module.getId(), module.getLessons().get(1).getId());

        assertThat(module.getLessons()).extracting(Lesson::getTitle).containsExactly("Aula 1", "Aula 3", "Aula 4");
        assertThat(positions(module)).containsExactly(1, 2, 3);
    }

    @Test
    void progressoAteAPosicaoMarcaEDesmarca() {
        Course course = Course.plan(USER, "Spring", null, 0);
        CourseModule module = course.addModule("A", 1, 4);

        course.completeLessonsUpTo(module.getId(), 2);
        assertThat(module.getLessons()).extracting(Lesson::isCompleted).containsExactly(true, true, false, false);

        course.completeLessonsUpTo(module.getId(), 0);
        assertThat(module.completedLessons()).isZero();
    }

    @Test
    void renomearAparaERecusaEmBranco() {
        Course course = Course.plan(USER, "Spring", null, 1);
        UUID moduleId = course.getModules().getFirst().getId();

        assertThat(course.renameModule(moduleId, "  Avançado ").getTitle()).isEqualTo("Avançado");
        assertThatThrownBy(() -> course.renameModule(moduleId, "   ")).isInstanceOf(BlankModuleTitleException.class);
        assertThat(course.module(moduleId).getTitle()).isEqualTo("Avançado");
    }

    @Test
    void moduloOuAulaQueNaoSaoDoCursoNaoExistem() {
        Course course = Course.plan(USER, "Spring", null, 0);
        CourseModule module = course.addModule("A", 1, 1);
        Course other = Course.plan(USER, "Docker", null, 0);
        CourseModule otherModule = other.addModule("B", 1, 1);

        assertThatThrownBy(() -> course.module(otherModule.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> course.markLessonCompleted(module.getId(),
                otherModule.getLessons().getFirst().getId(), true)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> course.removeLesson(module.getId(), UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
        assertThat(module.totalLessons()).isEqualTo(1);
    }

    @Test
    void listasDeForaNaoMudamOCurso() {
        Course course = Course.plan(USER, "Spring", null, 1);

        assertThatThrownBy(() -> course.getModules().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> course.getModules().getFirst().getLessons().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void metaPrecisaDeHorasPositivas() {
        assertThat(new Goal(USER, GoalType.DAILY, 1).targetHours()).isEqualTo(1);
        assertThatThrownBy(() -> new Goal(USER, GoalType.DAILY, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
