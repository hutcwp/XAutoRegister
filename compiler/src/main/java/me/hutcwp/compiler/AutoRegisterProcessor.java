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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;

import me.hutcwp.api.AutoRegister;
import me.hutcwp.api.Inject;
import me.hutcwp.api.RegisterClass;
import me.hutcwp.api.RegisterTarget;

@AutoService(Processor.class)
public class AutoRegisterProcessor extends AbstractProcessor {

    private Logger logger;
    private Messager messager;
    private Elements elementUtils;

    private boolean isGenenrate = false;

    private Map<String, RegisterUnit> registerMap = new HashMap<>();

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
        if (isGenenrate) {
            return;
        }

        isGenenrate = true;
        initMapTarget(roundEnv);
        initMapAutoRegister(roundEnv);
        buildAutoRegisterCode();
    }

    private void initMapTarget(RoundEnvironment roundEnv) {
        Set<? extends Element> registerTargets = roundEnv.getElementsAnnotatedWith(RegisterTarget.class);
        for (Element targetElement : registerTargets) {
            if (targetElement.getKind() != ElementKind.CLASS) {
                return;
            }

            if (!registerMap.containsKey(targetElement.asType().toString())) {
                logger.info("initMapTarget : targetElement = " + targetElement.asType().toString());
                RegisterUnit unit = new RegisterUnit();
                unit.setManagerElement(targetElement);
                registerMap.put(targetElement.asType().toString(), unit);
            }
        }
    }

    private void initMapAutoRegister(RoundEnvironment roundEnv) {
        Set<? extends Element> autoRegisters = roundEnv.getElementsAnnotatedWith(AutoRegister.class);
        for (Element autoRegisterElement : autoRegisters) {
            if (autoRegisterElement.getKind() != ElementKind.CLASS) {
                return;
            }

            TypeElement typeElement = (TypeElement) autoRegisterElement;
            logger.info("---->auto init map begin<-----");
            AutoRegister autoRegister = typeElement.getAnnotation(AutoRegister.class);
            try {
                autoRegister.targetClass();
            } catch (MirroredTypeException e) {
                String targetName = e.getTypeMirror().toString();
                logger.info("catch type mirror = " + targetName);
                RegisterUnit unit = getRegisterUnit(targetName);
                RegisterClass registerClass = getRegisterClassAnnotation(autoRegisterElement);
                logger.info("register class : " + autoRegisterElement);
                if (registerClass != null) {
                    if (unit.getParent() != null) {
                        logger.error(
                                new Exception("check if define more than one @AutoRegister to the same targetClass " +
                                        "->" + targetName));
                        return;
                    }
                    logger.info("set parent " + unit.getParent());
                    unit.setParent(autoRegisterElement);
                } else {
                    try {
                        logger.info("add child ,current parent is " + unit.getParent());
                        unit.addChild(autoRegisterElement);
                    } catch (Exception e2) {
                        logger.error("----add child error ---->" + e2);
                    }
                }
            }
            logger.info("---->auto init map finish<-----");
        }
    }

    private RegisterUnit getRegisterUnit(String targetName) {
        RegisterUnit unit;
        if (registerMap.containsKey(targetName)) {
            logger.info("initMapAutoRegister : containsKey = " + targetName);
            unit = registerMap.get(targetName);
        } else {
            logger.info("initMapAutoRegister : don not containsKey = " + targetName);
            unit = new RegisterUnit();
        }
        return unit;
    }

    private RegisterClass getRegisterClassAnnotation(Element element) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : mirrors) {
            RegisterClass registerClass = mirror.getAnnotationType().asElement().getAnnotation(RegisterClass.class);
            if (registerClass != null) {
                // logger.info("find out element:" + element);
                return registerClass;
            }
        }
        return null;
    }

    private void buildAutoRegisterCode() {
        logger.info("=================start build register code========================");
        for (Map.Entry<String, RegisterUnit> stringSetEntry : registerMap.entrySet()) {
            String managerName = stringSetEntry.getKey();
            RegisterUnit registerUnit = stringSetEntry.getValue();
            logger.info("ManagerName = " + managerName + ", RegisterUnit = " + registerUnit);
            generateCode(managerName, registerUnit);
        }
        logger.info("=================build register code end========================");
    }

    private void generateCode(String managerName, RegisterUnit registerUnit) {
        String suffix = "$AutoInject";
        ClassName manage = ClassName.bestGuess(managerName);
        String packageName = manage.packageName();
        String clsName = manage.simpleName() + suffix;

        logger.info("[generateCode]--------->" + clsName);
        TypeSpec.Builder syringeBuilder = TypeSpec.classBuilder(clsName)
                .addSuperinterface(Inject.class)
                .addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "obj");

        ClassName manager = ClassName.bestGuess(managerName);

        for (Element element : registerUnit.getChildren()) {
            String autoRegisterName = element.asType().toString();
            ClassName register = ClassName.bestGuess(autoRegisterName);

            methodBuilder.addStatement(
                    "(($T)obj).autoRegister(new $T())", manager, register);
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
}
