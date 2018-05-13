# verteiltesysteme

/*if(!readCommandLine(channel, state.getByteBuffer())) {
						System.out.println("found no command");
						continue;
					} 
					int response = channel.read(state.getByteBuffer());
					String responseCode = (response == 0) ? "" : decoder.decode(state.getByteBuffer())
							.toString().substring(0, 4);
					System.out.println("Received response code: " + responseCode);
					
					if(state.getState() == SMTPServerState.HELPSENT)
					{
						if(responseCode.equals("HELP"))
						{
							switch(state.getPreviousState()) {
							case SMTPServerState.CONNECTED:
								send(channel, state.getByteBuffer(), "220-service ready\r\n");
								state.setState(SMTPServerState.RECEIVEDWELCOME);
								break;
							case SMTPServerState.RECEIVEDWELCOME:
								send(channel, state.getByteBuffer(), "250-ok\r\n");
								state.setState(SMTPServerState.MAILFROMSENT);
								break;
							case SMTPServerState.MAILFROMSENT:
								send(channel, state.getByteBuffer(), "250-ok\r\n");
								state.setState(SMTPServerState.RCPTTOSENT);
								break;
							case SMTPServerState.RCPTTOSENT:
								send(channel, state.getByteBuffer(), "354-start mail input\r\n");
								state.setState(SMTPServerState.DATASENT);
								break;
							case SMTPServerState.MESSAGESENT:
								send(channel, state.getByteBuffer(), "214-help message\r\n");
								state.setState(SMTPServerState.QUITSENT);
								break;
							}
						}
						else
						{
							debugAndExit(channel, state.getByteBuffer(), responseCode);
						}
						
						continue;
					}
					
					if (responseCode.equals("")) {
						//send(channel, state.getByteBuffer(), "220-service ready\r\n");
						buffer.clear();
						System.err.println("NO COMMAND");
						byte[] initialResponse = new String("220-service ready\r\n").getBytes(msgCharset);
						buffer.put(initialResponse);
						buffer.flip();
						channel.write(buffer);
						buffer.clear();
						state.setState(SMTPServerState.RECEIVEDWELCOME);
					}
					
					switch(state.getState())
					{
					case SMTPServerState.CONNECTED:
						if(responseCode.equals("")) {
							send(channel, state.getByteBuffer(), "220-service ready\r\n");
							state.setState(SMTPServerState.RECEIVEDWELCOME);
						} else {
							debugAndExit(channel, state.getByteBuffer(), responseCode);
						}
						break;
					case SMTPServerState.RECEIVEDWELCOME:
						if(responseCode.equals("HELO")) {
							send(channel, state.getByteBuffer(), "250-ok\r\n");
							state.setState(SMTPServerState.MAILFROMSENT);
							
						} else {
							debugAndExit(channel, state.getByteBuffer(), responseCode);
						}
						break;
					case SMTPServerState.MAILFROMSENT:
						if(responseCode.equals("MAIL")) {
							send(channel, state.getByteBuffer(), "250-ok\r\n");
							state.setState(SMTPServerState.RCPTTOSENT);
						} else {
							debugAndExit(channel, state.getByteBuffer(), responseCode);
						}
						break;
					case SMTPServerState.RCPTTOSENT:
						if(responseCode.equals("RCPT")) {
							send(channel, state.getByteBuffer(), "250-ok\r\n");
							state.setState(SMTPServerState.DATASENT);
						} else {
							debugAndExit(channel, state.getByteBuffer(), responseCode);
						}
						break;
					case SMTPServerState.DATASENT:
						if(responseCode.equals("DATA")) {
							send(channel, state.getByteBuffer(), "354-start mail input\r\n");
							state.setState(SMTPServerState.MESSAGESENT);
						} else {
							debugAndExit(channel, state.getByteBuffer(), responseCode);
						}
						break;
					case SMTPServerState.MESSAGESENT:
						if(responseCode.equals("DATA")) {
							send(channel, state.getByteBuffer(), "250-ok\r\n");
							state.setState(SMTPServerState.QUITSENT);
						} else {
							debugAndExit(channel, state.getByteBuffer(), responseCode);
						}
						break;
					case SMTPServerState.QUITSENT:
						if(responseCode.equals("QUIT")) {
							if(!key.isValid())
								System.out.println("Connection closed by server");
								key.cancel();
								key.channel().close();
								System.exit(0);
						} else {
							debugAndExit(channel, state.getByteBuffer(), responseCode);	
						}
						break;	
					}*/
