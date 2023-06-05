package org.example.MainClass;

import org.example.Regression.GetAPI_AllSupport;
import org.testng.TestNG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class GetAPI {
    public static void main(String[] args) throws IOException {
        TestNG runner = new TestNG();
        List<String> xmlfile = new ArrayList<String>();

        xmlfile.add(System.getProperty("user.dir") + "\\src\\main\\testdata\\Test.xml");

        // xmlfile.add("C:\\Users\\Fi-User\\IdeaProjects\\APIAutomation\\src\\main\\testdata\\primaryID.xml");
        runner.setTestSuites(xmlfile);
        runner.run();

    }
}