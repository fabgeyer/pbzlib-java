# Library for serializing protobuf objects - Java version

This library is used for simplifying the serialization and deserialization of [protocol buffer](https://developers.google.com/protocol-buffers/) objects to/from files.
The main use-case is to save and read a large collection of objects of the same type.
Each file contains a header with the description of the protocol buffer, meaning that no compilation of `.proto` description file is required before reading a `pbz` file.

**WARNING:** This library is currently in alpha state and can only read `pbz` files.


## Installation and compilation

```
$ git clone https://github.com/fabgeyer/pbzlib-java.git
$ cd pbzlib-java
$ gradle build
```


## Example

Reading a `pbz` file:

```java
import com.github.fabgeyer.pbzlib.io.PBZReader;

public class Main {
	public static void main(String[] args) throws Exception {
		Reader rdr = new PBZReader("file.pbz");
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


## Versions in other languages

- [Python version](https://github.com/fabgeyer/pbzlib-py)
- [Go version](https://github.com/fabgeyer/pbzlib-go)
- [C/C++ version](https://github.com/fabgeyer/pbzlib-c-cpp)
