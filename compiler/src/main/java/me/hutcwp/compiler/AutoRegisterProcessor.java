package me.hutcwp.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import me.hutcwp.api.AutoRegister;
import me.hutcwp.api.BaseManager;
import me.hutcwp.api.Inject;
import me.hutcwp.api.RegisterTarget;

@AutoService(Processor.class)
public class AutoRegisterProcessor extends AbstractProcessor {

    private Logger logger;
    private Messager messager;
    private Elements elementUtils;

    // key为要被注入到的类名【Manager】，value是注入的类名集合
    private Map<String, Set<String>> registerMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnv.getMessager();
        logger = new Logger(messager);
        elementUtils = processingEnv.getElementUtils();
        logger.info("init...");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(AutoRegister.class.getCanonicalName());
        supportTypes.add(RegisterTarget.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        logger.info("process...");
        registerMap.clear();
        generateAutoRegister(roundEnvironment);
        return true;
    }

    private void generateAutoRegister(RoundEnvironment roundEnv) {
        logger.info("parseAutoRegister...");
        Set<? extends Element> autoRegisters = roundEnv.getElementsAnnotatedWith(AutoRegister.class);
        Set<? extends Element> registerTargets = roundEnv.getElementsAnnotatedWith(RegisterTarget.class);

        for (Element targetElement : registerTargets) {
            if (targetElement.getKind() == ElementKind.CLASS) {
                initMapTarget(targetElement);
            }
        }

        for (Element autoRegisterElement : autoRegisters) {
            if (autoRegisterElement.getKind() == ElementKind.CLASS) {
                try {
                    initMapAutoRegister(autoRegisterElement);
                } catch (Exception e) {
                    logger.error("autoRegisterElement error -> " + e.getCause());
                }
            }
        }
        buildAutoRegisterCode();
    }

    private void initMapAutoRegister(Element autoRegisterElement) throws Exception {
        TypeElement typeElement = (TypeElement) autoRegisterElement;
        logger.info("---->before");
        AutoRegister autoRegister = typeElement.getAnnotation(AutoRegister.class);

        try {
            Class clzName = autoRegister.targetClass();
        } catch (MirroredTypeException e) {
            TypeMirror typeMirror = e.getTypeMirror();
            logger.info("catch type mirror = " + typeMirror.toString());
            String targetName = typeMirror.toString();

            logger.info("initMapAutoRegister -> targetName = " + targetName);
            if (registerMap.containsKey(targetName)) {
                logger.info("initMapAutoRegister -> registerName = " + autoRegisterElement.asType());
                Set<String> set = registerMap.get(targetName);
                set.add(autoRegisterElement.asType().toString());
            }
        }
        logger.info("---->end");
    }

    private void initMapTarget(Element target) {
        String className = target.asType().toString();
        if (!registerMap.containsKey(className)) {
            logger.info("initMapTarget -> target = " + className);
            Set<String> registerTargetSet = new HashSet<>();
            registerMap.put(className, registerTargetSet);
        }
    }

    private void buildAutoRegisterCode() {
        for (Map.Entry<String, Set<String>> stringSetEntry : registerMap.entrySet()) {
            String managerName = stringSetEntry.getKey();
            Set<String> registerNames = stringSetEntry.getValue();
            System.out.println("Key = " + managerName + ", Value = " + registerNames);
            logger.info("Key = " + managerName + ", Value = " + registerNames);
            generateCode(managerName, registerNames);
        }
    }

    private void generateCode(String managerName, Set<String> registerNames) {
        String suffix = "$AutoInject";
        String packageName = getNameByMirroType(managerName)[0];
        String clsName = getNameByMirroType(managerName)[1] + suffix;

        logger.info("[generateCode]--------->" + clsName + "-------------<");
        TypeSpec.Builder syringeBuilder = TypeSpec.classBuilder(clsName)
                .addSuperinterface(Inject.class)
                .addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "obj");

        for (String autoRegister : registerNames) {
            ClassName register = ClassName.get(getNameByMirroType(autoRegister)[0],
                    getNameByMirroType(autoRegister)[1]);

            methodBuilder.addStatement(
                    "((" + BaseManager.class.getCanonicalName() + ")obj).autoRegister(new $T())", register);
        }
        syringeBuilder.addMethod(methodBuilder.build());

        try {
            JavaFile javaFile = JavaFile.builder(packageName, syringeBuilder.build())
                    .build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String[] getNameByMirroType(String fullName) {
        String packageName = fullName.substring(0, fullName.lastIndexOf("."));
        String className = fullName.substring(fullName.lastIndexOf(".") + 1);
        return new String[]{packageName, className};
    }
}
