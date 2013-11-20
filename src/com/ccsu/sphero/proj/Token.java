package com.ccsu.sphero.proj;

public class Token {
  public byte kind;
  public String spelling;
  public int line;

  private final static String[] spellings = {"Color", "Roll", "Turn", "Arc"};


  public Token(byte kind, String spelling, int line){
    this.kind = kind;
    this.spelling = spelling;
    this.line = line;
  }

  public final static byte 
  COLOR = 0, 
  ROLL = 1,
  TURN = 2,
  ARC = 3,
  NUMBER = 4, 
  LPAREN = 5, 
  RPAREN = 6, 
  SEMI = 7, 
  COMMA = 8,
  NEG = 9,
  EOT = 10;
}
