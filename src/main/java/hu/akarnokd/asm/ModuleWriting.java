package hu.akarnokd.asm;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;

import org.objectweb.asm.*;

import com.google.common.io.Files;

public class ModuleWriting {
    public static void main(String[] args) throws Exception {
        ClassWriter cw = new ClassWriter(0);

        cw.visit(V9, ACC_MODULE, "module-info", null, null, null);

        ModuleVisitor mw = cw.visitModule("io.reactivex.rxjava3", 0, null);
        mw.visitRequire("java.base", ACC_MANDATED, null);
        mw.visitRequire("org.reactivestreams", ACC_TRANSITIVE, null);

        mw.visitExport("io.reactivex.rxjava3", 0, new String[] {
                "io/reactivex/rxjava3/annotations",
                "io/reactivex/rxjava3/core",
                "io/reactivex/rxjava3/disposables",
                "io/reactivex/rxjava3/exceptions",
                "io/reactivex/rxjava3/flowables",
                "io/reactivex/rxjava3/functions",
                "io/reactivex/rxjava3/observables",
                "io/reactivex/rxjava3/observers",
                "io/reactivex/rxjava3/operators",
                "io/reactivex/rxjava3/parallel",
                "io/reactivex/rxjava3/plugins",
                "io/reactivex/rxjava3/processors",
                "io/reactivex/rxjava3/schedulers",
                "io/reactivex/rxjava3/subjects",
                "io/reactivex/rxjava3/subscribers",

        });
        mw.visitEnd();
        cw.visitEnd();

        Files.write(cw.toByteArray(), new File("c:/work/module-info.class"));
    }
}
