package io.github.macfja.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * InjectionUnit class.
 * Contains information about the class to inject and if the instance must be a singleton or not.
 *
 * @author MacFJA
 */
public class InjectionUnit implements Cloneable {
    /**
     * Class to use when requesting an instance
     */
    private final Class toInject;
    /**
     * The type of instance
     */
    private final Instantiation type;
    /**
     * The singleton instance (if the Instantiation is NewInstance)
     */
    private Object singleton;

    /**
     * Simple Constructor
     *
     * @param toInject The class that will be used
     * @param type     The type of instance (Singleton or not)
     */
    public InjectionUnit(Class toInject, Instantiation type) {
        this.toInject = toInject;
        this.type = type;
    }

    /**
     * Constructor with a pre-generated singleton;
     *
     * @param singletonInstance The singleton to use on later.
     */
    public InjectionUnit(Object singletonInstance) {
        this.toInject = singletonInstance.getClass();
        this.singleton = singletonInstance;
        this.type = Instantiation.Singleton;
    }

    /**
     * Check if a class have at least one constructor that can be used
     *
     * @param toInject The class to check
     * @param injector The class injector
     * @return {@code true} if a constructor can be use
     */
    public static Boolean isInstantiable(Class toInject, Injector injector) {
        for (Constructor constructor : toInject.getConstructors()) {
            if (isConstructorInjectable(constructor, injector)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a constructor can be use (no params, or all params can be injected)
     *
     * @param constructor The constructor to check
     * @param injector    The class injector
     * @return {@code true} if the constructor can be use
     */
    public static Boolean isConstructorInjectable(Constructor constructor, Injector injector) {
        for (Class variable : constructor.getParameterTypes()) {
            if (!injector.isInjectable(variable)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get an instance
     *
     * @return The instance
     * @throws IllegalAccessException    if this {@code Constructor} object is enforcing Java language access control
     *                                   and the underlying constructor is inaccessible.
     * @throws InvocationTargetException if the underlying constructor throws an exception.
     * @throws InstantiationException    if the class that declares the underlying constructor represents
     *                                   an abstract class.
     */
    public Object get(Injector parent) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (Instantiation.Singleton.equals(type)) {
            if (singleton == null) {
                singleton = build(parent);
            }
            return singleton;
        }
        return build(parent);
    }

    /**
     * Create an instance of toInject class
     *
     * @param parent The parent injector (which initiate the build)
     * @return The new instance
     * @throws IllegalAccessException    if this {@code Constructor} object is enforcing Java language access control
     *                                   and the underlying constructor is inaccessible.
     * @throws InstantiationException    if the class that declares the underlying constructor
     *                                   represents an abstract class.
     * @throws InvocationTargetException if the underlying constructor throws an exception.
     */
    private Object build(Injector parent) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object instance = null;

        if (toInject.getConstructors().length == 0) {
            instance = toInject.newInstance();
        } else {
            for (Constructor constructor : toInject.getConstructors()) {
                if (isConstructorInjectable(constructor, parent)) {
                    instance = runConstructor(constructor, parent);
                    break;
                }
            }

            if (instance == null) {
                throw new InstantiationException();
            }
        }

        if (parent.getInjectProperties()) {
            parent.injectIntoProperties(instance);
        }
        if (parent.getInjectSetters()) {
            parent.injectIntoSetters(instance);
        }
        return instance;
    }

    /**
     * Inject class and execute constructor
     *
     * @param constructor The constructor to execute
     * @param parent      The parent injector (which initiate the build)
     * @return A new instance created with the constructor
     * @throws IllegalAccessException    if this {@code Constructor} object is enforcing Java language access control
     *                                   and the underlying constructor is inaccessible.
     * @throws InvocationTargetException if the underlying constructor throws an exception.
     * @throws InstantiationException    if the class that declares the underlying constructor
     *                                   represents an abstract class.
     */
    private Object runConstructor(Constructor constructor, Injector parent)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        ArrayList<Object> objects = new ArrayList<>();
        for (Class param : constructor.getParameterTypes()) {
            objects.add(parent.get(param));
        }
        return constructor.newInstance(objects.toArray());

    }

    /**
     * Check if the class to inject have at least one constructor that can be used
     *
     * @param injector The class injector
     * @return {@code true} if a constructor can be use
     */
    public Boolean isInstantiable(Injector injector) {
        return isInstantiable(toInject, injector);
    }

    /**
     * List of possible instance type
     */
    public enum Instantiation {
        Singleton,
        NewInstance
    }

    @Override
    public InjectionUnit clone() throws CloneNotSupportedException {
        InjectionUnit clone = (InjectionUnit) super.clone();
        if (Instantiation.Singleton.equals(type)) {
            clone.singleton = singleton;
        }
        return clone;
    }
}
