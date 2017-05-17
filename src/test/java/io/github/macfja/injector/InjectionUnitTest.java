package io.github.macfja.injector;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class InjectionUnitTest {
    private static Injector parent;

    @BeforeClass
    public static void beforeSetup() {
        parent = new Injector("io.github");
    }

    @Test
    public void testSingletonConstructor() {
        int expected = TestIUSingleton.getStaticCount() + 1;
        TestIUSingleton singleton = new TestIUSingleton();
        InjectionUnit unit = new InjectionUnit(singleton);

        Assert.assertEquals(expected, singleton.getCount());
        try {
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Assert.fail();
        }
    }

    @Test
    public void testConstructorTypeSingleton() {
        int expected = TestIUSingleton.getStaticCount() + 1;
        InjectionUnit unit = new InjectionUnit(TestIUSingleton.class, InjectionUnit.Instantiation.Singleton);
        try {
            Assert.assertEquals(expected - 1, TestIUSingleton.getStaticCount());
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUSingleton.getStaticCount());
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUSingleton.getStaticCount());
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUSingleton.getStaticCount());
            Assert.assertEquals(expected, ((TestIUSingleton) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUSingleton.getStaticCount());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Assert.fail();
        }
    }

    @Test
    public void testConstructorTypeNewInstance() {
        int expected = TestIUNewInstance.getStaticCount() + 1;
        InjectionUnit unit = new InjectionUnit(TestIUNewInstance.class, InjectionUnit.Instantiation.NewInstance);
        try {
            Assert.assertEquals(expected - 1, TestIUNewInstance.getStaticCount());
            Assert.assertEquals(expected, ((TestIUNewInstance) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUNewInstance.getStaticCount());
            expected++;
            Assert.assertEquals(expected, ((TestIUNewInstance) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUNewInstance.getStaticCount());
            expected++;
            Assert.assertEquals(expected, ((TestIUNewInstance) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUNewInstance.getStaticCount());
            expected++;
            Assert.assertEquals(expected, ((TestIUNewInstance) unit.get(parent)).getCount());
            Assert.assertEquals(expected, TestIUNewInstance.getStaticCount());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Assert.fail();
        }
    }

    @Test
    public void testStaticMethodIsInstantiable() {
        Assert.assertTrue(InjectionUnit.isInstantiable(TestIUSingleton.class, parent));
        Assert.assertFalse(InjectionUnit.isInstantiable(TestIUPrivateConstructor.class, parent));
    }

    @Test
    public void testStaticMethodIsConstructorInjectable() {
        try {
            Constructor singletonConstructor = TestIUSingleton.class.getConstructor();
            Assert.assertTrue(InjectionUnit.isConstructorInjectable(singletonConstructor, parent));
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getClass().getName());
        }

        try {
            Constructor uninjectable = TestIUUnInjectableConstructor.class.getConstructor(java.awt.Button.class);
            Assert.assertFalse(InjectionUnit.isConstructorInjectable(uninjectable, parent));
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getClass().getName());
        }

    }

    @Test
    public void testMethodBuild() {
        InjectionUnit unit = new InjectionUnit(TestIUNoConstructor.class, InjectionUnit.Instantiation.NewInstance);
        try {
            TestIUNoConstructor object = (TestIUNoConstructor) unit.get(parent);
            Assert.assertNotNull(object);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Assert.fail();
        }

        unit = new InjectionUnit(TestIUUnInjectableConstructor.class, InjectionUnit.Instantiation.NewInstance);
        try {
            TestIUUnInjectableConstructor object = (TestIUUnInjectableConstructor) unit.get(parent);
            Assert.fail();
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assert.fail();
        } catch (InstantiationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testMethodRunConstructor() {
        InjectionUnit unit = new InjectionUnit(TestIUMultipleParamConstructor.class, InjectionUnit.Instantiation.NewInstance);
        try {
            TestIUMultipleParamConstructor object = (TestIUMultipleParamConstructor) unit.get(parent);
            Assert.assertNotNull(object);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            Assert.fail();
        }
    }

    @Test
    public void testMethodIsInstantiable() {
        InjectionUnit unit = new InjectionUnit(TestIUSingleton.class, InjectionUnit.Instantiation.Singleton);
        Assert.assertTrue(unit.isInstantiable(parent));

        unit = new InjectionUnit(TestIUPrivateConstructor.class, InjectionUnit.Instantiation.NewInstance);
        Assert.assertFalse(unit.isInstantiable(parent));
    }

    @Test
    public void testMethodClone() {
        InjectionUnit unit = new InjectionUnit(new TestIUSingleton());
        try {
            InjectionUnit clone = unit.clone();
        } catch (CloneNotSupportedException e) {
            Assert.fail();
        }
    }
}

class TestIUSingleton {
    private static int count = 0;

    public TestIUSingleton() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public static int getStaticCount() {
        return count;
    }
}

class TestIUNewInstance {
    private static int count = 0;

    public TestIUNewInstance() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public static int getStaticCount() {
        return count;
    }
}

class TestIUPrivateConstructor {
    private TestIUPrivateConstructor() {
    }
}

class TestIUUnInjectableConstructor {
    public TestIUUnInjectableConstructor(java.awt.Button button) {
    }
}

class TestIUNoConstructor {
}

class TestIUMultipleParamConstructor {
    public TestIUMultipleParamConstructor(TestIUNewInstance instance1, TestIUNewInstance instance2) {
    }
}