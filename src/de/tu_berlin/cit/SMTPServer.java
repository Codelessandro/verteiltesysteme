package de.tu_berlin.cit;

import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.channels.*;
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
	
	private static Charset msgCharset = Charset.forName("US-ASCII");
	private static CharsetDecoder decoder = msgCharset.newDecoder();
		
	public static void main(String[] args) throws IOException, CharacterCodingException,
			UnsupportedCharsetException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		Selector selector = Selector.open();
		InetSocketAddress remoteAddress = new InetSocketAddress(args[0], 
						Integer.parseInt(args[1]));
		System.out.println("Server runs on: " + remoteAddress.getHostName() 
				+ " on port: " + remoteAddress.getPort());
		
		serverChannel.configureBlocking(false);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while (true) {
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			
			if (iter.hasNext()) {
				SelectionKey key = iter.next();
				
				if (key.isAcceptable()) {
					Path dir = Files.createDirectory(Paths.get("sender"));
					Path file = Files.createFile(Paths.get("/"));
					SocketChannel clientChannel = (SocketChannel) key.channel();
					ByteBuffer bb = ByteBuffer.allocate(1024);
					clientChannel.read(bb);
					FileChannel fileChannel = FileChannel.open(file);
					fileChannel.write(bb);
					
				}
			}
			
		}
		
		
	}
		
		
}
