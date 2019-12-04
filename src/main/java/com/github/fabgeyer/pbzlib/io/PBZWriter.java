
package com.github.fabgeyer.pbzlib.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;


public class PBZWriter {
	private boolean opened;
	private final OutputStream fileStream;
	private final OutputStream gzipStream;
	private final CodedOutputStream cos;
	private Descriptor lastDescriptor = null;

	public PBZWriter(String filename, FileDescriptor fd) throws IOException {
		fileStream = new FileOutputStream(filename);
		gzipStream = new GZIPOutputStream(fileStream);
		cos = CodedOutputStream.newInstance(gzipStream);
		opened = true;

		// Write magic header
		gzipStream.write(Constants.MAGIC);

		// Write FileDescriptor
		FileDescriptorSet fs = FileDescriptorSet.newBuilder().addFile(fd.toProto()).build();
		gzipStream.write(Constants.T_FILE_DESCRIPTOR);
		fs.writeDelimitedTo(gzipStream);
	}

	public void add(Message msg) throws IOException {
		if (!opened) {
			throw new IOException("Cannot add message to closed file");
		}

		Descriptor descriptor = msg.getDescriptorForType();
		if (descriptor != lastDescriptor) {
			gzipStream.write(Constants.T_DESCRIPTOR_NAME);
			byte[] bytes = descriptor.getFullName().getBytes();
			cos.writeUInt32NoTag(bytes.length);
			cos.flush();
			gzipStream.write(bytes);

			lastDescriptor = descriptor;
		}

		gzipStream.write(Constants.T_MESSAGE);
		msg.writeDelimitedTo(gzipStream);
		gzipStream.flush();
	}

	public void close() throws IOException {
		if (!opened) {
			return;
		}

		gzipStream.close();
		fileStream.close();
		opened = true;
	}
}
