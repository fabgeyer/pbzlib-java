# Library for serializing protobuf objects - Java version

This library is used for simplifying the serialization and deserialization of [protocol buffer](https://developers.google.com/protocol-buffers/) objects to/from files.
The main use-case is to save and read a large collection of objects of the same type.
Each file contains a header with the description of the protocol buffer, meaning that no compilation of `.proto` description file is required before reading a `pbz` file.


## Installation and compilation

```
$ git clone https://github.com/fabgeyer/pbzlib-java.git
$ cd pbzlib-java
$ gradle build
```


## Example
### Reading

Reading a `pbz` file:

```java
import com.github.fabgeyer.pbzlib.io.PBZReader;
import com.google.protobuf.DynamicMessage;

public class Main {
	public static void main(String[] args) throws Exception {
		PBZReader rdr = new PBZReader("file.pbz");
		while (true) {
			DynamicMessage msg = rdr.nextMessage();
			if (msg == null) {
				break;
			}
			System.out.println(msg);
		}
	}
}
```

Reading a `pbz` file with a given target class:

```java
import your.protobuf.YourProtobufClass;
import com.github.fabgeyer.pbzlib.io.PBZReader;

public class Main {
	public static void main(String[] args) throws Exception {
		PBZReader rdr = new PBZReader("file.pbz");
		while (true) {
			YourProtobufClass obj = rdr.nextMessage(YourProtobufClass.parser());
			if (obj == null) {
				break;
			}
			System.out.println(obj);
		}
	}
}
```

### Writing

Writing a `pbz` file:

```java
import com.github.fabgeyer.pbzlib.io.PBZWriter;
import your.protobuf.MainClass;
import your.protobuf.Message;

public class Main {
	public static void main(String[] args) throws Exception {
		PBZReader wrtr = new PBZWriter("file.pbz", MainClass.getDescriptor());
		Message msg = Message.newBuilder.setField(value).build();
		wrtr.add(msg);
		wrtr.close();
	}
}
```


## Versions in other languages

- [Python version](https://github.com/fabgeyer/pbzlib-py)
- [Go version](https://github.com/fabgeyer/pbzlib-go)
- [C/C++ version](https://github.com/fabgeyer/pbzlib-c-cpp)
