# Jargo - JSON Databinding made simple

Jargo is a small, lightweight, zero-configuration alternative to Jackson Databind.

The following Classes can be (de)serialized:

- Primitives `byte`, `short`, `int`, `long`, `double`, `boolean`
  - ... and their boxed equivalents in `java.lang`
- `java.lang.String`
- Enums
- Records
- `java.time.Instant`
- `List`, `Set`, `Map` with specified type arguments
- `java.util.Optional`

# Usage

```java
import org.jargo.Jargo;

public class JargoDemo {
  public static void main(String[] args) {
    String serialized = Jargo.serialize("Hello, Jargo");
    System.out.println(serialized); // "Hello, Jargo"
    Object deserialized = Jarge.deserialize(serialized);
    System.out.println(deserialized.getClass()); // java.lang.String
    System.out.println(deserialized); // Hello, Jargo
  }
}
```

# Advanced Usage

Records with only one component named `value` are considered opaque and will not end up as an object
in the serialized json. This allows developers to use simple [ValueObjects](https://martinfowler.com/bliki/ValueObject.html).

Example:

```java
import org.jargo.Jargo;

public class JargoDemo {
  
  record Person(Firstname firstname, Listname lastname) {}
  record Firstname(String value) {}
  record Lastname(String value) {}
  
  public static void main(String[] args) {
    Person person = new Person(new Firstname("John"), new Firstname("Doe"));
    String serialized = Jargo.serialize(person);
    System.out.println(serialized); // {"firstname":"John","lastname":"Doe"}
    Object deserialized = Jarge.deserialize(serialized);
    System.out.println(deserialized); // Person[firstname=Firstname[value=John], lastname=Lastname[value=Doe]]
  }
}
```

# Contribute

Contributions are welcome! Please keep in mind that this is a private project from some dude on the
internet, so don't expect immediate responses.
