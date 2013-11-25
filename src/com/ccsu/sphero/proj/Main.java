package com.ccsu.sphero.proj;

import android.util.Log;

public class Main{
  public static void main(String[] args){
    Parser p = new Parser();
    p.parse();
    Log.d("Parser Test", "The syntax of the script is correct.");
  }
}
