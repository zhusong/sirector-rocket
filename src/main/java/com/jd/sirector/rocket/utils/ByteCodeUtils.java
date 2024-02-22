package com.jd.sirector.rocket.utils;


import java.io.File;
import java.io.IOException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.CtField.Initializer;
import org.springframework.util.StringUtils;

public class ByteCodeUtils {
    public ByteCodeUtils() {
    }

    public static CtField buildField(CtClass cc, String name) throws CannotCompileException {
        return buildField(cc, name, "java.lang.Object");
    }

    public static CtField buildField(CtClass cc, String name, String type) throws CannotCompileException {
        CtField field = new CtField(ClassUtils.get(type), name, cc);
        field.setModifiers(2); //private
        cc.addField(field);
        return field;
    }

    public static CtField buildField(CtClass cc, String name, String type, Initializer initializer) throws CannotCompileException {
        CtField field = new CtField(ClassUtils.get(type), name, cc);
        field.setModifiers(2);
        cc.addField(field, initializer);
        return field;
    }

    public static void buildFieldGetSet(CtClass cc, String name) throws CannotCompileException {
        String getMethod = "get" + Utils.firstUpper(name);
        String setMethod = "set" + Utils.firstUpper(name);
        CtField field = buildField(cc, name);
        cc.addMethod(CtNewMethod.getter(getMethod, field));
        cc.addMethod(CtNewMethod.setter(setMethod, field));
    }

    public static void buildFieldGetSet(CtClass cc, String name, String type) throws CannotCompileException {
        String getMethod = "get" + Utils.firstUpper(name);
        String setMethod = "set" + Utils.firstUpper(name);
        CtField field = null;
        if(!"void".equals(type)){
            field = buildField(cc, name, type);
        }else{
            field = buildField(cc, name);
        }
        cc.addMethod(CtNewMethod.getter(getMethod, field));
        cc.addMethod(CtNewMethod.setter(setMethod, field));
    }

    public static void buildFieldGetSet(CtClass cc, String name, String type, Initializer initializer) throws CannotCompileException {
        String getMethod = "get" + Utils.firstUpper(name);
        String setMethod = "set" + Utils.firstUpper(name);
        CtField field = new CtField(ClassUtils.get(type), name, cc);
        field.setModifiers(2);
        cc.addField(field, initializer);
        cc.addMethod(CtNewMethod.getter(getMethod, field));
        cc.addMethod(CtNewMethod.setter(setMethod, field));
    }

    public static CtConstructor buildConstructor(CtClass cc, String[] parameterNames, String body, CtClass[] exceptions) throws NotFoundException, CannotCompileException {
        ClassPool cp = ClassUtils.getClassPool();
        CtClass[] ctClasses = new CtClass[parameterNames.length];

        for(int i = 0; i < parameterNames.length; ++i) {
            ctClasses[i] = cp.get(parameterNames[i]);
        }

        return CtNewConstructor.make(ctClasses, exceptions, body, cc);
    }

    public static void importPackage(String packageName) {
        ClassPool cp = ClassUtils.getClassPool();
        cp.importPackage(packageName);
    }

    public static void outputClass(CtClass cc, boolean debug, String path) throws CannotCompileException, IOException {
        if (debug) {
            String out = path;
            if (Utils.isBlank(path)) {
                out = System.getProperty("user.home") + File.separatorChar + "bytecode";
            }

            cc.writeFile(out);
            cc.defrost();
        }
    }
}