package org.example.Login;


import io.restassured.RestAssured;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Pojo.Signin;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import static io.restassured.RestAssured.*;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.given;

public class Data {
    public static String  Email, password, grandtype, refreshtoken;
    public static double  Switch_Units,Switch_Amt;
    public static String Switch_Folio,Switch_Targetscheme;
    public static String HoldID,FolioID,BaseURL,Inv_Amount,SchemeSearch,Expected_Scheme,Goal_Name,Switch_TargetScheme;
    public static String Redeem_units=null,Redeem_amt=null,Redeem_Folio;
    @Test
    public static String Regression() throws IOException {

        //  Platform currentPlatform = Platform.getCurrent();

        FileInputStream fis = new FileInputStream("C:\\Users\\Fi-User\\IdeaProjects\\Redemption\\src\\main\\testdata\\Login.xlsx");


         //       FileInputStream(System.getProperty("user.dir")+"\\src\\main\\testdata\\Login.xlsx");

/*      FileInputStream fis = new
              FileInputStream(System.getProperty("user.dir")+"/src/main/testdata/Login.xlsx");*/

        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet1 = wb.getSheetAt(0);
        BaseURL = sheet1.getRow(0).getCell(1).getStringCellValue();
        Email = sheet1.getRow(1).getCell(1).getStringCellValue();
        password = sheet1.getRow(2).getCell(1).getStringCellValue();
        grandtype = sheet1.getRow(3).getCell(1).getStringCellValue();
        refreshtoken = sheet1.getRow(4).getCell(1).getStringCellValue();


        HashMap<String, String> login = new HashMap<String, String>();
        login.put("emailId", Email);
        login.put("password",password);
        login.put("grantType", grandtype);
        login.put("refreshToken", refreshtoken);

//Holding and Folio Data
        HoldID= sheet1.getRow(6).getCell(1).getStringCellValue();
        FolioID=sheet1.getRow(7).getCell(1).getStringCellValue();

//Invest More Amount
        Inv_Amount =String.valueOf((int) sheet1.getRow(8).getCell(1).getNumericCellValue());

        //Scheme_Search
        SchemeSearch = sheet1.getRow(9).getCell(1).getStringCellValue();
//Expected_Scheme
        Expected_Scheme=sheet1.getRow(10).getCell(1).getStringCellValue();
//Goal_Name
        Goal_Name=sheet1.getRow(11).getCell(1).getStringCellValue();
//Switch_Target_Scheme
        Switch_TargetScheme=sheet1.getRow(24).getCell(1).getStringCellValue();

//Redeemtion Data
        Redeem_units= sheet1.getRow(16).getCell(1).getStringCellValue();
        Redeem_amt= sheet1.getRow(17).getCell(1).getStringCellValue();
        Redeem_Folio= sheet1.getRow(18).getCell(1).getStringCellValue();
// Switch Data
        Switch_Units= Double.parseDouble(String.valueOf((int) sheet1.getRow(21).getCell(1).getNumericCellValue()));

        Switch_Amt= Double.parseDouble(String.valueOf((int) sheet1.getRow(22).getCell(1).getNumericCellValue()));
       /* Switch_Units= Integer.parseInt(sheet1.getRow(21).getCell(1).getStringCellValue());
        Switch_Amt= Integer.parseInt(sheet1.getRow(22).getCell(1).getStringCellValue());*/
        Switch_Folio= sheet1.getRow(23).getCell(1).getStringCellValue();
        Switch_Targetscheme= sheet1.getRow(24).getCell(1).getStringCellValue();


        RestAssured.baseURI= BaseURL;
        Signin.Root response=given().log().all()
                .header("x-api-version","2.0")
                .header("channel-id","10")
                .header("Content-Type","application/json")

                .body(login).when().post("/core/auth/sign-in")
                .then().assertThat().statusCode(200)
                .header("Content-Type","application/json")
                .extract().response().as(Signin.Root.class);
        return response.getData().getAccessToken();
    }
    @Test
    public void testingdata() throws IOException {
        FileInputStream fis = new
                 FileInputStream("C:\\Users\\Fi-User\\IdeaProjects\\Redemption\\src\\main\\testdata\\Login.xlsx");
           //     FileInputStream(System.getProperty("user.dir")+"\\src\\main\\testdata\\Login.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet1 = wb.getSheetAt(0);

        Redeem_Folio= sheet1.getRow(24).getCell(1).getStringCellValue();

        System.out.println(Redeem_Folio);

    }

}
