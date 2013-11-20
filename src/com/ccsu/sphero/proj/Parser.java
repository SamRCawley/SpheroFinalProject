package com.ccsu.sphero.proj;

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
        if(currentToken.line == line)
            currOut+= currentToken.spelling;
        else
        {
            System.out.println(currOut);
            line++;
            currOut = "\t" + currentToken.spelling;
        }
        acceptIt();
    }  
    else
      new Error("Syntax error: '" + currentToken.spelling + "' is not expected.", currentToken.line);
  }

  private void acceptIt() {
    currentToken = scanner.scan();
  }

  public void parse() {
    SourceFile sourceFile = new SourceFile();
    scanner = new Scanner(sourceFile.openFile());
    parseScript();
    if (currentToken.kind != Token.EOT)
      new Error("Syntax error: Redundant characters at the end of program.",
                currentToken.line);
  }

  private void parseScript() 
  {
      System.out.println("<begin script>");
      currentToken = scanner.scan();
      parseStatements();
      System.out.println("<end script>");
      
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
      accept(Token.LPAREN);
      for(int i = 0; i < 3; i++)
      {
          int color = Integer.parseInt(currentToken.spelling);
          if(color >= 0 && color <=255)
          {
            accept(Token.NUMBER);
            if(i < 2)
                accept(Token.COMMA);
          }
          else
            new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
      }
      accept(Token.RPAREN);
      accept(Token.SEMI);
   }
   
   private void parseRoll()
   {
      accept(Token.ROLL);
      accept(Token.LPAREN);
      float distance = Float.parseFloat(currentToken.spelling);
      if(distance >= 0)
      {
         accept(Token.NUMBER);
         accept(Token.COMMA);
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
      accept(Token.RPAREN);
      accept(Token.SEMI);
   }
   
   private void parseTurn()
   {
      accept(Token.TURN);
      accept(Token.LPAREN);
      int angle = Integer.parseInt(currentToken.spelling);
      if(angle <=360 && angle >=-360)
        accept(Token.NUMBER);
      accept(Token.RPAREN);
      accept(Token.SEMI);
   }
   
   private void parseArc()
   {
      accept(Token.ARC);
      accept(Token.LPAREN);
      float radius = Float.parseFloat(currentToken.spelling);
      if(radius >= 0)
      {
         accept(Token.NUMBER);
         accept(Token.COMMA);
      }
      else
         new Error("Parameter Error " + currentToken.spelling + " is not expected.", currentToken.line);
      int angle = Integer.parseInt(currentToken.spelling);
      if(angle <=360 && angle >=-360)
      {
         accept(Token.NUMBER);
         accept(Token.COMMA);
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
      accept(Token.RPAREN);
      accept(Token.SEMI);
   }
}
