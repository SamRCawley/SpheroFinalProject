package com.ccsu.sphero.proj;

import java.io.*;

import android.app.Activity;

public class SourceFile extends Activity{
  public BufferedReader openFile(){
    String fileName="";
    BufferedReader inFile=null;
    BufferedReader stdin = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.test)));
    try{
      fileName = stdin.readLine();
      inFile = new BufferedReader(new FileReader(fileName));
    }
    catch(FileNotFoundException e){
      System.out.println("The source file " + fileName + " was not found.");
    }
    catch(IOException e){
      System.out.println(e);
    }
    return inFile;
  }
}
