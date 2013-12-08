package com.ccsu.sphero.proj;

public class Error{
  public Error(String message, int line){
	  System.out.append("This works");
	  System.exit(0);
    throw new ArithmeticException(message);
  }
}