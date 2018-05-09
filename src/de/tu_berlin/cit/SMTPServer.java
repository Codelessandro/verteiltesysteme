package de.tu_berlin.cit;

import SMTPClientState;
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

	
		
//		if (args.length != 2) {
//			System.out.println("to less arguments");
//			System.exit(1);
//		}
		
//		String str = "Hello";
//		byte[] bytes = str.getBytes();
//		for (byte b: bytes) {
//			System.out.println(b);
//		}
		
		// String -> bytes
/*		String s = "Verteilte Systeme";
		Charset msgCharset = Charset.forName("US-ASCII");
		byte[] b = s.getBytes(msgCharset);
		ByteBuffer bb = ByteBuffer.allocate(17);
		bb.put(b);
		ByteBuffer bb = ByteBuffer.wrap(b);
		System.out.println("encoded: " + bb);
		
		// bytes -> String
		CharsetDecoder decoder = msgCharset.newDecoder();
		CharBuffer charBuf = decoder.decode(bb);
		String decStr = charBuf.toString();
		System.out.println("decoded: " + decStr);
		
		// Selector for selecting channel registration
		Selector selector = Selector.open();
		ServerSocketChannel servSock = ServerSocketChannel.open();
		servSock.configureBlocking(false);
		servSock.socket().bind(new InetSocketAddress(1234));
		servSock.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true) {
			if(selector.select() == 0) {
				continue;
			}
			
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			
			while(iter.hasNext()) {
				SelectionKey key = iter.next();
				// check whether the key of the channel accepts new socket connection
				if(key.isAcceptable()) {
					ServerSocketChannel sock = (ServerSocketChannel) key.channel();
					SocketChannel client = sock.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}
				if(key.isReadable()) {
					ByteBuffer buf = ByteBuffer.allocate(1024);
					SocketChannel channel = (SocketChannel) key.channel();
					channel.read(buf);
					buf.flip();
				}
				
				if(key.isConnectable()) {
					
				}
				
				iter.remove();
			}
		}
		
		
		
		
		
		
		
		
//		InetSocketAddress address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
//		Socket server = null;
//		try {
//			server = new Socket(address.getHostName(), address.getPort());
//		} catch (UnknownHostException e) {
//			System.err.println("Host does not exist");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.err.println("Problem during IO operation");
//			e.printStackTrace();
//		}
	*/
		
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
				continue;
			}
			
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				
				/*System.out.println("Server: " + key.toString());*/
				//System.out.println("Server: " + key.readyOps());
				System.out.println("isWritable: " + key.isWritable());
				System.out.println("isReadable: " + key.isReadable());
				
				if (key.isAcceptable()) {
					System.out.println("Server: isAccaptable()");
					ServerSocketChannel sock = (ServerSocketChannel) key.channel();
					SocketChannel client = sock.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}
				
				if (key.isReadable()) {
					System.out.println("Server: isReadable()");
					ByteBuffer bb = ByteBuffer.allocate(1024);
					SocketChannel channel = (SocketChannel) key.channel();
					channel.read(bb);
					bb.flip();
					System.out.println("Buffer: " + bb);
				}
				
				if (key.isWritable()) {
					System.out.println("Server: isWritable()");
					
					Charset msgCharset = Charset.forName("US-ASCII");
					CharsetDecoder decoder = msgCharset.newDecoder(); 
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					SocketChannel channel = (SocketChannel) key.channel();
					SMTPClientState state = key.attachment();
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
					
					/*System.out.println("print File: ");
					FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.WRITE);
					fileChannel.write(buffer);*/
				}
				
				iter.remove();
			}
			
		}
		
		
	}
		
		
}
