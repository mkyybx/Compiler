package compiler;

import java.io.*;
import java.util.*;

class error {
	private int line;
	private int col;
	private String message;
	
	error(int line, int col, String message) {
		this.line = line;
		this.col = col;
		this.message = message;
	}
	
	public String toString() {
		return line + "\t" + col + "\t" + message;
	}
}

public class Vocabulary {
	static HashMap<String,Integer> identifiers = new HashMap<String, Integer>();
	static DataInputStream in;
	static byte temp;
	static StringBuilder buffer;
	static boolean preCompileMode = false;
	static boolean is0x0DRead = false;
	static boolean throwedEOF = false;
	static final String[][] keyword = {{"_Bool", "_Complex", "_Imaginary"},{},{"auto"}, {"break"}, {"case", "char", "const", "continue"}, {"default", "do", "double"}, {"else", "enum", "extern"}, {"float", "for"}, {"goto"}, {}, {"if", "inline", "int"}, {}, {}, {"long"}, {}, {}, {}, {}, {}, {"register", "restrict", "return"}, {"short", "signed", "sizeof", "static", "struct", "switch"}, {"typedef"}, {"union", "unsigned"}, {"void", "volatile"}, {"while"},{},{},{}};
	static final String[] punc1 = {"[","]","(",")","{","}",".","&","*","+","-","~","!","/","%","<",">","^","|","?",":",";","=",",","#"};
	static final String[] punc2 = {"->","++","--","<<",">>","<=",">=","==","!=","&&","||","*=","/=","%=","+=","-=","&=","^=","|=","##","<:",":>","<%","%>","%:"};
	static final String[] punc3 = {"...","<<=",">>=","%:%"};
	static final String[] punc4 = {"%:%:"};
	static int line = 1;
	static int col = 1;
	static int state = 0;
	static int[] word = new int[128];
	static int total = 0;
	static ArrayList<error> errors = new ArrayList<error>();
	public static void exit() {
		System.out.println("\n\nStatistics:");
		System.out.println("Lines:" + line);
		System.out.println("keyword:" + word['k'] + "\n" +
							"identifier:" + word['i'] + "\n" +
							"string-literal:" + word['s'] + "\n" +
							"punctuator:(excluding spaces and new-line chars):" + word['p'] + "\n" +
							"headername:" + word['h'] + "\n" +
							"decimal floating constant:" + word['d'] + '\n' +
							"heximal floating constant:" + word['x'] + '\n' +
							"decial integer constant:" + word['z'] + '\n' + 
							"octa integer constant:" + word['o'] + '\n' +
							"hex integer constant:" + word['0'] + '\n' +
							"char constant:" + word['r'] + '\n' + 
							"annotation:" + word['a']
							);
		System.out.println("Total characters:" + total);
		System.out.println("Errors:(" + errors.size() + ")\nLine\tCol\tDetail");
		for (int i = 0; i < errors.size(); i++)
			System.out.println(errors.get(i).toString());
		System.exit(0);
	}
	public static void checkSpaceAndEnter() {
		try {
			boolean is0x0DRead = false;
			while (true) {
				if (temp != 0x0D && temp != 0x0A && temp != ' ' && temp != 0x09) {
					is0x0DRead = false;
					break;
				}
				else if (temp == ' ') {
					System.out.print(' ');
					is0x0DRead = false;
					read();
				}
				else if (temp == 0x09) {
					System.out.print('\t');
					is0x0DRead = false;
					read();
				}
				else if (temp == 0x0D) {
					is0x0DRead = true;
					System.out.println();
					preCompileMode = false;
					read();
				}
				else if (temp == 0x0A && is0x0DRead) {
					is0x0DRead = false;
					read();
				}
				else if (temp == 0x0A && !is0x0DRead) {
					is0x0DRead = false;
					break;
				}
			}
		} catch(IOException ex) {
			exit();
		}
	}
	public static void read() throws IOException {
		try {
			temp = in.readByte();
			total++;
		} catch (IOException ex) {
			if (throwedEOF)
				exit();
			else {
				throwedEOF = true;
				throw ex;
			}
		}
		if (temp != 0x0D && temp != 0x0A) {
			is0x0DRead = false;
			col++;
		}
		else if (temp == 0x0D) {
			is0x0DRead = true;
			line++;
			col = 1;
		}
		else if ((temp == 0x0A && is0x0DRead))
			is0x0DRead = false;
	}
	public static void print(String temp, char type) {
		/*
		 * k:keyword
		 * i:identifier
		 * s:string-literal
		 * p:punctuator
		 * h:headername
		 * d:decimal floating constant
		 * x:heximal floating constant
		 * z:decial zhengshu constant
		 * o:octa integer constant
		 * 0:hex integer constant
		 * r:char constant
		 * a:annotation
		 * */
		System.out.print(temp);
		word[type]++;
		switch (type) {
		case 'k':
			System.out.print("(" + "K:" + temp + ")");
			break;
		case 'i':
			Integer num = identifiers.get(temp);
			if (num == null)
				identifiers.put(temp, num = identifiers.size());
			System.out.print("(I:" + num + ")");
			break;
		case 'z':
			System.out.print("(DIC)");
			break;
		case 'o':
			System.out.print("(OIC)");
			break;
		case 'd':
			System.out.print("(DFC)");
			break;
		case 'x':
			System.out.print("(HFC)");
			break;
		case '0':
			System.out.print("(HIC)");
			break;
		case 'p':
			System.out.print("(" + "P:" + temp + ")");
			break;
		case 'r':
			System.out.print("(CC)");
			break;
		case 's':
			System.out.print("(S)");
			break;
		case 'h':
			System.out.print("(HN)");
			break;
		case 'a':
			System.out.print("(A)");
			break;
		default:
			System.out.print("WHAT!");
		}
	}
	//temp is used buffer isn't filled, buffer has 2 bytes
	public static void checkAnnotation() {
		// //
		buffer.append((char)temp);
		if (temp == '/') {
			while (true) {
				try {
					read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					print(buffer.toString(),'a');;
				}
				//end
				if (temp == 0x0D || temp == 0x0A) {
					print(buffer.toString(),'a');
					break;
				}
				else buffer.append((char)temp);
			}
		}
		// /**/
		else if (temp == '*') {
			boolean waitForSlash = false;
			while (true) {
				try {
					read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					print(buffer.toString(),'a');
					errors.add(new error(line,col,"(notation uncompleted)"));
				}
				buffer.append((char)temp);
				if (temp == '*' && !waitForSlash)
					waitForSlash = true;
				else if (temp == '/' && waitForSlash) {
					try {
						read();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						print(buffer.toString(),'a');
					}
					print(buffer.toString(),'a');
					break;
				}
				else if (waitForSlash)
					waitForSlash = false;
			}
		}
	}
	//temp is used buffer isn't filled
	public static void checkHeaders() {
		buffer.append((char)temp);
		boolean isH = (temp == '<');
		boolean init = true;
		while (true) {
			try {
				read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				print(buffer.toString(), 'h');
				errors.add(new error(line,col,"(header uncompleted)"));
			}
			buffer.append((char)temp);
			//end
			if ((temp == '>' && isH) || (temp == '\"' && !isH)) {
				//non-empty
				if (init)
					errors.add(new error(line,col,"(no chars between < and > or \" and \")"));
				try {
					read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					print(buffer.toString(), 'h');
				}
				print(buffer.toString(), 'h');
				break;
			}
			//next line
			else if (temp == 0x0D) {
				//error
				errors.add(new error(line,col,"(find new-line chars)"));
				temp = ' ';
				buffer.append((char)temp);
			}
			init = false;
		}
		preCompileMode = false;
	}
	//temp is used buffer isn't filled
	public static void checkCharAndString() throws IOException {
		buffer.append((char)temp);
		boolean isChar = (temp == '\'');
		boolean init = true;
		boolean escapeMode = false;
		
		while (true) {
			try {
				read();
			} catch (IOException ex) {
				print(buffer.toString(),'s');
				errors.add(new error(line,col,"(string or char uncompleted)"));
			}
			buffer.append((char)temp);
			//end char
			if (temp == '\'' && isChar) {
				if (init) {
					//error
					errors.add(new error(line,col,"(no chars between ' and ')"));
					break;
				}
				else if (!escapeMode){
					print(buffer.toString(),'r');
					read();
					break;
				}
				else if (escapeMode)
					escapeMode = false;
			}
			else if (temp == '\"' && !isChar) {
				if (!escapeMode) {
					print(buffer.toString(),'s');
					read();
					break;
				}
				else escapeMode = false;
			}
			//next line
			else if (temp == 0x0D) {
				//error
				errors.add(new error(line,col,"(find new-line chars)"));
				temp = ' ';
				buffer.append((char)temp);
				escapeMode = false;
			}
			else if (temp == '\\') {
				if (!escapeMode)
					escapeMode = true;
				else escapeMode = false;
			}
			else escapeMode = false;
			init = false;
		}
	}
	//temp is used buffer isn't filled
	public static void checkIntegerSuffix() throws IOException {
		if (temp == 'u' || temp == 'U') {
			buffer.append((char)temp);
			read();
			if (temp == 'l' || temp == 'L') {
				buffer.append((char)temp);
				read();
				if (temp == 'l' || temp == 'L') {
					buffer.append((char)temp);
					read();
					return;
				}
				else return;
			}
			else return;
		}
		else if (temp == 'l' || temp == 'L') {
			buffer.append((char)temp);
			read();
			if (temp == 'l' || temp == 'L') {
				buffer.append((char)temp);
				read();
				if (temp == 'u' || temp == 'U') {
					buffer.append((char)temp);
					read();
				}
				else return;
			}
			else if (temp == 'u' || temp == 'U') {
				buffer.append((char)temp);
				read();
				return;
			}
			else return;
		}
		else return;
	}
	//temp is used buffer isn't filled
	public static void checkExponentialPart() throws IOException {
		try {
			if (temp == 'e' || temp == 'E') {
				buffer.append((char)temp);
				read();
				if (temp == '+' || temp == '-') {
					buffer.append((char)temp);
					read();
				}
				else if (temp >= '0' && temp <= '9')
					;
				else {
					//error
					errors.add(new error(line,col,"(" + temp + " may be + or - ?)"));
					temp = '+';
					buffer.append((char)temp);
					read();
				}
				if (checkDigitalSequence()) {
					//error
					errors.add(new error(line,col,"(no number after e or E?)"));
				}
			}
			else return;
		} catch (IOException ex) {
			System.out.print("no number after exponential part");
			throw ex;
		}
	}
	public static void checkExponentialPartBinary() throws IOException {
		try {
			if (temp == 'p' || temp == 'P') {
				buffer.append((char)temp);
				read();
				if (temp == '+' || temp == '-') {
					buffer.append((char)temp);
					read();
				}
				else if (temp >= '0' && temp <= '9')
					;
				else {
					//error
					errors.add(new error(line,col,"(" + temp + " may be + or - ?)"));
					temp = '+';
					buffer.append((char)temp);
					read();
				}
				if (checkDigitalSequence()) {
					//error
					errors.add(new error(line,col,"(no number after p or P?)"));
				}
			}
			else return;
		} catch (IOException ex) {
			System.out.print("no number after exponential part");
			throw ex;
		}
	}
	//temp is used buffer isn't filled
	public static boolean checkDigitalSequence() throws IOException {
		boolean init = true;
		while (true) {
			if (temp >= '0' && temp <= '9') {
				buffer.append((char)temp);
				init = false;
			}
			else return init;
			read();
		}
	}
	public static boolean checkDigitalSequenceHeximal() throws IOException {
		boolean init = true;
		while (true) {
			if ((temp >= '0' && temp <= '9') || (temp <= 'f' && temp >= 'a') || (temp <= 'F' && temp >= 'A')) {
				buffer.append((char)temp);
				init = false;
			}
			else return init;
			read();
		}
	}
	public static void checkWord() throws IOException {
		while (true) {
			//continue check
			if ((temp >= 'A' && temp <= 'Z') || (temp >= 'a' && temp <= 'z') || (temp >= '0' && temp <= '9') || temp == '_') {
				buffer.append((char)temp);
				read();
				continue;
			}
			//end of a word
			else {
				String temps = buffer.toString();
				if (temps.charAt(0) - '_' <= keyword.length && temps.charAt(0) - '_' >= 0) {
					for (int i = 0; i < keyword[temps.charAt(0) - '_'].length; i++) {
						//if it's a keyword
						if (temps.equals(keyword[temps.charAt(0) - '_'][i])) {
							print(temps,'k');
							break;
						}
						//not a keyword
						else if (i == keyword[temps.charAt(0) - '_'].length - 1) {
							print(temps,'i');
						}
					}
					if (keyword[temps.charAt(0) - '_'].length == 0)
						print(temps,'i');
				}
				//not a keyword for not matching the capital letter
				else print(temps,'i');
				break;
			}
		}
	
	}
	public static void main(String args[]) throws FileNotFoundException {
		in = new DataInputStream(new FileInputStream("in.c"));
		//linked with char *
		System.out.println("K:keyword\nI:identifier\nDIC:decimal integer constant\nOIC:octa integer constant\nDFC:decimal floating constant\nHFC:hex floating constant");
		System.out.println("HIC:hex integer constant\nP:punctuator\nCC:char constant\nS:string-literals\nHN:header name\nA:annotation\n");
		try {
			read();
		} catch (IOException ex) {
			exit();
		}
			while (true) {
				checkSpaceAndEnter();
				buffer = new StringBuilder();
				//keyword, identifier or enumeration
				if (temp == '_' || (temp >= 'A' && temp <= 'Z') || (temp >= 'a' && temp <= 'z')) {
				try {
					buffer.append((char)temp);
					//may be a char or string
					if (temp == 'L') {
						//a char or string
						read();
						if (temp == '\'' || temp == '\"')
							try {
								checkCharAndString();
							} catch (IOException ex) {
								if (temp == '\'')
									print(buffer.toString(),'c');
								else print(buffer.toString(), 's');
							}
						//not a char or string
						else checkWord();
					}
					else {
						read();
						checkWord();
					}
				} catch (IOException ex) {
					String temps = buffer.toString();
					if (temps.charAt(0) - '_' <= keyword.length && temps.charAt(0) - '_' >= 0) {
						for (int i = 0; i < keyword[temps.charAt(0) - '_'].length; i++) {
							//if it's a keyword
							if (temps.equals(keyword[temps.charAt(0) - '_'][i])) {
								print(temps,'k');
								break;
							}
							//not a keyword
							else if (i == keyword[temps.charAt(0) - '_'].length - 1) {
								print(temps,'i');
							}
						}
						if (keyword[temps.charAt(0) - '_'].length == 0)
							print(temps,'i');
					}
					//not a keyword for not matching the capital letter
					else print(temps,'i');
				}
				}
				
				//decimal floating constant or punctuator
				else if (temp == '.') {
				try {
					buffer.append((char)temp);
					read();
					//number, for variable starts with letter or '_'
					if (temp <= '9' && temp >= '0') {
						try {
							checkDigitalSequence();
							checkExponentialPart();
							if (temp == 'f' || temp == 'F' || temp == 'l' || temp == 'L')
								read();
							print(buffer.toString(),'d');
						} catch (IOException ex) {
							print(buffer.toString(),'d');
						}
					}
					//punctuator ...
					else if (temp == '.'){
						buffer.append((char)temp);
						read();
						if (temp == '.') {
							print(buffer.toString(),'p');
						}
					}
					//punctuator .
					else {
						print(buffer.toString(),'p');
					}
				} catch (IOException ex) {
					print(buffer.toString(),'p');
				}
				}
				
				//integer constant, floating constant
				else if (temp >= '0' && temp <= '9') {
					buffer.append((char)temp);
					//octa integer or hex integer or float
					if (temp == '0') {
							try {
								read();
							} catch (IOException ex) {
								print(buffer.toString(),'o');
							}
							//hex integer or float
							if (temp == 'x' || temp == 'X') {
								buffer.append((char)temp);
								while (true) {
									try {
										read();
									} catch (IOException ex) {
										errors.add(new error(line,col,"(missing number after 0x)"));
										print(buffer.toString(), 'x');
									}
									if ((temp >= '0' && temp <= '9') || (temp <= 'f' && temp >= 'a') || (temp <= 'F' && temp >= 'A'))
										buffer.append((char)temp);
									//hex float
									else if (temp == '.' || temp == 'p' || temp == 'P') {
										buffer.append((char)temp);
										try {
											if (temp == '.') {
												read();
												checkDigitalSequenceHeximal();
											}
											checkExponentialPartBinary();
											//suffix
											if (temp == 'f' || temp == 'F' || temp == 'l' || temp == 'L')
												read();
											print(buffer.toString(),'x');
										} catch (IOException ex) {
											print(buffer.toString(),'x');
										}
										break;
									}
									//hex integer
									else {
										try {
											checkIntegerSuffix();
										} catch (IOException ex) {
											print(buffer.toString(),'0');
										}
										print(buffer.toString(),'0');
										break;
									}
								}
							
							}
						
						//octa integer or float
						else {
							while (true) {
								if (temp >= '0' && temp < '8')
									buffer.append((char)temp);
								//decimal float
								else if (temp == '.' || temp == 'e' || temp == 'E') {
									try {
										buffer.append((char)temp);
										if (temp == '.') {
											read();
											checkDigitalSequence();
										}
										checkExponentialPart();
										//suffix
										if (temp == 'f' || temp == 'F' || temp == 'l' || temp == 'L')
											read();
										print(buffer.toString(),'d');
										break;
									} catch (IOException ex) {
										print(buffer.toString(),'d');
									}
								}
								//octa integer
								else {
									try {
										checkIntegerSuffix();
										print(buffer.toString(),'o');
									}
									catch (IOException ex) {
										print(buffer.toString(),'o');
									}
									break;
								}
								try {
									read();
								} catch (IOException ex) {
									print(buffer.toString(),'d');
								}
							}
						}
					}
					//decimal integer or float
					else if (temp > '0' && temp <= '9') {
						while (true) {
							try {
								read();
								if (temp >= '0' && temp <= '9')
									buffer.append((char)temp);
								//float
								else if (temp == '.' || temp == 'e' || temp == 'E') {
									try {
										buffer.append((char)temp);
										read();
										if (temp == '.') {
											read();
											checkDigitalSequence();
										}
										checkExponentialPart();
										
										//suffix
										if (temp == 'f' || temp == 'F' || temp == 'l' || temp == 'L')
											read();
										print(buffer.toString(),'d');
									} catch (IOException ex) {
										print(buffer.toString(),'d');
									}
									break;
								}
								//suffix
								else {
									checkIntegerSuffix();
									print(buffer.toString(),'z');
									break;
								}
							} catch (IOException ex) {
								print(buffer.toString(),'z');
							}
						}
					}
				}
				
				//character constant
				else if (temp == '\'') {
					try {
						checkCharAndString();
					} catch (Exception ex) {
						;
					}
				}
				
				//string-literal or header name
				else if (temp == '\"') {
					if (!preCompileMode)
						try {
							checkCharAndString();
						} catch (Exception ex) {
							;
						}
					else checkHeaders();
				}
				
				//header name or punctuator
				else if (temp == '<') {
					if (!preCompileMode) {
						try {
							buffer.append((char)temp);
							read();
							//<= <: <%
							if (temp == '%' || temp == '=' || temp == ':') {
								buffer.append((char)temp);
								read();
							}
							//<< <<=
							else if (temp == '<') {
								buffer.append((char)temp);
								read();
								if (temp == '=') {
									buffer.append((char)temp);
									read();
								}
							}
							print(buffer.toString(), 'p');
						} catch (IOException ex) {
							print(buffer.toString(), 'p');
						}
					}
					else checkHeaders();
				}
				
				//annotation or punctuator
				else if (temp == '/') {
					try {
						buffer.append((char)temp);
						read();
						//annotation
						if (temp == '/' || temp == '*')
							checkAnnotation();
						//  /=
						else if (temp == '=') {
							buffer.append((char)temp);
							read();
							print(buffer.toString(),'p');
						}
						else print(buffer.toString(),'p');
					} catch (IOException ex) {
						print(buffer.toString(),'p');
					}
				}
				
				//punctuator
				else {
					try {
						buffer.append((char)temp);
						boolean found = false;
						for (int i = 0; i < punc1.length; i++) {
							if (buffer.toString().equals(punc1[i])) {
								read();
								buffer.append((char)temp);
								for (int j = 0; j < punc2.length; j++) {
									if (buffer.toString().equals(punc2[j])) {
										read();
										buffer.append((char)temp);
										for (int k = 0; k < punc3.length; k++) {
											if (buffer.toString().equals(punc3[k])) {
												read();
												buffer.append((char)temp);
												for (int l = 0; l < punc4.length; l++) {
													if (buffer.toString().equals(punc4[l])) {
														read();
														buffer.append((char)temp);
														print(buffer.toString(),'p');
														found = true;
														read();
													}
													else if (found)
														break;
													else if (l == punc4.length - 1) {
														print(buffer.deleteCharAt(3).toString(),'p');
														found = true;
														break;
													}
												}
											}
											else if (found)
												break;
											else if (k == punc3.length - 1) {
												print(buffer.deleteCharAt(2).toString(),'p');
												found = true;
												break;
											}
										}
									}
									else if (found)
										break;
									else if (j == punc2.length - 1) {
										print(buffer.deleteCharAt(1).toString(),'p');
										found = true;
										if (buffer.charAt(0) == '#')
											preCompileMode = true;
										break;
									}
								}
							}
							else if (found)
								break;
							else if (i == punc1.length - 1) {
								//error
								if (!found) {
									errors.add(new error(line,col,"(ASCII:" + (int)temp + " cannot be recogonized!)"));
									read();
								}
								break;
							}
						}
					} catch (IOException ex) {
						print(buffer.toString(),'p');
					}
				}
				
			}
	}
}
