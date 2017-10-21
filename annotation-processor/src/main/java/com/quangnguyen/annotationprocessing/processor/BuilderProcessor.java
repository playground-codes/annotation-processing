package com.quangnguyen.annotationprocessing.processor;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.quangnguyen.annotationprocessing.processor.BuilderProperty")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    // Iterate through all annotation types.
    for (TypeElement annotation : annotations) {
      // Retrieve all elements(package, class, method, property) annotated
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

      // Split annotated methods into two collections: correctly annotated methods and other wrong annotated methods.
      // In this case, the correctly annotated methods is the POJO's setter which starts with "set" and has only one parameter.
      Map<Boolean, List<Element>> annotatedMethods = annotatedElements.stream().collect(
          Collectors.partitioningBy(element ->
              element.getSimpleName().toString().startsWith("set") &&
                  ((ExecutableType) element.asType()).getParameterTypes().size() == 1));

      List<Element> setters = annotatedMethods.get(true);
      List<Element> otherMethods = annotatedMethods.get(false);

      // Print an error for each erroneously annotated element during the source processing stage.
      otherMethods.forEach(element -> processingEnv.getMessager()
          .printMessage(Diagnostic.Kind.ERROR,
              "@BuilderProperty must be applied to a setXxx method " + "with a single argument",
              element));

      // If setters collection is empty, skip.
      if (setters.isEmpty()) {
        continue;
      }

      // Get the fully qualified class name from the enclosing element.
      String className =
          ((TypeElement) setters.get(0).getEnclosingElement()).getQualifiedName().toString();

      // Map between the names of the setters and the names of their argument types
      Map<String, String> setterMap = setters.stream().collect(Collectors.toMap(
          setter -> setter.getSimpleName().toString(),
          setter -> ((ExecutableType) setter.asType()).getParameterTypes().get(0).toString()
      ));

      // Generate source files using `Filer`
      try {
        writeBuilderFile(className, setterMap);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return true;
  }

  private void writeBuilderFile(
      String className, Map<String, String> setterMap)
      throws IOException {
    String packageName = null;
    int lastDot = className.lastIndexOf('.');
    if (lastDot > 0) {
      packageName = className.substring(0, lastDot);
    }

    String simpleClassName = className.substring(lastDot + 1);
    String builderClassName = className + "Builder";
    String builderSimpleClassName = builderClassName
        .substring(lastDot + 1);

    JavaFileObject builderFile = processingEnv.getFiler()
        .createSourceFile(builderClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

      if (packageName != null) {
        out.print("package ");
        out.print(packageName);
        out.println(";");
        out.println();
      }

      out.print("public class ");
      out.print(builderSimpleClassName);
      out.println(" {");
      out.println();

      out.print("    private ");
      out.print(simpleClassName);
      out.print(" object = new ");
      out.print(simpleClassName);
      out.println("();");
      out.println();

      out.print("    public ");
      out.print(simpleClassName);
      out.println(" build() {");
      out.println("        return object;");
      out.println("    }");
      out.println();

      setterMap.entrySet().forEach(setter -> {
        String methodName = setter.getKey();
        String argumentType = setter.getValue();

        out.print("    public ");
        out.print(builderSimpleClassName);
        out.print(" ");
        out.print(methodName);

        out.print("(");

        out.print(argumentType);
        out.println(" value) {");
        out.print("        object.");
        out.print(methodName);
        out.println("(value);");
        out.println("        return this;");
        out.println("    }");
        out.println();
      });

      out.println("}");
    }
  }
}
