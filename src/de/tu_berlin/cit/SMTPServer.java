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
		ByteBuffer buffer = ByteBuffer.allocate(1024);
//		ByteBuffer readBuf = ByteBuffer.allocate(1024);
		
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
				
				//System.out.println("Server toString(): " + key.toString());
				//System.out.println("Server readyOps(): " + key.readyOps());
				//System.out.println("Server interestOps(): " + key.interestOps());
				//System.out.println("isWritable: " + key.isWritable());
				//System.out.println("isReadable: " + key.isReadable());
				//System.out.println("isAcceptable: " + key.isAcceptable());
				
				
				
				if (key.isAcceptable()) {
					System.out.println("Server: isAccaptable()");
					
					ServerSocketChannel sock = (ServerSocketChannel) key.channel();
					SocketChannel client = sock.accept();
					client.configureBlocking(false);
					SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					
					SMTPServerState state = new SMTPServerState();
					clientKey.attach(state);
					
				}
				
				if (key.isWritable()) {
					System.err.println("Server: isWritable()");
					
					SMTPServerState state = (SMTPServerState) key.attachment();
					SocketChannel clientChannel = (SocketChannel) key.channel();
					
					buffer.clear();
					buffer.put(new byte[1024]);
					buffer.clear();
					
					int readBytes = clientChannel.read(buffer);
					buffer.flip();
					CharBuffer charB = decoder.decode(buffer);
					
					String response = charB.toString();
					System.out.println("CharBuffer: " + response);
					String resCode = (readBytes == 0) ? "": response.substring(0, 4);
					System.err.println("resCode: " + resCode);
					
					if (resCode.equals("HELP")) {
						System.err.println("Command: HELP");
						switch(state.getPreviousState()) {
							case SMTPClientState.CONNECTED:
								send(clientChannel, buffer, "214-help message\r\n");
								state.setState(SMTPClientState.RECEIVEDWELCOME);
								break;
							case SMTPClientState.RECEIVEDWELCOME:
								send(clientChannel, buffer, "214-help message\r\n");
								state.setState(SMTPClientState.MAILFROMSENT);
								break;
							case SMTPClientState.MAILFROMSENT:
								send(clientChannel, buffer, "214-help message\r\n");
								state.setState(SMTPClientState.RCPTTOSENT);
								break;
							case SMTPClientState.RCPTTOSENT:
								send(clientChannel, buffer, "214-help message\r\n");
								state.setState(SMTPClientState.DATASENT);
								break;
							case SMTPClientState.MESSAGESENT:
								send(clientChannel, buffer, "214-help message\r\n");
								state.setState(SMTPClientState.QUITSENT);
								break;
						}
					}
					
					switch (state.getState()) {
						case SMTPServerState.CONNECTED:
							if (resCode.equals("")) {
								System.err.println("NO COMMAND");
								send(clientChannel, buffer, "220-service ready\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.RECEIVEDWELCOME);
							} else {
								send(clientChannel, buffer, "220-service ready\r\n.\r\n");
								//debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.RECEIVEDWELCOME:
							if (resCode.equals("HELO")) {
								System.err.println("Command: HELO");
								send(clientChannel, buffer, "250-ok\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.MAILFROMSENT);
							} else {
								send(clientChannel, buffer, "220-service ready\r\n");
								//debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.MAILFROMSENT:
							if (resCode.equals("MAIL")) {
								System.err.println("Command: MAIL");
								send(clientChannel, buffer, "250-ok\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.RCPTTOSENT);
							} else {
								send(clientChannel, buffer, "250-ok\r\n");
								//debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.RCPTTOSENT:
							if (resCode.equals("RCPT")) {
								System.err.println("Command: RCPT");
								send(clientChannel, buffer, "250-ok\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.DATASENT);
							} else {
								send(clientChannel, buffer, "250-ok\r\n");
								//debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.DATASENT:
							if (resCode.equals("DATA")) {
								System.err.println("Command: DATA");
								send(clientChannel, buffer, "354-start with mail input\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.MESSAGESENT);
							} else {
								send(clientChannel, buffer, "250-ok\r\n");
								//debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.MESSAGESENT:
							if (resCode.equals("QUIT"))
								
							send(clientChannel, buffer, "250-ok\r\n");
							state.setPreviousState(state.getState());
							state.setState(SMTPServerState.QUITSENT);
							break;
						case SMTPServerState.QUITSENT:
							if (resCode.equals("QUIT")) {
								System.err.println("Command: QUIT");
								send(clientChannel, buffer, "221-service closing transmission channel\r\n");
								System.out.println("client wants to finish the connection");
								clientChannel.close();
							} else {
								send(clientChannel, buffer, "220-service ready\r\n");
								//debugAndExit(clientChannel, buffer, resCode);
							}
							break;
							
						default:
							System.err.println("wrong command");
					}					
					
				}	
				iter.remove();
			}	
		}
	}

	private static void debugAndExit(SocketChannel channel, ByteBuffer buffer, String responseCode) 
			throws IOException {
		buffer.position(0);
		System.out.println("buffer: " + decoder.decode(buffer));
		System.err.print("Unexpected response code " + responseCode + ", exiting...");
		channel.close();
		System.exit(1);
		
	}

	private static void send(SocketChannel channel, ByteBuffer buffer, String resMsg) throws IOException {
		buffer.clear();
		
		buffer.put(resMsg.getBytes(msgCharset));
		
		buffer.flip();
		
		channel.write(buffer);
		
		buffer.clear();
		
	}

	private static boolean readCommandLine(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
		socketChannel.read(buffer);
		CharBuffer cb = decoder.decode(buffer);
		String command = cb.toString().substring(0, 4);
		if (command.equals("") | command.equals("HELO") | command.equals("RCPT") 
				| command.equals("DATA") | command.equals("MAIL") |
						command.equals("HELP") | command.equals("QUIT") | command.equals("DATA")) {
							return true;
						}
		
		return false;
	}		
}