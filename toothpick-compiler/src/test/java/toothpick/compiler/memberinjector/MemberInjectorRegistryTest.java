package toothpick.compiler.memberinjector;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.JavaFileObject;
import org.junit.Test;
import toothpick.compiler.registry.generators.RegistryGeneratorTestUtilities;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class MemberInjectorRegistryTest {
  @Test
  public void testASimpleRegistry() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestASimpleRegistry", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestASimpleRegistry {", //
        "  @Inject String s; ", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import java.lang.String;", //
        "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
        "", //
        "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
        "  public MemberInjectorRegistry() {", //
        "  }", //
        "", //
        "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
        "    String className = clazz.getName().replace('$','.');", //
        "    int bucket = (className.hashCode() & 0);", //
        "    switch(bucket) {", //
        "      case (0):", //
        "      return getMemberInjectorBucket0(clazz, className);", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> MemberInjector<T> getMemberInjectorBucket0(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestASimpleRegistry\"):", //
        "      return (MemberInjector<T>) new test.TestASimpleRegistry$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testARegistry_withDependencies() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistry", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistry {", //
        "  @Inject String s; ", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import java.lang.String;", //
        "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
        "", //
        "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
        "  public MemberInjectorRegistry() {", //
        "    addChildRegistry(new toothpick.MemberInjectorRegistry());", //
        "    addChildRegistry(new toothpick.MemberInjectorRegistry());", //
        "  }", //
        "", //
        "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
        "    String className = clazz.getName().replace('$','.');", //
        "    int bucket = (className.hashCode() & 0);", //
        "    switch(bucket) {", //
        "      case (0):", //
        "      return getMemberInjectorBucket0(clazz, className);", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> MemberInjector<T> getMemberInjectorBucket0(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistry\"):", //
        "      return (MemberInjector<T>) new test.TestARegistry$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Arrays.asList("toothpick", "toothpick")))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testARegistry_withNoFactories() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistryWithNoFactories", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistryWithNoFactories {", //
        "  @Inject String s; ", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
        "", //
        "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
        "  public MemberInjectorRegistry() {", //
        "  }", //
        "", //
        "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
        "    String className = clazz.getName().replace('$','.');", //
        "    int bucket = (className.hashCode() & -1);", //
        "    switch(bucket) {", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST, "java.*,android.*,test.*"))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testARegistry_withMoreThanOneBucket() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistryWithMoreThanOneBucket", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistryWithMoreThanOneBucket {", //
        "  @Inject String s;", //
        "  public static class InnerClass1 {", //
        "    @Inject String s;", //
        "  }", //
        "  public static class InnerClass2 {", //
        "    @Inject String s;", //
        "  }", //
        "  public static class InnerClass3 {", //
        "    @Inject String s;", //
        "  }", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import java.lang.String;", //
        "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
        "", //
        "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
        "  public MemberInjectorRegistry() {", //
        "  }", //
        "", //
        "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
        "    String className = clazz.getName().replace('$','.');", //
        "    int bucket = (className.hashCode() & 3);", //
        "    switch(bucket) {", //
        "      case (0):", //
        "      return getMemberInjectorBucket0(clazz, className);", //
        "      case (1):", //
        "      return getMemberInjectorBucket1(clazz, className);", //
        "      case (2):", //
        "      return getMemberInjectorBucket2(clazz, className);", //
        "      case (3):", //
        "      return getMemberInjectorBucket3(clazz, className);", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> MemberInjector<T> getMemberInjectorBucket0(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistryWithMoreThanOneBucket\"):", //
        "      return (MemberInjector<T>) new test.TestARegistryWithMoreThanOneBucket$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> MemberInjector<T> getMemberInjectorBucket1(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistryWithMoreThanOneBucket.InnerClass1\"):", //
        "      return (MemberInjector<T>) new test.TestARegistryWithMoreThanOneBucket$InnerClass1$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> MemberInjector<T> getMemberInjectorBucket2(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistryWithMoreThanOneBucket.InnerClass2\"):", //
        "      return (MemberInjector<T>) new test.TestARegistryWithMoreThanOneBucket$InnerClass2$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> MemberInjector<T> getMemberInjectorBucket3(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistryWithMoreThanOneBucket.InnerClass3\"):", //
        "      return (MemberInjector<T>) new test.TestARegistryWithMoreThanOneBucket$InnerClass3$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    RegistryGeneratorTestUtilities.setInjectionTarjetsPerGetterMethod(1);

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
