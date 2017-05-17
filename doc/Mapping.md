# Mapping

A mapping is a rule that indicate the _Injector_ how to handle a class.

There are several mapping:

- The singleton mapping (for a given class, the same instance is always return)
- The interface to implementation (when the interface is requested a new instance of a defined class is provided)
- The interface to singleton (when the interface is requested a singleton is provided)
- The parent to child (when requested, a new instance of the defined class children is returned)
- The parent to child singleton (when requested, a singleton of the defined class children is returned)

## Interface and parent mapping

The **interface to implementation**, the **interface to singleton**, the **parent to child** and the **parent to child singleton** are base on the same principle.  
The method `addMapping` is used in its form:
```
public void addMapping(Class, InjectionUnit)
```
Where the first parameter is the interface/parent and the second contains information about implementation/child/singleton.

```
                             | First parameter   | Second parameter 
-----------------------------+-------------------+------------------------------------------------------------------------------
 interface to implementation | MyInterface.class | new InjectionUnit(MyImpl.class, InjectionUnit.Instantiation.NewInstance)
 interface to singleton      | MyInterface.class | new InjectionUnit(MyImpl.class, InjectionUnit.Instantiation.Singleton)
 interface to singleton      | MyInterface.class | new InjectionUnit(new MyImpl())
 parent to child             | MyAbstract.class  | new InjectionUnit(MyExtender.class, InjectionUnit.Instantiation.NewInstance)
 parent to child singleton   | MyAbstract.class  | new InjectionUnit(MyExtender.class, InjectionUnit.Instantiation.Singleton)
 parent to child singleton   | MyAbstract.class  | new InjectionUnit(new MyExtender())
```

## Singleton mapping

There are 3 way to declare a Singleton:

```
injector.addMapping(new MySingleton());
```

```
injector.addMapping(MySingleton.class, new InjectionUnit(new MySingleton()));
// or
injector.addMapping(MyInterface.class, new InjectionUnit(new MyImplementation()));
```

```
injector.addMapping(MySingleton.class, new InjectionUnit(MySingleton.class, InjectionUnit.Instantiation.Singleton));
// or
injector.addMapping(MyInterface.class, new InjectionUnit(MyImplementation.class, InjectionUnit.Instantiation.Singleton));
```