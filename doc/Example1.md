# Example 1

This example is a case where method injection is used (differing class loading).

This approach can also be used for lazy loading.

```java
package example;

class PanelA extends javax.swing.JPanel {
    // Do your stuff
}

class PanelB extends javax.swing.JPanel {
    // Do other stuff
}

class Application extends javax.swing.JFrame {
    private static io.github.macfja.injector.Injector injector = new io.github.macfja.injector.Injector("example");
    
    public static void main(String[] args) {
        injector.addMapping(
                Application.class,
                new io.github.macfja.injector.InjectionUnit(
                        Application.class,
                        io.github.macfja.injector.InjectionUnit.Instantiation.Singleton
                )
        );
        
        injector.get(Application.class);
        
    }
    
    public Application(PanelA panel) {
        setContentPane(panel);
        setVisible(true);
        
        new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        injector.injectIntoMethodName(Application.this, "init");
                    }
                }, 1000);
    }
    
    public void init(PanelB panelB) {
        setContentPane(panelB);
    }
}
```