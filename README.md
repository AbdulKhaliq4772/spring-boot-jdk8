# Spring-boot JDK 8

## Quickstart

1. Clone this git repo
1. Open two terminal windows and change to the cloned directory
1. In terminal window 1 run: `mvn spring-boot:run`
1. Open **http://localhost:8080/greeting** in a browser window. Login with user/password
1. Make changes in the **GreetingController** class and/or the **greeting.html** template file
1. In terminal window 2 run: `mvn compile`
1. Reload the web page in the browser to see the changes

Instead of manually running mvn compile you can make the compilation from within your favorite IDE as long as:

1. The version of the java compiler is the same
1. The IDE puts the compiled classes in the **target/classes** directory

You can even enable auto-compilation if your IDE supports that, for totally seamless development.