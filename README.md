# MavenResolver 1.0.0

MavenResolver is an api that allow you to download maven / gradle dependencies at runtime of your application / your plugins instead of exporting all your dependencies in your jar. One of the main advantage is it's completely Thread-Safe and also download the dependencies in parrallel to be faster

## Usage

To start you have to call the api in the Main class of your program.

```java
DependencyManager dependencyManager = new DependencyManager(this.getClass);
```

After you have to preload your dependencies

```java
dependencyManager.preLoad("groupId", "artifactId", "version");
```

and to finish

```java
dependencyManager.load(new File(yourDependeciesFolder));
```

## Example
```java
private DependencyManager dependencyManager;

public static void main(String[] args){
    this.dependencyManager = new DependencyManager(this.getClass);
    loadDependencies();
}

public void loadDependencies(){
        dependencyManager.preLoad(new Dependency("org.redisson", "redisson", "3.13.2"));
        dependencyManager.preLoad(new Dependency("com.rabbitmq", "amqp-client", "5.9.0"));
        dependencyManager.preLoad(new Dependency("org.mongodb", "mongo-java-driver", "3.12.6"));
        dependencyManager.load();
}
```

## Community
If you want to optimize, add new fonctionality or have a problem, you can open a PR with your commit oren an issue if you have a problem

I will soon push it to a public maven repository.
