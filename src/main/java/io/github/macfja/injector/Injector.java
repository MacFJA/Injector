package io.github.macfja.injector;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Injector class.
 * The dependency injector entry point.
 *
 * @author MacFJA
 */
public class Injector implements Cloneable {
    /**
     * Mapping of class that have a particular injection
     */
    private final Map<Class, InjectionUnit> mapping = new ConcurrentHashMap<>();
    /**
     * List of package name that can be injected
     */
    private Set<String> workingPackages = new HashSet<>();
    /**
     * Should the injector inject into properties
     */
    private Boolean injectProperties = true;
    /**
     * Should the injector inject class with setters
     */
    private Boolean injectSetters = true;

    /**
     * Create an injector
     *
     * @param basePackage The base package that can be injected
     */
    public Injector(String basePackage) {
        addWorkingPackage(basePackage);
    }

    /**
     * Create an injector with a list of package
     *
     * @param packages The list of packages that can be injected
     */
    public Injector(Set<String> packages) {
        workingPackages = packages;
    }

    /**
     * Add an injection rule of a class
     *
     * @param forClass  The class to inject
     * @param instantiationType The Type of instantiation
     */
    public void addMapping(Class forClass, InjectionUnit.Instantiation instantiationType) {
        mapping.put(forClass, new InjectionUnit(forClass, instantiationType));
    }

    /**
     * Add an injection rule of a class
     *
     * @param forClass  The class to inject
     * @param injection The injection rule
     */
    public void addMapping(Class forClass, InjectionUnit injection) {
        mapping.put(forClass, injection);
    }

    /**
     * Add a singleton rule
     *
     * @param singleton The singleton to use
     */
    public void addMapping(Object singleton) {
        mapping.put(singleton.getClass(), new InjectionUnit(singleton));
    }

    /**
     * Add a package in the list of packages that can be injected
     *
     * @param packageName Name of the package to add
     */
    public void addWorkingPackage(String packageName) {
        workingPackages.add(packageName);
    }

    /**
     * Check if a class can be injected
     *
     * @param aClass The class to check
     * @return {@code true} is the class injectable
     */
    public boolean isInjectable(Class aClass) {
        if (mapping.containsKey(aClass)) {
            return true;
        }

        /*
        Classes name for primitive type
        {@link http://stackoverflow.com/a/12505922}

        [Z = boolean
        [B = byte
        [S = short
        [I = int
        [J = long
        [F = float
        [D = double
        [C = char
        [L = any non-primitives(Object)
         */
        if (aClass.isPrimitive() || Arrays.asList("[Z", "[B", "[S", "[I", "[J", "[F", "[D", "[C").contains(aClass.getName())) {
            return false;
        }

        if (aClass.getPackage() == null) {
            return false;
        }

        Boolean inWorkingPackages = false;
        for (String packageName : workingPackages) {
            if (aClass.getPackage().getName().startsWith(packageName)) {
                inWorkingPackages = true;
                break;
            }

        }
        if (!inWorkingPackages) {
            return false;
        }
        return InjectionUnit.isInstantiable(aClass, this);
    }

    /**
     * Get an instance of the requested class.
     * Silently fail.
     *
     * @param aClass The class
     * @return an instance of the class
     */
    public <T> T get(Class<? extends T> aClass) {
        try {
            if (mapping.containsKey(aClass)) {
                return (T) mapping.get(aClass).get(this);
            } else {
                return (T) new InjectionUnit(aClass, InjectionUnit.Instantiation.NewInstance).get(this);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LoggerFactory.getLogger(this.getClass()).error("Unable to get an instance of " + aClass.getName(), e);
        }
        return null;
    }

    public <T> T get(Class<? extends T> aClass, Object... params) {
        try {
            Injector wrapper = clone();
            for(Object item : params) {
                wrapper.addMapping(item);
            }
            return wrapper.get(aClass);
        } catch (CloneNotSupportedException e) {
            LoggerFactory.getLogger(this.getClass()).warn("Unable to clone the current injector, skip 'params' injection", e);
            return get(aClass);
        }
    }

    /**
     * Inject instance into an existing object properties.
     * Silently fail on non accessible properties.
     *
     * @param instance The object to work on
     */
    public void injectIntoProperties(Object instance) {
        Set<Field> fields = new HashSet<>();
        fields.addAll(Arrays.asList(instance.getClass().getFields()));
        fields.addAll(Arrays.asList(instance.getClass().getDeclaredFields()));
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    field.set(instance, get(field.getType()));
                } catch (IllegalAccessException e) {
                    LoggerFactory.getLogger(this.getClass()).warn("Can't inject into property " + field.getName(), e);
                }
            }
        }
    }

    /**
     * Inject instances into a method parameters and execute it.
     *
     * @param instance The object
     * @param method   The method to execute
     * @return The method result
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws IllegalArgumentException  <p>if the method is an instance method and the specified object argument is not
     *                                   an instance of the class or interface declaring the underlying method (or of a
     *                                   subclass or implementor thereof); if the number of actual and formal parameters
     *                                   differ; if an unwrapping conversion for primitive arguments fails; or if, after
     *                                   possible unwrapping, a parameter value cannot be converted to the corresponding
     *                                   formal parameter type by a method invocation conversion.</p>
     * @throws IllegalAccessException    if this {@code Method} object is enforcing Java language access control and
     *                                   the underlying method is inaccessible.
     */
    public Object injectIntoMethod(Object instance, Method method)
            throws InvocationTargetException, IllegalAccessException {
        ArrayList<Object> objects = new ArrayList<>();
        for (Class param : method.getParameterTypes()) {
            objects.add(get(param));
        }
        return method.invoke(instance, objects.toArray());
    }

    /**
     * Try to inject instances into a method parameters and execute it base of a method.
     * Will try all method with that name.
     *
     * @param instance   The object
     * @param methodName The method name to execute
     * @return The method result
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws IllegalAccessException    if this {@code Method} object is enforcing Java language access control and
     *                                   the underlying method is inaccessible.
     * @throws IllegalArgumentException  <p>if the method is an instance method and the specified object argument is not
     *                                   an instance of the class or interface declaring the underlying method (or of a
     *                                   subclass or implementor thereof); if the number of actual and formal parameters
     *                                   differ; if an unwrapping conversion for primitive arguments fails; or if, after
     *                                   possible unwrapping, a parameter value cannot be converted to the corresponding
     *                                   formal parameter type by a method invocation conversion.</p>
     * @throws NoSuchMethodException     The no method with the provided name can be executed
     */
    public Object injectIntoMethodName(Object instance, String methodName)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException {
        for (Method method : instance.getClass().getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            if (isMethodInjectable(method)) {
                return injectIntoMethod(instance, method);
            }
        }
        throw new NoSuchMethodException();
    }

    /**
     * Loop over all setters of an object and inject an instance.
     * Silently fail on non accessible setters.
     *
     * @param instance The object to work on
     */
    public void injectIntoSetters(Object instance) {
        Set<Method> methods = new HashSet<>();
        methods.addAll(Arrays.asList(instance.getClass().getDeclaredMethods()));
        methods.addAll(Arrays.asList(instance.getClass().getMethods()));
        for (Method method : methods) {
            if (
                    method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && isMethodInjectable(method)
                    ) {
                try {
                    injectIntoMethod(instance, method);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    LoggerFactory.getLogger(this.getClass()).warn("Can't inject into setter " + method.getName(), e);
                }
            }
        }
    }

    /**
     * Check if a method can be used with injected classes.
     * The method must have the annotation @Inject.
     * Return also true to is the method have the @Inject and no parameters
     *
     * @param method The method to check
     * @return {@code true} is the method can be used
     */
    public Boolean isMethodInjectable(Method method) {
        return isMethodInjectable(method, false);
    }

    /**
     * Check if a method can be used with injected classes.
     *
     * @param method The method to check
     * @param force  If true, the @Inject annotation presence is not checked
     * @return {@code true} if the method can be used
     */
    public Boolean isMethodInjectable(Method method, Boolean force) {
        if (!method.isAnnotationPresent(Inject.class) && !force) {
            return false;
        }
        for (Class variable : method.getParameterTypes()) {
            if (!isInjectable(variable)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indicate if the injector will inject into object properties
     *
     * @return {@code true} if the injection is active
     */
    public Boolean getInjectProperties() {
        return injectProperties;
    }

    /**
     * Indicate if the injector should inject public properties
     *
     * @param injectProperties {@code true} to activate
     */
    public void setInjectProperties(Boolean injectProperties) {
        this.injectProperties = injectProperties;
    }

    /**
     * Indicate if the injector will inject into object setters
     *
     * @return {@code true} if the injection is active
     */
    public Boolean getInjectSetters() {
        return injectSetters;
    }

    /**
     * Indicate if the injector should inject into setters
     *
     * @param injectSetters {@code true} to activate
     */
    public void setInjectSetters(Boolean injectSetters) {
        this.injectSetters = injectSetters;
    }

    @Override
    public Injector clone() throws CloneNotSupportedException {
        Injector clone = (Injector) super.clone();
        clone.workingPackages.addAll(workingPackages);
        clone.mapping.putAll(mapping);
        clone.injectProperties = injectProperties;
        clone.injectSetters = injectSetters;

        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Injector injector = (Injector) o;

        if (!mapping.equals(injector.mapping)) return false;
        if (!workingPackages.equals(injector.workingPackages)) return false;
        if (!injectProperties.equals(injector.injectProperties)) return false;
        return injectSetters.equals(injector.injectSetters);
    }
}
