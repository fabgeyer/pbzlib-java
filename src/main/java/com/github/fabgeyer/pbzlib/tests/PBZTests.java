
package com.github.fabgeyer.pbzlib.tests;

import com.github.fabgeyer.pbzlib.io.PBZReader;
import com.github.fabgeyer.pbzlib.io.PBZWriter;
import com.github.fabgeyer.pbzlib.tests.Header;
import com.github.fabgeyer.pbzlib.tests.Object;
import com.github.fabgeyer.pbzlib.tests.Messages;

import com.google.protobuf.DynamicMessage;


public class PBZTests {
	public static void main(String[] args) throws Exception {
		PBZWriter wrt = new PBZWriter("test.pbz", Messages.getDescriptor());
		Header hdr = Header.newBuilder().setVersion(1).build();
		wrt.add(hdr);
		for (int i = 0; i < 10; i++) {
			Object obj = Object.newBuilder().setId(i).build();
			wrt.add(obj);
		}
		wrt.close();

		PBZReader rdr = new PBZReader("test.pbz");
		while (true) {
			DynamicMessage msg = rdr.nextMessage();
			if (msg == null) {
				break;
			}
			System.out.println(msg);
		}
	}
}
