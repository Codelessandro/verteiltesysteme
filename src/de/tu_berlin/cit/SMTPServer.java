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
					System.out.println("Server: isReadable()");
					
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					SocketChannel clientChannel = (SocketChannel) key.channel();
					clientChannel.read(buffer);
					buffer.flip();
					System.out.println("Buffer: " + buffer);
					
					/*String result = new String(buffer.array()).trim();
					
					if (result.endsWith("\n\r.\n\r")) {
						clientChannel.close();
					}*/
				}
				
				if (key.isWritable()) {
					System.out.println("Server: isWritable()");
					
					// write a response code to the client with a hyphen
					/*SocketChannel client = (SocketChannel) key.channel();
					byte[] hyphen = new String("250-first line").getBytes(msgCharset);
					ByteBuffer buffer = ByteBuffer.wrap(hyphen);
					client.write(buffer);*/
					
					// retrieve the state object from the channel
					/*SocketChannel channel = (SocketChannel) key.channel();
					SMTPClientState state = (SMTPClientState) key.attachment();
					System.out.println("client state: " + state);*/
					
					// attach a new server state object to the channel
					SMTPServerState state = new SMTPServerState();
					state.setMessage(new String("220-service ready").getBytes(msgCharset));
					key.attach(state);
					
				}
				
				/*if (key.isWritable()) {
					System.out.println("Server: isWritable()");
					
					byte responseCode = (byte) 250;
					
					ByteBuffer bb = ByteBuffer.allocate(10);
					bb.put(responseCode);
					
					SocketChannel client = (SocketChannel) key.channel();
					key.attachment();
					client.write(bb);
					
					/*Charset msgCharset = Charset.forName("US-ASCII");
					CharsetDecoder decoder = msgCharset.newDecoder(); 
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					SocketChannel channel = (SocketChannel) key.channel();
					//SMTPClientState state = key.attachment();
					channel.read(buffer);
					System.out.println("capacity: " + buffer.capacity());
					System.out.println("buffer: " + buffer);
					CharBuffer charBuf = decoder.decode(buffer);
					System.out.println("capacity: " + charBuf.capacity());
					System.out.println("position: " + charBuf.position());
					System.out.println("limit: " + charBuf.limit());
					//System.out.println(charBuf);
					
					//System.out.println("str: " + res);
					
					
					//Path dir = Files.createDirectory(Paths.get("sender"));
					//File file = Files.createFile(Paths.get("./")).toFile();
					//System.out.println(file);
					
					System.out.println("print File: ");
					FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.WRITE);
					fileChannel.write(buffer);
				}*/
				
				iter.remove();
			}
			
		}
		
		
	}
		
		
}
