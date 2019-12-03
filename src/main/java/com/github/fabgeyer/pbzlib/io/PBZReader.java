
package com.github.fabgeyer.pbzlib.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;


public class PBZReader {
	public static final byte[] MAGIC = {0x41, 0x42};
	public static final int T_FILE_DESCRIPTOR = 1;
	public static final int T_DESCRIPTOR_NAME = 2;
	public static final int T_MESSAGE = 3;

	private boolean opened;
	private final InputStream fileStream;
	private final InputStream gzipStream;
	private final Map<String, Descriptor> descriptorsByName = new HashMap<>();
	private Descriptor nextDescriptor = null;

	public Reader(String filename) throws IOException, DescriptorValidationException {
		fileStream = new FileInputStream(filename);
		gzipStream = new GZIPInputStream(fileStream);

		// Parse magic header
		byte[] header = new byte[2];
		gzipStream.read(header, 0, 2);
		if (!Arrays.equals(header, MAGIC)) {
			throw new IOException("Invalid magic header");
		}

		int type = gzipStream.read();
		int firstByte = gzipStream.read();
		int size = CodedInputStream.readRawVarint32(firstByte, gzipStream);

		byte[] buf = new byte[size];
		gzipStream.read(buf, 0, size);

		FileDescriptor[] deps = new FileDescriptor[0];
		FileDescriptorSet fs = FileDescriptorSet.parseFrom(buf);
		for (FileDescriptorProto fdp : fs.getFileList()) {
			FileDescriptor fd = FileDescriptor.buildFrom(fdp, deps);
			for (Descriptor descr : fd.getMessageTypes()) {
				descriptorsByName.put(descr.getFullName(), descr);
			}
		}
	
		opened = true;
	}

	public DynamicMessage nextMessage() throws IOException {
		if (!opened) {
			return null;
		}

		while (true) {
			if (gzipStream.available() == 0) {
				break;
			}

			int type = gzipStream.read();
			int firstByte = gzipStream.read();
			int size = CodedInputStream.readRawVarint32(firstByte, gzipStream);

			byte[] buf = new byte[size];
			gzipStream.read(buf, 0, size);

			if (type == T_DESCRIPTOR_NAME) {
				String name = new String(buf);
				nextDescriptor = descriptorsByName.get(name);
				if (nextDescriptor == null) {
					throw new IOException("Descritor '" + name + "' not found");
				}
			}
			else if (type == T_MESSAGE) {
				DynamicMessage msg = DynamicMessage.parseFrom(nextDescriptor, buf);
				return msg;
			}
		}

		gzipStream.close();
		fileStream.close();
		opened = false;
		return null;
	}
}
