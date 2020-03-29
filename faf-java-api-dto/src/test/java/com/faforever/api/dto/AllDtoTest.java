package com.faforever.api.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.PackageFilter;
import pl.pojo.tester.api.assertion.Assertions;
import pl.pojo.tester.internal.utils.ReflectionUtils;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AllDtoTest {

  @Test
  void allDto() {
    // TODO test getter and setter
    Assertions.assertPojoMethodsForAll(new DtoFilter()).quickly();
  }

  @Test
  void unequalityAbstractEntityTest() {
    // Reproduction of issue #11
    AbstractEntity player = Player.builder().id("equal-id").build();
    AbstractEntity avatar = Avatar.builder().id("equal-id").build();

    assertNotEquals(player, avatar);
  }

  private static class DtoFilter implements PackageFilter {

    @Override
    @SneakyThrows
    public Class<?>[] getClasses() {
      try {
        Class<?>[] classesFromPackage = ReflectionUtils.getClassesFromPackage(AbstractEntity.class.getPackage().getName());
        return Stream.of(classesFromPackage)
          .filter(aClass -> !aClass.isEnum() && !Modifier.isAbstract(aClass.getModifiers()))
          .toArray(Class[]::new);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
