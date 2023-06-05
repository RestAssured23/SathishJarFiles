package org.example;

import org.testng.TestNG;

import java.util.ArrayList;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        TestNG runner = new TestNG();
        List<String> xmlfile = new ArrayList<String>();

     //   xmlfile.add(System.getProperty("user.dir") + "\\src\\main\\testdata\\Test.xml");

       xmlfile.add("C:\\Users\\Fi-User\\IdeaProjects\\Redemption\\src\\main\\testdata\\Redeem.xml");
        runner.setTestSuites(xmlfile);
        runner.run();
    }
}