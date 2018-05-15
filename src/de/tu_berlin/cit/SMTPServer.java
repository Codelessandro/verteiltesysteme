package de.tu_berlin.cit;

import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.channels.*;
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
		boolean serviceReady = false;
		boolean exists = Files.exists(Paths.get("mails"));
		
		if (!exists) {
			Path root = Files.createDirectory(Paths.get("mails"));
		}
			
		Path dir = null;
		Path file = null;
		FileChannel fc = null;
		String mailFrom = "";
		String rcptTo = "";
		
		System.out.println("Server runs on localhost");
		
		while (true) {
			
			if (selector.select() == 0) {
				continue;
			}
			
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				
				if (key.isAcceptable()) {
					
					ServerSocketChannel sock = (ServerSocketChannel) key.channel();
					SocketChannel client = sock.accept();
					client.configureBlocking(false);
					SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					
					SMTPServerState state = new SMTPServerState();
					clientKey.attach(state);
					
				}
				
				if (key.isWritable()) {
					
					SMTPServerState state = (SMTPServerState) key.attachment();
					SocketChannel clientChannel = (SocketChannel) key.channel();
					
					buffer.clear();
					buffer.put(new byte[1024]);
					buffer.clear();
					
					int readBytes = clientChannel.read(buffer);
					
					if (readBytes == 0 && serviceReady) {
						continue;
					}
					
					buffer.flip();
					CharBuffer charB = decoder.decode(buffer);
					
					String response = charB.toString();
					System.out.println("CharBuffer: " + response);
					
					if (response.length() <= 4 & serviceReady) {
						System.err.println("Text after command was empty");
						System.exit(1);
					}
					
					String resCode = (readBytes == 0) ? "": response.substring(0, 4);
					System.out.println("resCode: " + resCode);
					
					if (resCode.equals("HELP")) {
						System.out.println("Command: HELP");
						
						switch(state.getPreviousState()) {
							case SMTPServerState.CONNECTED:
								send(clientChannel, buffer, "214-please send HELO\r\n.\r\n");
								state.setState(SMTPServerState.RECEIVEDWELCOME);
								break;
							case SMTPServerState.RECEIVEDWELCOME:
								send(clientChannel, buffer, "214-please send MAIL FROM\r\n.\r\n");
								state.setState(SMTPServerState.MAILFROMSENT);
								break;
							case SMTPServerState.MAILFROMSENT:
								send(clientChannel, buffer, "214-please send RCPT TO\r\n.\r\n");
								state.setState(SMTPServerState.RCPTTOSENT);
								break;
							case SMTPServerState.RCPTTOSENT:
								send(clientChannel, buffer, "214-please send DATA\r\n.\r\n");
								state.setState(SMTPServerState.DATASENT);
								break;
							case SMTPServerState.MESSAGESENT:
								send(clientChannel, buffer, "214-please send QUIT\r\n.\r\n");
								state.setState(SMTPServerState.QUITSENT);
								break;
						}
						continue;
					}
					
					switch (state.getState()) {
						case SMTPServerState.CONNECTED:
							if (resCode.equals("")) {
								System.out.println("NO COMMAND");
								send(clientChannel, buffer, "220-service ready\r\n.\r\n");
								serviceReady = true;
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.RECEIVEDWELCOME);
							} else {
								debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.RECEIVEDWELCOME:
							if (resCode.equals("HELO")) {
								System.out.println("Command: HELO");
								send(clientChannel, buffer, "250-ok\r\n.\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.MAILFROMSENT);
							} else {
								debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.MAILFROMSENT:
							if (resCode.equals("MAIL")) {
								System.out.println("Command: MAIL");
								send(clientChannel, buffer, "250-ok\r\n.\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.RCPTTOSENT);
								
								mailFrom = response.substring(0, response.length()-2);
							} else {
								debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.RCPTTOSENT:
							if (resCode.equals("RCPT")) {
								System.out.println("Command: RCPT");
								send(clientChannel, buffer, "250-ok\r\n.\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.DATASENT);
								
								rcptTo = response.substring(0, response.length()-2);
							} else {
								debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.DATASENT:
							if (resCode.equals("DATA")) {
								System.out.println("Command: DATA");
								send(clientChannel, buffer, "354-start with mail input\r\n.\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.MESSAGESENT);
							} else {
								debugAndExit(clientChannel, buffer, resCode);
							}
							break;
						case SMTPServerState.MESSAGESENT:
							if (resCode.matches("[a-z| ' ']{4}")) {
								send(clientChannel, buffer, "250-ok\r\n.\r\n");
								state.setPreviousState(state.getState());
								state.setState(SMTPServerState.QUITSENT);
								
								int msgID = (int)(Math.random() * 1000);
								
								if (!Files.exists(Paths.get("mails/" + rcptTo))) {
									dir = Files.createDirectory(Paths.get("mails/" + rcptTo));
								} else {
									System.out.println("directory already exists!");
								}
								
								file = Files.createFile(
										Paths.get("mails/" + rcptTo + "/" +  mailFrom + "_" + msgID));
								fc = FileChannel.open(Paths.get("mails/" + rcptTo + "/" +  mailFrom + "_" + msgID), 
										StandardOpenOption.WRITE);
								fc.write(ByteBuffer.wrap(response.getBytes(msgCharset)));
							} else {
								debugAndExit(clientChannel, buffer, resCode);
							}
							break;
							
							
						case SMTPServerState.QUITSENT:
							if (resCode.equals("QUIT")) {
								System.out.println("Command: QUIT");
								send(clientChannel, buffer, "221-service closing transmission channel\r\n.\r\n");
								System.out.println("client wants to finish the connection");
								key.cancel();
								key.channel().close();
							} else {
								debugAndExit(clientChannel, buffer, resCode);
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
}