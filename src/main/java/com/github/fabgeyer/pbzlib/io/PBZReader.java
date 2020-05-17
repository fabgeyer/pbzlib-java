
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
	private boolean opened;
	private final InputStream fileStream;
	private final InputStream gzipStream;
	private final CodedInputStream ciStream;
	private final Map<String, Descriptor> descriptorsByName = new HashMap<>();
	private Descriptor nextDescriptor = null;

	public PBZReader(String filename) throws IOException, DescriptorValidationException {
		fileStream = new FileInputStream(filename);
		gzipStream = new GZIPInputStream(fileStream);

		// Parse magic header
		byte[] header = new byte[2];
		gzipStream.read(header, 0, 2);
		if (!Arrays.equals(header, Constants.MAGIC)) {
			throw new IOException("Invalid magic header");
		}
		ciStream = CodedInputStream.newInstance(gzipStream);

		while (true) {
			int type = ciStream.readRawByte();
			int size = ciStream.readRawVarint32();
			byte[] buf = ciStream.readRawBytes(size);

			if (type == Constants.T_PROTOBUF_VERSION) {
				// Ignore protobuf version
				continue;
			}

			if (type != Constants.T_FILE_DESCRIPTOR) {
				throw new IOException("Invalid message type");
			}

			try {
				FileDescriptor[] deps = new FileDescriptor[0];
				FileDescriptorSet fs = FileDescriptorSet.parseFrom(buf);
				for (FileDescriptorProto fdp : fs.getFileList()) {
					FileDescriptor fd = FileDescriptor.buildFrom(fdp, deps);
					for (Descriptor descr : fd.getMessageTypes()) {
						descriptorsByName.put(descr.getFullName(), descr);
					}
				}
			} catch (Exception e) {
				throw new IOException("Could not parse PBZ descriptor file!");
			}
			break;
		}
	
		opened = true;
	}

	public DynamicMessage nextMessage() throws IOException {
		if (!opened) {
			return null;
		}

		while (true) {
			if (ciStream.isAtEnd()) {
				break;
			}

			int type = ciStream.readRawByte();
			int size = ciStream.readRawVarint32();
			byte[] buf = ciStream.readRawBytes(size);

			if (type == Constants.T_DESCRIPTOR_NAME) {
				String name = new String(buf);
				nextDescriptor = descriptorsByName.get(name);
				if (nextDescriptor == null) {
					throw new IOException("Descritor '" + name + "' not found");
				}
			}
			else if (type == Constants.T_MESSAGE) {
				DynamicMessage msg = DynamicMessage.parseFrom(nextDescriptor, buf);
				return msg;
			}
			else {
				throw new IOException("Invalid message type: " + type);
			}
		}

		close();
		return null;
	}

	public void close() throws IOException {
		if (!opened) {
			return;
		}

		gzipStream.close();
		fileStream.close();
		opened = false;
	}
}
