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
        /*if(currentToken.line == line)
            currOut+= currentToken.spelling;
        else
        {
            System.out.println(currOut);
            line++;
            currOut = "\t" + currentToken.spelling;
        }*/
        acceptIt();
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
          int color = Integer.parseInt(currentToken.spelling);
          if(color >= 0 && color <=255)
          {
            accept(Token.NUMBER);
          }
          else
            throw new Exception("Parameter Error " + currentToken.spelling + " is not expected at line: " + currentToken.line);
      }
   }
   
   private void parseRoll() throws Exception
   {
      accept(Token.ROLL);
      float distance = Float.parseFloat(currentToken.spelling);
      if(distance >= 0)
      {
         accept(Token.NUMBER);
      }
      else
         throw new Exception("Parameter Error " + currentToken.spelling + " is not expected at line: " + currentToken.line);
      float speed = Float.parseFloat(currentToken.spelling);
      if(speed >= 0 && speed <= 1)
      {
         accept(Token.NUMBER);
      }
      else
         throw new Exception("Parameter Error " + currentToken.spelling + " is not expected at line: " + currentToken.line);
   }
   
   private void parseTurn() throws Exception
   {
      accept(Token.TURN);
      int angle = Integer.parseInt(currentToken.spelling);
      if(angle <=360 && angle >=-360)
        accept(Token.NUMBER);
   }
   
   private void parseArc() throws Exception
   {
      accept(Token.ARC);
      float radius = Float.parseFloat(currentToken.spelling);
      if(radius >= 0)
      {
         accept(Token.NUMBER);
      }
      else
         throw new Exception("Parameter Error " + currentToken.spelling + " is not expected at line: " + currentToken.line);
      int angle = Integer.parseInt(currentToken.spelling);
      if(angle <=360 && angle >=-360)
      {
         accept(Token.NUMBER);
      }
      else
         throw new Exception("Parameter Error " + currentToken.spelling + " is not expected at line: " + currentToken.line);
      float speed = Float.parseFloat(currentToken.spelling);
      if(speed >= 0)
      {
         accept(Token.NUMBER);
      }
      else
         throw new Exception("Parameter Error " + currentToken.spelling + " is not expected at line:" + currentToken.line);
   }
}
