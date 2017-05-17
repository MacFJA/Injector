package io.github.macfja.injector;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class InjectorTest {
    @Test
    public void testConstructors() {
        Injector simple = new Injector("io.github");
        Assert.assertNotNull(simple);

        Injector multiple = new Injector(new HashSet<String>(Arrays.asList("io.github", "io.github.macfja")));
        Assert.assertNotNull(multiple);
    }

    @Test
    public void testMethodAddMappingSingleton() {
        int startAt = TestISingleton.getCount();
        Injector simple = new Injector("io.github");
        simple.addMapping(new TestISingleton());
        Assert.assertEquals(startAt + 1, TestISingleton.getCount());
        simple.get(TestISingleton.class);
        Assert.assertEquals(startAt + 1, TestISingleton.getCount());
    }

    @Test
    public void testMethodAddMappingNormalSingleton() {
        int startAt = TestISingleton.getCount();
        Injector simple = new Injector("io.github");
        simple.addMapping(TestISingleton.class, new InjectionUnit(TestISingleton.class, InjectionUnit.Instantiation.Singleton));
        Assert.assertEquals(startAt, TestISingleton.getCount());
        simple.get(TestISingleton.class);
        Assert.assertEquals(startAt + 1, TestISingleton.getCount());
        simple.get(TestISingleton.class);
        Assert.assertEquals(startAt + 1, TestISingleton.getCount());
    }

    @Test
    public void testMethodAddMappingNormalNewInstance() {
        int startAt = TestISingleton.getCount();
        Injector simple = new Injector("io.github");
        simple.addMapping(TestISingleton.class, new InjectionUnit(TestISingleton.class, InjectionUnit.Instantiation.NewInstance));
        Assert.assertEquals(startAt, TestISingleton.getCount());
        simple.get(TestISingleton.class);
        Assert.assertEquals(startAt + 1, TestISingleton.getCount());
        simple.get(TestISingleton.class);
        Assert.assertEquals(startAt + 2, TestISingleton.getCount());
    }

    @Test
    public void testMethodAddWorkingPackage() {
        Injector injector = new Injector("io.github");
        Assert.assertFalse(injector.isInjectable(InjectionUnit.class));
        injector.addWorkingPackage("java.lang");
        Assert.assertTrue(injector.isInjectable(InjectionUnit.class));
    }

    @Test
    public void testMethodIsInjectable() {
        Injector injector = new Injector("io.github");

        Assert.assertFalse(injector.isInjectable(Integer.TYPE));
        Assert.assertFalse(injector.isInjectable(TestIPrimitive.class));

        Integer number = 10;
        Assert.assertFalse(injector.isInjectable(TestIJavaPackage.class));
        injector.addMapping(number);
        Assert.assertTrue(injector.isInjectable(TestIJavaPackage.class));
    }

    @Test
    public void testMethodGetSimple() {
        Injector injector = new Injector("io.github");
        int count = TestISingleton.getCount();
        Assert.assertEquals(count, TestISingleton.getCount());
        injector.get(TestISingleton.class);
        Assert.assertEquals(count + 1, TestISingleton.getCount());

        injector.addMapping(new TestISingleton());
        Assert.assertEquals(count + 2, TestISingleton.getCount());
        injector.get(TestISingleton.class);
        Assert.assertEquals(count + 2, TestISingleton.getCount());

        Assert.assertNull(injector.get(TestIPrimitive.class));
    }

    @Test
    public void testMethodGetComplex() {
        Injector injector = new Injector("io.github");

        Assert.assertNull(injector.get(TestIJavaPackage.class));

        Assert.assertNotNull(injector.get(TestIJavaPackage.class, 10));
    }

    @Test
    public void testInjectIntoProperties() {
        Injector injector = new Injector("io.github");
        TestIInjections testIProperties = new TestIInjections();

        injector.injectIntoProperties(testIProperties);

        Assert.assertNotNull(testIProperties.getPublicProp());
        Assert.assertNotNull(testIProperties.getProtectedProp());
        Assert.assertNotNull(testIProperties.getPackageProp());
        Assert.assertNull(testIProperties.getPrivateProp());
    }

    @Test
    public void testInjectIntoMethod() {
        Injector injector = new Injector("io.github");
        TestIInjections testIProperties = new TestIInjections();

        Set<Method> methods = new HashSet<>();
        methods.addAll(Arrays.asList(testIProperties.getClass().getMethods()));
        methods.addAll(Arrays.asList(testIProperties.getClass().getDeclaredMethods()));
        for (Method field : methods) {
            try {
                injector.injectIntoMethod(testIProperties, field);
            } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                // noop
            }
        }

        Assert.assertNotNull(testIProperties.getPublicProp());
        Assert.assertNotNull(testIProperties.getProtectedProp());
        Assert.assertNotNull(testIProperties.getPackageProp());
        Assert.assertNull(testIProperties.getPrivateProp());
    }

    @Test
    public void testInjectIntoMethodName() {
        Injector injector = new Injector("io.github");
        TestIInjections testIProperties = new TestIInjections();

        List<String> methods = new ArrayList<>(Arrays.asList(
                "setPublicProp",
                "setProtectedProp",
                "setPackageProp",
                "setPrivateProp"
        ));
        for (String field : methods) {
            try {
                injector.injectIntoMethodName(testIProperties, field);
            } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
                // noop
            }
        }

        Assert.assertNotNull(testIProperties.getPublicProp());
        Assert.assertNull(testIProperties.getProtectedProp());
        Assert.assertNull(testIProperties.getPackageProp());
        Assert.assertNull(testIProperties.getPrivateProp());
    }

    @Test
    public void testMethodInjectSetters() {
        Injector injector = new Injector("io.github");
        TestIInjections testIProperties = new TestIInjections();

        injector.injectIntoSetters(testIProperties);

        Assert.assertNotNull(testIProperties.getPublicProp());
        Assert.assertNotNull(testIProperties.getProtectedProp());
        Assert.assertNotNull(testIProperties.getPackageProp());
        Assert.assertNull(testIProperties.getPrivateProp());
    }

    @Test
    public void testMethodIsMethodInjectable() {
        Injector injector = new Injector("io.github");
        TestIInjections testIProperties = new TestIInjections();

        Method[] methods = testIProperties.getClass().getMethods();

        for (Method method : methods) {
            if (Arrays.asList("setPublicProp", "setProtectedProp", "setPackageProp").contains(method.getName())) {
                Assert.assertTrue(injector.isMethodInjectable(method));
            } else {
                Assert.assertFalse(injector.isMethodInjectable(method));
            }
        }

        for (Method method : methods) {
            if (Arrays.asList("setPublicProp", "setProtectedProp", "setPackageProp", "getPublicProp", "getProtectedProp", "getPackageProp", "getPrivateProp").contains(method.getName())) {
                Assert.assertTrue(injector.isMethodInjectable(method, true));
            }
            if (Objects.equals(method.getName(), "equals")) {
                Assert.assertFalse(injector.isMethodInjectable(method, true));
                Assert.assertFalse(injector.isMethodInjectable(method, false));
            }
        }
    }

    @Test
    public void testPropertyInjectProperties() {
        Injector injector = new Injector("io.github");
        Assert.assertTrue(injector.getInjectProperties());
        injector.setInjectProperties(false);
        Assert.assertFalse(injector.getInjectProperties());
    }

    @Test
    public void testPropertyInjectSetters() {
        Injector injector = new Injector("io.github");
        Assert.assertTrue(injector.getInjectSetters());
        injector.setInjectSetters(false);
        Assert.assertFalse(injector.getInjectSetters());
    }

    @Test
    public void testMethodCloneEquals() {
        Injector injector = new Injector("io.github");
        try {
            Injector clone = injector.clone();
            Assert.assertEquals(injector, clone);
            Assert.assertNotSame(injector, clone);
        } catch (CloneNotSupportedException e) {
            Assert.fail();
        }
    }
}

class TestISingleton {
    private static int count = 0;

    public TestISingleton() {
        count++;
    }

    public static int getCount() {
        return count;
    }
}

class TestIPrimitive {
    public TestIPrimitive(int value) {
    }
}

class TestIJavaPackage {
    public TestIJavaPackage(Integer value) {
    }
}

class TestIInjections {
    @Inject
    public TestISingleton publicProp;
    @Inject
    protected TestISingleton protectedProp;
    @Inject
    TestISingleton packageProp;
    @Inject
    private TestISingleton privateProp;

    public TestISingleton getPublicProp() {
        return publicProp;
    }

    public TestISingleton getProtectedProp() {
        return protectedProp;
    }

    public TestISingleton getPackageProp() {
        return packageProp;
    }

    public TestISingleton getPrivateProp() {
        return privateProp;
    }

    @Inject
    public void setPublicProp(TestISingleton publicProp) {
        this.publicProp = publicProp;
    }

    @Inject
    protected void setProtectedProp(TestISingleton protectedProp) {
        this.protectedProp = protectedProp;
    }

    @Inject
    void setPackageProp(TestISingleton packageProp) {
        this.packageProp = packageProp;
    }

    @Inject
    private void setPrivateProp(TestISingleton privateProp) {
        this.privateProp = privateProp;
    }
}