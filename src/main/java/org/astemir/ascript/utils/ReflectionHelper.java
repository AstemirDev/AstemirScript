package org.astemir.ascript.utils;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {


    public static Object invokeStaticMethod(String className,String methodName,Object[] argsValues){
        return invokeStaticMethod(getMethod(className,methodName,objClasses(argsValues)),argsValues);
    }

    public static Object invokeStaticMethod(Method method,Object[] argsValues){
        try {
            return method.invoke(null,argsValues);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(Object object,String methodName,Object[] argsValues){
        try {
            return getMethod(object.getClass(),methodName,objClasses(argsValues)).invoke(object,argsValues);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    public static Object invokeMethod(Object object,Method method,Object[] argsValues){
        try {
            return method.invoke(object,argsValues);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(String className,String methodName, String[] paramsClassesNames){
        return getMethod(findClass(className),methodName,paramsClassesNames);
    }

    public static Method getMethod(Class className,String methodName, String[] params){
        try {
            return className.getDeclaredMethod(methodName,objClasses(params));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(String className,String methodName, Class[] params){
        return getMethod(findClass(className),methodName,params);
    }

    public static Method getMethod(Class className,String methodName, Class[] params){
        try {
            return className.getDeclaredMethod(methodName,params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object newInstance(String className,Object[] params){
        return newInstance(findClass(className),params);
    }

    public static Object newInstance(Class className,Object[] params){
        try {
            return className.getDeclaredConstructor(objClasses(params)).newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public static Class findClass(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class[] objClasses(Object[] objects){
        Class[] classes = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            Class className = object.getClass();
            if (className == Integer.class){
                className = Integer.TYPE;
            }else
            if (className == Double.class){
                className = Double.TYPE;
            }else
            if (className == Float.class){
                className = Float.TYPE;
            }
            classes[i] = className;
        }
        return classes;
    }

}
