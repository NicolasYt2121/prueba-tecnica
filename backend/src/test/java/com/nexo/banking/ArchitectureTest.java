package com.nexo.banking;

import java.nio.file.*;
import java.util.*;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ArchitectureTest {
    @Test void coreCompilesWithOnlyTheJdk() throws Exception {
        // Keep compiler output under Maven's build directory (also works in restricted environments).
        Path output=Files.createDirectories(Path.of("target","architecture-core-"+UUID.randomUUID()));
        Path base=Path.of("src/main/java/com/nexo/banking");
        List<String> args=new ArrayList<>(List.of("-proc:none","-classpath",output.toString(),"-d",output.toString()));
        for(String part:List.of("domain","application","service")) {
            try(var paths=Files.walk(base.resolve(part))) {
                paths.filter(p->p.toString().endsWith(".java")).forEach(p->args.add(p.toString()));
            }
        }
        assertThat(ToolProvider.getSystemJavaCompiler().run(null,null,null,args.toArray(String[]::new)))
            .as("El núcleo debe compilar sin Spring, JPA, HTTP ni adaptadores").isZero();
    }

    @Test void controllersDependOnInputPorts() throws Exception {
        for(String kind:List.of("Customer","Account","Transaction")) {
            Class<?> controller=Class.forName("com.nexo.banking.controller."+kind+"Controller");
            var constructors=controller.getConstructors();
            assertThat(constructors).hasSize(1);
            assertThat(constructors[0].getParameterTypes()).hasSize(1);
            Class<?> dependency=constructors[0].getParameterTypes()[0];
            assertThat(dependency.isInterface()).isTrue();
            assertThat(dependency.getPackageName()).isEqualTo("com.nexo.banking.application.port.in");
        }
    }
}
