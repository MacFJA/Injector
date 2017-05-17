# Example 2

This example show the interface use case.

```java
package example;
interface ServiceInterface {
    void connect();
    void disconnect();
    void doAction(String param);
}
class Application {
    private ServiceInterface service;
    public static io.github.macfja.injector.Injector injector;
    
    public static void main(String[] args) {
        injector = new io.github.macfja.injector.Injector("example");
        injector.addMapping(
                ServiceInterface.class,
                new io.github.macfja.injector.InjectionUnit(
                        SocketService.class,
                        io.github.macfja.injector.InjectionUnit.Instantiation.Singleton
                )
        );
        
        injector.get(Application.class);
        
    }
    
    public Application(ServiceInterface service) {
        this.service = service;
        
        doStuff();
    }
       
    
    public void doStuff() {
        service.connect();
        service.doAction("first");
        service.doAction("second");
        service.disconnect();
    }
}

class SocketService implements ServiceInterface {
    @Override
    public void connect() {
        // Open socket
    }
    
    @Override
    public void doAction(String param) {
        // Send data in socket
    }
    
    @Override
    public void disconnect() {
        // Close socket
    }
}

class FileService implements ServiceInterface {
    @Override
    public void connect() {
        // Open file stream
    }
    
    @Override
    public void doAction(String param) {
        // Write in file
    }
    
    @Override
    public void disconnect() {
        // Close file stream
    }
}
```