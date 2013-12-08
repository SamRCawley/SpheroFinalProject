package com.ccsu.sphero.proj;

import java.io.BufferedReader;

public class Parser{
  private Token currentToken;
  Scanner scanner;
  String currOut ="\t";
  int line = 1;
  boolean statementloop = true;
  boolean pairloop = true;

  private void accept(byte expectedKind) {
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
      new Error("Syntax error: '" + currentToken.spelling + "' is not expected.", currentToken.line);
  }

  private void acceptIt() {
    currentToken = scanner.scan();
  }

  public void parse(BufferedReader infile) {
    scanner = new Scanner(infile);
    parseScript();
    if (currentToken.kind != Token.EOT)
      new Error("Syntax error: Redundant characters at the end of program.",
                currentToken.line);
  }

  private void parseScript() 
  {
      currentToken = scanner.scan();
      parseStatements();
      
  }

  private void parseStatements()
  {
      while(statementloop) 
          parseStmt();
  }

  private void parseStmt()
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
  
  private void parseColor()
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
            new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
      }
   }
   
   private void parseRoll()
   {
      accept(Token.ROLL);
      float distance = Float.parseFloat(currentToken.spelling);
      if(distance >= 0)
      {
         accept(Token.NUMBER);
      }
      else
         new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
      float speed = Float.parseFloat(currentToken.spelling);
      if(speed >= 0 && speed <= 1)
      {
         accept(Token.NUMBER);
      }
      else
         new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
   }
   
   private void parseTurn()
   {
      accept(Token.TURN);
      int angle = Integer.parseInt(currentToken.spelling);
      if(angle <=360 && angle >=-360)
        accept(Token.NUMBER);
   }
   
   private void parseArc()
   {
      accept(Token.ARC);
      float radius = Float.parseFloat(currentToken.spelling);
      if(radius >= 0)
      {
         accept(Token.NUMBER);
      }
      else
         new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
      int angle = Integer.parseInt(currentToken.spelling);
      if(angle <=360 && angle >=-360)
      {
         accept(Token.NUMBER);
      }
      else
         new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
      float speed = Float.parseFloat(currentToken.spelling);
      if(speed >= 0)
      {
         accept(Token.NUMBER);
      }
      else
         new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
   }
}
