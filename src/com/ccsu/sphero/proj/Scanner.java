package com.ccsu.sphero.proj;

import java.io.*;

public class Scanner{
  private char currentChar;
  private byte currentKind;
  private StringBuffer currentSpelling;
  private BufferedReader inFile;
  private static int line = 1;

  public Scanner(BufferedReader inFile){
    this.inFile = inFile;
    try{
      int i = this.inFile.read();
      if(i == -1) //end of file
        currentChar = '\u0000';
      else
        currentChar = (char)i;
    }
    catch(IOException e){
        new Error(e.toString(),-1);
    }
  }

  private void takeIt(){
    currentSpelling.append(currentChar);
    try{
      int i = inFile.read();
      if(i == -1) //end of file
        currentChar = '\u0000';
      else
        currentChar = (char)i;
    }
    catch(IOException e){
    	new Error(e.toString(),-1);
    }
  }

  private void discard(){
    try{
      int i = inFile.read();
      if(i == -1) //end of file
        currentChar = '\u0000';
      else
        currentChar = (char)i;
    }
    catch(IOException e){
    	new Error(e.toString(),-1);
    }
  }

  private byte scanToken()
  {
    switch(currentChar)
    {
      case '(':
      {
          takeIt();
          return Token.LPAREN;
      }
      case ')':
      {
          takeIt();
          return Token.RPAREN;
      }
      case ';':
      {
          takeIt();
          return Token.SEMI;
      }
      case ',':
      {
          takeIt();
          return Token.COMMA;
      }
      case '\u0000':
      {
          takeIt();
          return Token.EOT;
      }
      default: 
      {
         
          if(isLetter(currentChar))
          {
              while(isLetter(currentChar))
                  takeIt();
              if(currentSpelling.toString().equalsIgnoreCase("Color"))
                  return Token.COLOR;
              else if(currentSpelling.toString().equalsIgnoreCase("Roll"))
                  return Token.ROLL;
              else if(currentSpelling.toString().equalsIgnoreCase("Turn"))
                  return Token.TURN;
              else if(currentSpelling.toString().equalsIgnoreCase("Arc"))
                  return Token.ARC;
              else
                  return Token.EOT;
          }
          else if(currentChar == '-')
          {
              takeIt();
              while(isDigit(currentChar) || (currentChar == '.'))
                takeIt();
              return Token.NUMBER;
          }
          else if(isDigit(currentChar))
          {
              while(isDigit(currentChar) || (currentChar == '.'))
                takeIt();
              return Token.NUMBER;
          }
          else
            return Token.EOT;
      }
    }
  }

  private void scanSeparator(){
    switch(currentChar){
      case ' ': case '\n': case '\r': case '\t':
        if(currentChar == '\n')
          line++;
        discard();
    }
  }

  public Token scan(){
    currentSpelling = new StringBuffer("");
    while(currentChar == ' ' || currentChar == '\n' || currentChar == '\r')
      scanSeparator();
    currentKind = scanToken();
    return new Token(currentKind, currentSpelling.toString(), line);
  }

  private boolean isDigit(char c){
    return '0' <= c && c <= '9';
  }

  private boolean isLetter(char c){
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }
}
