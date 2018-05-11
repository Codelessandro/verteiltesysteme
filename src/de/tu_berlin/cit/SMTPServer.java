package de.tu_berlin.cit;

import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.channels.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;


public class SMTPServer {
	
	private static Charset msgCharset = Charset.forName("US-ASCII");
	private static CharsetDecoder decoder = msgCharset.newDecoder();
		
	public static void main(String[] args) throws IOException, CharacterCodingException,
			UnsupportedCharsetException {
		
		ServerSocketChannel server = ServerSocketChannel.open();
		Selector selector = Selector.open();
		server.configureBlocking(false);
		server.socket().bind(new InetSocketAddress(1234));
		server.register(selector, SelectionKey.OP_ACCEPT);
		
		System.out.println("Server runs on localhost");
		
		while (true) {
			
			if (selector.select() == 0) {
				System.out.println("select(): " + selector.select());
				continue;
			}
			
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				
				/*System.out.println("Server: " + key.toString());
				System.out.println("Server: " + key.readyOps());*/
				System.out.println("isWritable: " + key.isWritable());
				System.out.println("isReadable: " + key.isReadable());
				System.out.println("isAcceptable: " + key.isAcceptable());
				
				
				
				if (key.isAcceptable()) {
					System.out.println("Server: isAccaptable()");
					ServerSocketChannel sock = (ServerSocketChannel) key.channel();
					SocketChannel client = sock.accept();
					client.configureBlocking(false);
					SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					System.out.println("clientKey: " + clientKey);
					/*SMTPServerState state = new SMTPServerState();
					state.setMessage(new String("220-service ready").getBytes(msgCharset));
					clientKey.attach(state);*/
				}
				
				if (key.isReadable()) {
					System.err.println("Server: isReadable()");
					
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					SocketChannel clientChannel = (SocketChannel) key.channel();
					clientChannel.read(buffer);
					buffer.flip();
					System.out.println("ByteBuffer: " + buffer);
					CharBuffer charB = decoder.decode(buffer);
					String response = charB.toString();
					System.out.println("CharBuffer: " + response);
					System.out.println("substring: " + response.substring(0, 4));
					
					switch (response.substring(0, 4)) {
						case "HELO":
							System.err.println("Command: HELO");
							clientChannel.write(ByteBuffer.wrap(new String("250-ok\r\n").getBytes(msgCharset)));
							break;
						case "MAIL":
							System.err.println("Command: MAIL");
							clientChannel.write(ByteBuffer.wrap(new String("250-ok\r\n").getBytes(msgCharset)));
							break;
						case "DATA":
							System.err.println("Command: DATA");
							clientChannel.write(ByteBuffer.wrap(new String("250-ok\r\n").getBytes(msgCharset)));
							break;
						case "RCPT":
							System.err.println("Command: RCPT");
							clientChannel.write(ByteBuffer.wrap(new String("250-ok\r\n").getBytes(msgCharset)));
							break;
						case "QUIT":
							System.err.println("Command: QUIT");
							clientChannel.write(ByteBuffer.wrap(new String("250-ok\r\n").getBytes(msgCharset)));
							break;
						case "HELP":
							System.err.println("Command: HELP");
							clientChannel.write(ByteBuffer.wrap(new String("250-ok\r\n").getBytes(msgCharset)));
							break;
							
						
						default:
							System.out.println("wrong command");
					}
					
				}
				
				if (key.isWritable()) {
					System.out.println("Server: isWritable()");
					
					// write a response code to the client with a hyphen
					SocketChannel channel = (SocketChannel) key.channel();
					byte[] response = new String("220-service ready\r\n").getBytes(msgCharset);
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					buffer.put(response);
					buffer.flip();
					channel.write(buffer);
					
					
				}
				
				
				iter.remove();
			}
			
		}
		
		
	}
		
		
}
