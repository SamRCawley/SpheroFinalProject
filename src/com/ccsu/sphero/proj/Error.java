package com.ccsu.sphero.proj;

public class Error{
  public Error(String message, int line){
    throw new ArithmeticException(message);
  }
}