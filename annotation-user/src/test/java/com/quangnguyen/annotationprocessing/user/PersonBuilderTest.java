package com.quangnguyen.annotationprocessing.user;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PersonBuilderTest {
  @Test
  public void personBuilder_ShouldBeGenerated() {
    Person person = new PersonBuilder().setName("Jack").setAge(30).build();

    assertEquals(person.getAge(), 30);
    assertEquals(person.getName(), "Jack");
  }
}
