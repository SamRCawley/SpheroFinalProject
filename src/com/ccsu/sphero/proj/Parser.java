package com.ccsu.sphero.proj;

import java.io.BufferedReader;

public class Parser{
  private Token currentToken;
  Scanner scanner;
  String currOut ="\t";
  int line = 1;
  boolean statementloop = true;
  boolean pairloop = true;

  private void accept(byte expectedKind) throws Exception {
    if (currentToken.kind == expectedKind)
    {
        acceptIt();
    }  
    else
      throw new Exception("Syntax error: '" + currentToken.spelling + "' is not expected at line: " + currentToken.line);
  }
  
  private void accept(byte expectedKind, float rangeMin, float rangeMax) throws Exception {
	    if (currentToken.kind == expectedKind)
	    {
	    	if(Float.parseFloat(currentToken.spelling) >= rangeMin && Float.parseFloat(currentToken.spelling) <= rangeMax)
	        	acceptIt();
	        else throw new Exception("Syntax error: '" + currentToken.spelling + "' is out of range at line: " + currentToken.line);
	    }  
	    else
	      throw new Exception("Syntax error: '" + currentToken.spelling + "' is not expected at line: " + currentToken.line);
	  }
  
  private void accept(byte expectedKind, int rangeMin, int rangeMax) throws Exception {
	    if (currentToken.kind == expectedKind)
	    {
	        if(Integer.parseInt(currentToken.spelling) >= rangeMin && Integer.parseInt(currentToken.spelling) <= rangeMax)
	        	acceptIt();
	        else throw new Exception("Syntax error: '" + currentToken.spelling + "' is out of range at line: " + currentToken.line);
	    }  
	    else
	      throw new Exception("Syntax error: '" + currentToken.spelling + "' is not expected at line: " + currentToken.line);
	  }

  private void acceptIt() {
    currentToken = scanner.scan();
  }

  public void parse(BufferedReader infile) throws Exception{
    scanner = new Scanner(infile);
    parseScript();
    if (currentToken.kind != Token.EOT)
      new Exception("Syntax error: Redundant characters at the end of program at line: " +
                currentToken.line);
  }

  private void parseScript() throws Exception
  {
      currentToken = scanner.scan();
      parseStatements();
      
  }

  private void parseStatements() throws Exception
  {
      while(statementloop) 
          parseStmt();
  }

  private void parseStmt() throws Exception
  {

      if(currentToken.kind == Token.COLOR)
      {  
          parseColor();
      }
      else if(currentToken.kind == Token.ROLL)
      {
        parseRoll();
      }
      else if(currentToken.kind == Token.TURN)
      {
        parseTurn();
      }
      else if(currentToken.kind == Token.ARC)
      {
        parseArc();
      }
      else if(currentToken.kind == Token.OTHER)
      {
    	  throw new Exception("Unrecognized " + currentToken.spelling +" at line: " + currentToken.line);
      }
      else
      {
          statementloop = false;
      }          
  }
  
  private void parseColor() throws Exception
  {
      accept(Token.COLOR);
      for(int i = 0; i < 3; i++)
      {
    	  accept(Token.NUMBER, 0, 255);
      }
   }
   
   private void parseRoll() throws Exception
   {
      accept(Token.ROLL);
	  accept(Token.NUMBER, 0f, Float.MAX_VALUE);
      accept(Token.NUMBER, 0f, 1f);
   }
   
   private void parseTurn() throws Exception
   {
      accept(Token.TURN);
	  accept(Token.NUMBER, -360, 360);
   }
   
   private void parseArc() throws Exception
   {
      accept(Token.ARC);
      accept(Token.NUMBER, 0f, Float.MAX_VALUE);
      accept(Token.NUMBER, -360, 360);
      accept(Token.NUMBER, 0, Float.MAX_VALUE);
   }
}
