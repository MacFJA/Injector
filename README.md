# Injector

 - [Injection possibility](#injection)
   - [Constructor Injection](#injection-constructor)
   - [Setters Injection](#injection-setters)
   - [Properties Injection](#injection-properties)
   - [Method Injection](#injection-method)
 - [Injection types](#types)
 - [Installation](#installation)
 - [Examples](#examples)
   - [Declaring a mapping](#examples-mapping)
     - [For a singleton](#examples-mapping-singleton)
     - [For an interface/abstract class to concrete implementation](#examples-mapping-interface)

## Injection possibility<a id="injection"></a>

This library offer several types of injection:

- Constructors injection
- Setters injection
- Properties injection
- Method injection

### Constructor Injection<a id="injection-constructor"></a>

The constructor injection try to create a class instance by looping over every class constructor until it found one that can be used .

The injection criteria are:

- Parameters are not Java primitive
- Parameters packages are in injector package list
- All parameters constructor do the same

### Setters Injection<a id="injection-setters"></a>

The setter injection is automatically run after the constructor injection if the injector have the option activated.
(Can be also be call on an existing instance)  
For a setter method to be injected, it must validate the following conditions:

- The method name **MUST** start with `set`
- The method **MUST** have exactly one parameter
- The method **MUST** have the annotation `@javax.inject.Inject`
- The method parameter must be an injectable class

### Properties Injection<a id="injection-properties"></a>

The property injection is automatically run after the constructor injection if the injector have the option activated.
(Can be also be call on an existing instance)  
For a property to be injected, it must validate the following conditions:

- The property **MUST** be accessible
- The property **MUST** have the annotation `@javax.inject.Inject`
- The property must be an injectable class

### Method Injection<a id="injection-method"></a>

A method can have its parameters injected.

There are two way to inject parameters in a method.  
First one is with a `java.lang.reflect.Method` object, in this case there are no control, if a parameter can't be injected `null` will be used.  
The second way is to use the method name, with this way, all method of the object with this name will be try, and the method must have the annotation `@javax.inject.Inject` and every parameters must be injectable.

## Injection types<a id="types"></a>

There are two injection types:

- Singleton
- Every times a new instance

## Installation<a id="installation"></a>

Clone the project:
```
git clone https://github.com/MacFJA/Injector.git
```
Install the project into your local Maven repository:
```
cd Injector/
mvn clean
mvn install
```
Remove the source:
```
cd ..
rm -r Injector/
```
Add the depency in your Maven project:
```xml
<project>
    <!-- ... -->
    <dependencies>
        <!-- ... -->
        <dependency>
            <groupId>io.github.macfja</groupId>
            <artifactId>injector</artifactId>
            <version>1.1.0</version>
        </dependency>
        <!-- ... -->
    </dependencies>
    <!-- ... -->
</project>
```

## Examples<a id="examples"></a>

### Declaring a mapping<a id="examples-mapping"></a>

#### For a singleton<a id="examples-mapping-singleton"></a>

```java
io.github.macfja.injector.Injector injector = new io.github.macfja.injector.Injector("mypackage");
injector.addMapping(new mypackage.MyClass());
// ... later
injector.get(mypackage.MyClass.class); // return the instance created in addMapping method
// ... later
injector.get(mypackage.MyClass.class); // still the same instance
```

or

```java
io.github.macfja.injector.Injector injector = new io.github.macfja.injector.Injector("mypackage");
injector.addMapping(mypackage.MyClass.class, new io.github.macfja.injector.InjectionUnit(new mypackage.MyClass()));
// ... later
injector.get(mypackage.MyClass.class); // return the instance created in addMapping method
// ... later
injector.get(mypackage.MyClass.class); // still the same instance
```

or

```java
io.github.macfja.injector.Injector injector = new io.github.macfja.injector.Injector("mypackage");
injector.addMapping(
    mypackage.MyClass.class,
    new io.github.macfja.injector.InjectionUnit(
        mypackage.MyClass.class,
        io.github.macfja.injector.InjectionUnit.Instantiation.Singleton
    )
);
// ... later
injector.get(mypackage.MyClass.class); // create a new instance (first call)
// ... later
injector.get(mypackage.MyClass.class); // still the same instance
```

or

```java
io.github.macfja.injector.Injector injector = new io.github.macfja.injector.Injector("mypackage");
injector.addMapping(
    mypackage.MyClass.class,
    io.github.macfja.injector.InjectionUnit.Instantiation.Singleton
);
// ... later
injector.get(mypackage.MyClass.class); // create a new instance (first call)
// ... later
injector.get(mypackage.MyClass.class); // still the same instance
```

#### For an interface/abstract class to concrete implementation<a id="examples-mapping-interface"></a>

```java
io.github.macfja.injector.Injector injector = new io.github.macfja.injector.Injector("mypackage");
injector.addMapping(mypackage.MyInterface.class, new io.github.macfja.injector.InjectionUnit(/* ... */));
// ... later
injector.get(mypackage.MyInterface.class); // return an instance according to the InjectionUnit
```