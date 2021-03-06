package io.micronaut.testsuitehelper;

import static javax.lang.model.SourceVersion.RELEASE_8;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(RELEASE_8)
public class TestGeneratingAnnotationProcessor extends AbstractProcessor {

    private boolean executed = false;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (executed) {
            return false;
        }

        try {
            final String output = determineOutputPath();
            final File outputDir = new File(output);

            switch (outputDir.getName()) {
                case "main":
                case "classes":
                    break;
                case "test":
                case "test-classes":
                    final JavaFileObject issue = processingEnv
                        .getFiler()
                        .createSourceFile("io.micronaut.test.generated.Example");
                    try (final Writer w = issue.openWriter()) {
                        w.write("package io.micronaut.test.generated;\n\npublic interface Example {}");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown builder for output " + outputDir);
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        executed = true;
        return false;
    }

    private String determineOutputPath() throws IOException {
        // go write a file so as to figure out where we're running
        final FileObject resource = processingEnv
            .getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", "tmp" + System.currentTimeMillis(), (Element[]) null);
        try {
            return new File(resource.toUri()).getCanonicalFile().getParent();
        } finally {
            resource.delete();
        }
    }

}
