package org.example.Regression;

import com.google.gson.stream.JsonToken;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.example.DBConnectiion.DBconnection;
import org.example.Login.Data;
import org.example.Pojo.HoldingProfile;
import org.example.Pojo.InvestedScheme;
import org.example.Pojo.MFscheme;
import org.example.Pojo.OTP.CommonOTP;
import org.example.Pojo.OTP.VerifyOtpRequest;
import org.example.Pojo.RecentTransaction;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class Switch {
    RequestSpecification req = new RequestSpecBuilder()
            //                  .setBaseUri(Data.BaseURL)
            .addHeader("x-api-version", "2.0")
            .addHeader("channel-id", "12")
            .addHeader("x-fi-access-token", Data.Regression())
            .setContentType(ContentType.JSON).build();
    ResponseSpecification respec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON).build();

    String Holdingid, InvestorId, folio, otp_refid, dbotp, DB_refid, qref_id, RT_refno,Source_SchemeName;
    String goalid, goalname, schemcode, schemename, unintsformat, dividendoption, option, bankid, minunitformat, minuamountformat;
    double minamount, units, minunit, currentamount,Total_units;
    String fromschemename, fromschemecode, fromoption,   toschemename, toschemcode, tooption,AMC_Name, AMC_Code;


    public Switch() throws IOException, IOException {
    }

    @Test
    public void Holding_Profile() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL);
        HoldingProfile.Root hold_response = res.when().get("/core/investor/holding-profiles")
                .then().log().all().spec(respec).extract().response().as(HoldingProfile.Root.class);
        int size = hold_response.getData().size();  // Data Size
        for (int i = 0; i < size; i++) {
            if (hold_response.getData().get(i).getHoldingProfileId().equalsIgnoreCase(Data.HoldID)) {
                Holdingid = hold_response.getData().get(i).getHoldingProfileId();
                System.out.println("Holding ID :" + Holdingid);
                for (int j = 0; j < hold_response.getData().get(i).getInvestors().size(); j++) {
                    InvestorId = hold_response.getData().get(i).getInvestors().get(j).getInvestorId();
                    System.out.println("Investor ID : " + InvestorId);
                }
            }
        }
    }
    @Test(priority = 1)
    public void InvestedSchem_API() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .queryParam("holdingProfileId", Data.HoldID);
        InvestedScheme.Root response = res.when().get("/core/investor/invested-schemes")
                .then().log().all().spec(respec).extract().response().as(InvestedScheme.Root.class);
        int count = response.getData().size();

        for (int i = 0; i < count; i++) {
            if (response.getData().get(i).getFolio().equalsIgnoreCase(Data.Switch_Folio)) {
                fromschemename = response.getData().get(i).getSchemeName();
                fromschemecode = response.getData().get(i).getSchemeCode();
                folio = response.getData().get(i).getFolio();
                units = response.getData().get(i).getUnits();
                fromoption = response.getData().get(i).getOption();
                goalid = response.getData().get(i).getGoalId();
                bankid = response.getData().get(i).getBankId();
                minamount = response.getData().get(i).getSwitchOut().getMinimumAmount();
                minunit = response.getData().get(i).getSwitchOut().getMinimumUnits();
                currentamount = response.getData().get(i).getCurrentAmount();
                goalname = response.getData().get(i).getGoalName();
                Total_units = response.getData().get(i).getUnits();
                System.out.println(fromschemename);
                System.out.println(units);
            }
        }
    }
    @Test(priority = 2)
    public void product_search_mf_form() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .queryParam("page", 1)
                .queryParam("size", 100)
                .queryParam("schemeCodes", fromschemecode);
        MFscheme.Root response = res.when().get("/core/product-search/mf/schemes")
                .then().log().all().spec(respec).extract().response().as(MFscheme.Root.class);
        for (int i = 0; i < response.getData().getContent().size(); i++) {
            AMC_Name = response.getData().getContent().get(i).getAmc();
            AMC_Code = response.getData().getContent().get(i).getAmcCode();
            Source_SchemeName=response.getData().getContent().get(i).getName();
            System.out.printf(AMC_Code+"\t"+AMC_Name+"\t"+Source_SchemeName);

        }
    }

    @Test(priority = 3)
    public void TargetScheme_Search_Test() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body("{\n" +
                        "  \"page\": 1,\n" +
                        "  \"size\": 10,\n" +
                        "  \"orderBy\": \"rating\",\n" +
                        "  \"orderType\": \"DESC\",\n" +
                        "  \"categories\": [],\n" +
                        "  \"subCategories\": [],\n" +
                        "  \"query\": \"\",\n" +
                        "  \"risk\": [],\n" +
                        "  \"ratings\": [],\n" +
                        "  \"amcs\": [\n" +
                        "    {\n" +
                        "      \"name\": \"" + AMC_Name + "\",\n" +
                        "      \"value\": \"" + AMC_Code + "\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"searchCode\": [\n" +
                        "    {\n" +
                        "      \"value\": \"\",\n" +
                        "      \"sort\": true\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}");
        MFscheme.Root response = res.when().post("/core/product-search/mf")
                .then().log().all().spec(respec).extract().response().as(MFscheme.Root.class);
        int size = response.getData().getContent().size();
        if (Data.Switch_TargetScheme.equalsIgnoreCase("0")) {
            for (int i = 0; i <=2; i++) {
                toschemename = response.getData().getContent().get(i).getName();
                toschemcode = response.getData().getContent().get(i).getSchemeCode();
                tooption = response.getData().getContent().get(i).getOption();
                System.out.println("To SchemeName: " + toschemename);
                System.out.println("To schemecode: " + toschemcode);
                System.out.println("To Option: " + tooption);
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (response.getData().getContent().get(i).getName().equalsIgnoreCase(Data.Switch_TargetScheme)) {
                    toschemename = response.getData().getContent().get(i).getName();
                    toschemcode = response.getData().getContent().get(i).getSchemeCode();
                    tooption = response.getData().getContent().get(i).getOption();
                    System.out.println("To SchemeName: " + toschemename);
                    System.out.println("To schemecode: " + toschemcode);
                    System.out.println("To Option: " + tooption);
                }
            }
        }
    }


    @Test(priority = 4)
    public void Common_OTP() {
        Map<String, Object> otppayload = new HashMap<String, Object>();
        otppayload.put("type", "mobile_and_email");
        otppayload.put("idType", "folio");
        otppayload.put("referenceId",folio);
        otppayload.put("workflow", "switch");

        RequestSpecification commonotp = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(otppayload);
        CommonOTP.Root responce = commonotp.when().post("/core/investor/common/otp")
                .then().log().all().spec(respec).extract().response().as(CommonOTP.Root.class);
        otp_refid = responce.getData().getOtpReferenceId();
    }

    @Test(priority = 5)
    public void DB_Connection() throws SQLException {
        Statement s1 = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            DBconnection ds = new DBconnection();
            con = ds.getConnection();
            s1 = con.createStatement();
            rs = s1.executeQuery("select * from dbo.OTP_GEN_VERIFICATION ogv where referenceId ='" + otp_refid + "'");
            rs.next();
            dbotp = rs.getString("otp");
            DB_refid = rs.getString("referenceid");
            System.out.println("OTP :" + dbotp);
            System.out.println("OTPReferenceID :" + DB_refid);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (s1 != null) s1.close();
            if (rs != null) rs.close();
            if (con != null) con.close();
        }
    }

    @Test(priority = 6)
    public void Verify_OTP() {
        VerifyOtpRequest.Root payload = new VerifyOtpRequest.Root();
        VerifyOtpRequest.Otp otp = new VerifyOtpRequest.Otp();
        otp.setSms("");
        otp.setEmail("");
        otp.setEmail_or_sms(dbotp);
        payload.setOtp(otp);
        payload.setOtpReferenceId(DB_refid);
        RequestSpecification res1 = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(payload);
        res1.when().post("/core/investor/common/otp/verify")
                .then().log().all().spec(respec);
    }

    @Test(priority = 7)
    public void Switch_API() {
        Map<String, Object> Growth_Growth_Unit = new HashMap<String, Object>();
        Growth_Growth_Unit.put("holdingProfileId", Holdingid);
        Growth_Growth_Unit.put("folio", folio);
        Growth_Growth_Unit.put("goalId", goalid);
        Growth_Growth_Unit.put("goalName", goalname);
        Growth_Growth_Unit.put("fromSchemeName", fromschemename);
        Growth_Growth_Unit.put("fromSchemeCode", fromschemecode);
        Growth_Growth_Unit.put("toSchemeName", toschemename);
        Growth_Growth_Unit.put("toSchemeCode", toschemcode);
        Growth_Growth_Unit.put("units", Data.Switch_Units);
        Growth_Growth_Unit.put("fromOption", fromoption);
        Growth_Growth_Unit.put("toOption", tooption);
        Growth_Growth_Unit.put("switchMode", "partial");
        Growth_Growth_Unit.put("switchType", "regular");
        Growth_Growth_Unit.put("bankId", bankid);
        Growth_Growth_Unit.put("otpReferenceId", DB_refid);

        Map<String, Object> Growth_Growth_Amount = new HashMap<String, Object>();
        Growth_Growth_Amount.put("holdingProfileId", Holdingid);
        Growth_Growth_Amount.put("folio", folio);
        Growth_Growth_Amount.put("goalId", goalid);
        Growth_Growth_Amount.put("goalName", goalname);
        Growth_Growth_Amount.put("fromSchemeName", fromschemename);
        Growth_Growth_Amount.put("fromSchemeCode", fromschemecode);
        Growth_Growth_Amount.put("toSchemeName", toschemename);
        Growth_Growth_Amount.put("toSchemeCode", toschemcode);
        Growth_Growth_Unit.put("amount", Data.Switch_Amt);
        Growth_Growth_Amount.put("fromOption", fromoption);
        Growth_Growth_Amount.put("toOption", tooption);
        Growth_Growth_Amount.put("switchMode", "partial");
        Growth_Growth_Amount.put("switchType", "regular");
        Growth_Growth_Amount.put("bankId", bankid);
        Growth_Growth_Amount.put("otpReferenceId", DB_refid);

        Map<String, Object> Growth_Growth_All = new HashMap<String, Object>();
        Growth_Growth_All.put("holdingProfileId", Holdingid);
        Growth_Growth_All.put("folio", folio);
        Growth_Growth_All.put("goalId", goalid);
        Growth_Growth_All.put("goalName", goalname);
        Growth_Growth_All.put("fromSchemeName", fromschemename);
        Growth_Growth_All.put("fromSchemeCode", fromschemecode);
        Growth_Growth_All.put("toSchemeName", toschemename);
        Growth_Growth_All.put("toSchemeCode", toschemcode);
        Growth_Growth_Unit.put("units", Total_units);
        Growth_Growth_All.put("fromOption", fromoption);
        Growth_Growth_All.put("toOption", tooption);
        Growth_Growth_All.put("switchMode", "full");
        Growth_Growth_All.put("switchType", "regular");
        Growth_Growth_All.put("bankId", bankid);
        Growth_Growth_All.put("otpReferenceId", DB_refid);

        Map<String, Object> Growth_Div_Unit = new HashMap<String, Object>();
        Growth_Div_Unit.put("holdingProfileId", Holdingid);
        Growth_Div_Unit.put("folio", folio);
        Growth_Div_Unit.put("goalId", goalid);
        Growth_Div_Unit.put("goalName", goalname);
        Growth_Div_Unit.put("fromSchemeName", fromschemename);
        Growth_Div_Unit.put("fromSchemeCode", fromschemecode);
        Growth_Div_Unit.put("toSchemeName", toschemename);
        Growth_Div_Unit.put("toSchemeCode", toschemcode);
        Growth_Growth_All.put("units", Data.Switch_Units);
        Growth_Div_Unit.put("fromOption", fromoption);
        Growth_Div_Unit.put("toDividendOption", "Payout");       // Payout / Reinvestment
        Growth_Div_Unit.put("toOption", tooption);
        Growth_Div_Unit.put("switchMode", "partial");              //partial or all
        Growth_Div_Unit.put("switchType", "regular");
        Growth_Div_Unit.put("bankId", bankid);
        Growth_Div_Unit.put("otpReferenceId", DB_refid);

        Map<String, Object> Growth_Div_Amount = new HashMap<String, Object>();
        Growth_Div_Amount.put("holdingProfileId", Holdingid);
        Growth_Div_Amount.put("folio", folio);
        Growth_Div_Amount.put("goalId", goalid);
        Growth_Div_Amount.put("goalName", goalname);
        Growth_Div_Amount.put("fromSchemeName", fromschemename);
        Growth_Div_Amount.put("fromSchemeCode", fromschemecode);
        Growth_Div_Amount.put("toSchemeName", toschemename);
        Growth_Div_Amount.put("toSchemeCode", toschemcode);
        Growth_Div_Amount.put("amount", Data.Switch_Amt);
        Growth_Div_Amount.put("fromOption", fromoption);
        Growth_Div_Amount.put("toDividendOption", "Payout");       // Payout / Reinvestment
        Growth_Div_Amount.put("toOption", tooption);
        Growth_Div_Amount.put("switchMode", "partial");              //partial or all
        Growth_Div_Amount.put("switchType", "regular");
        Growth_Div_Amount.put("bankId", bankid);
        Growth_Div_Amount.put("otpReferenceId", DB_refid);

        Map<String, Object> Growth_Div_All = new HashMap<String, Object>();
        Growth_Div_All.put("holdingProfileId", Holdingid);
        Growth_Div_All.put("folio", folio);
        Growth_Div_All.put("goalId", goalid);
        Growth_Div_All.put("goalName", goalname);
        Growth_Div_All.put("fromSchemeName", fromschemename);
        Growth_Div_All.put("fromSchemeCode", fromschemecode);
        Growth_Div_All.put("toSchemeName", toschemename);
        Growth_Div_All.put("toSchemeCode", toschemcode);
        Growth_Growth_All.put("units", Total_units);
        Growth_Div_All.put("fromOption", fromoption);
        Growth_Div_All.put("toDividendOption", "Payout");       // Payout / Reinvestment
        Growth_Div_All.put("toOption", tooption);
        Growth_Div_All.put("switchMode", "full");              //partial or all
        Growth_Div_All.put("switchType", "regular");
        Growth_Div_All.put("bankId", bankid);
        Growth_Div_All.put("otpReferenceId", DB_refid);


        Map<String, Object> Div_Div_Unit = new HashMap<String, Object>();
        Div_Div_Unit.put("holdingProfileId", Holdingid);
        Div_Div_Unit.put("folio", folio);
        Div_Div_Unit.put("goalId", goalid);
        Div_Div_Unit.put("goalName", goalname);
        Div_Div_Unit.put("fromSchemeName", fromschemename);
        Div_Div_Unit.put("fromSchemeCode", fromschemecode);
        Div_Div_Unit.put("toSchemeName", toschemename);
        Div_Div_Unit.put("toSchemeCode", toschemcode);
        Div_Div_Unit.put("units", Data.Switch_Units);
        Div_Div_Unit.put("fromOption", fromoption);
        Div_Div_Unit.put("fromDividendOption", "Payout");
        Div_Div_Unit.put("toOption", tooption);
        Div_Div_Unit.put("toDividendOption", "Reinvestment");       // Payout / Reinvestment
        Div_Div_Unit.put("switchMode", "partial");              //partial or all
        Div_Div_Unit.put("switchType", "regular");
        Div_Div_Unit.put("bankId", bankid);
        Div_Div_Unit.put("otpReferenceId", DB_refid);

        Map<String, Object> Div_Div_Amount = new HashMap<String, Object>();
        Div_Div_Amount.put("holdingProfileId", Holdingid);
        Div_Div_Amount.put("folio", folio);
        Div_Div_Amount.put("goalId", goalid);
        Div_Div_Amount.put("goalName", goalname);
        Div_Div_Amount.put("fromSchemeName", fromschemename);
        Div_Div_Amount.put("fromSchemeCode", fromschemecode);
        Div_Div_Amount.put("toSchemeName", toschemename);
        Div_Div_Amount.put("toSchemeCode", toschemcode);
        Div_Div_Amount.put("amount", Data.Switch_Amt);
        Div_Div_Amount.put("fromOption", fromoption);
        Div_Div_Amount.put("fromDividendOption", "Payout");
        Div_Div_Amount.put("toOption", tooption);
        Div_Div_Amount.put("toDividendOption", "Reinvestment");       // Payout / Reinvestment
        Div_Div_Amount.put("switchMode", "partial");              //partial or all
        Div_Div_Amount.put("switchType", "regular");
        Div_Div_Amount.put("bankId", bankid);
        Div_Div_Amount.put("otpReferenceId", DB_refid);

        Map<String, Object> Div_Div_All = new HashMap<String, Object>();
        Div_Div_All.put("holdingProfileId", Holdingid);
        Div_Div_All.put("folio", folio);
        Div_Div_All.put("goalId", goalid);
        Div_Div_All.put("goalName", goalname);
        Div_Div_All.put("fromSchemeName", fromschemename);
        Div_Div_All.put("fromSchemeCode", fromschemecode);
        Div_Div_All.put("toSchemeName", toschemename);
        Div_Div_All.put("toSchemeCode", toschemcode);
        Div_Div_All.put("units", Total_units);
        Div_Div_All.put("fromOption", fromoption);
        Div_Div_All.put("fromDividendOption", "Payout");
        Div_Div_All.put("toOption", tooption);
        Div_Div_All.put("toDividendOption", "Reinvestment");       // Payout / Reinvestment
        Div_Div_All.put("switchMode", "full");              //partial or all
        Div_Div_All.put("switchType", "regular");
        Div_Div_All.put("bankId", bankid);
        Div_Div_All.put("otpReferenceId", DB_refid);


        Map<String, Object> Div_Growth_Unit = new HashMap<String, Object>();
        Div_Growth_Unit.put("holdingProfileId", Holdingid);
        Div_Growth_Unit.put("folio", folio);
        Div_Growth_Unit.put("goalId", goalid);
        Div_Growth_Unit.put("goalName", goalname);
        Div_Growth_Unit.put("fromSchemeName", fromschemename);
        Div_Growth_Unit.put("fromSchemeCode", fromschemecode);
        Div_Growth_Unit.put("toSchemeName", toschemename);
        Div_Growth_Unit.put("toSchemeCode", toschemcode);
        Div_Growth_Unit.put("units", Data.Switch_Units);
        Div_Growth_Unit.put("fromOption", fromoption);
        Div_Growth_Unit.put("fromDividendOption", "Payout");
        Div_Growth_Unit.put("toOption", tooption);
        Div_Growth_Unit.put("switchMode", "partial");              //partial or full
        Div_Growth_Unit.put("switchType", "regular");
        Div_Growth_Unit.put("bankId", bankid);
        Div_Growth_Unit.put("otpReferenceId", DB_refid);

        Map<String, Object> Div_Growth_Amount = new HashMap<String, Object>();
        Div_Growth_Amount.put("holdingProfileId", Holdingid);
        Div_Growth_Amount.put("folio", folio);
        Div_Growth_Amount.put("goalId", goalid);
        Div_Growth_Amount.put("goalName", goalname);
        Div_Growth_Amount.put("fromSchemeName", fromschemename);
        Div_Growth_Amount.put("fromSchemeCode", fromschemecode);
        Div_Growth_Amount.put("toSchemeName", toschemename);
        Div_Growth_Amount.put("toSchemeCode", toschemcode);
        Div_Growth_Amount.put("amount", Data.Switch_Amt);
        Div_Growth_Amount.put("fromOption", fromoption);
        Div_Growth_Amount.put("fromDividendOption", "Payout");
        Div_Growth_Amount.put("toOption", tooption);
        Div_Growth_Amount.put("switchMode", "partial");              //partial or full
        Div_Growth_Amount.put("switchType", "regular");
        Div_Growth_Amount.put("bankId", bankid);
        Div_Growth_Amount.put("otpReferenceId", DB_refid);

        Map<String, Object> Div_Growth_All = new HashMap<String, Object>();
        Div_Growth_All.put("holdingProfileId", Holdingid);
        Div_Growth_All.put("folio", folio);
        Div_Growth_All.put("goalId", goalid);
        Div_Growth_All.put("goalName", goalname);
        Div_Growth_All.put("fromSchemeName", fromschemename);
        Div_Growth_All.put("fromSchemeCode", fromschemecode);
        Div_Growth_All.put("toSchemeName", toschemename);
        Div_Growth_All.put("toSchemeCode", toschemcode);
        Div_Growth_All.put("units", Total_units);
        Div_Growth_All.put("fromOption", fromoption);
        Div_Growth_All.put("fromDividendOption", "Payout");
        Div_Growth_All.put("toOption", tooption);
        Div_Growth_All.put("switchMode", "full");              //partial or full
        Div_Growth_All.put("switchType", "regular");
        Div_Growth_All.put("bankId", bankid);
        Div_Growth_All.put("otpReferenceId", DB_refid);

        if ((Data.Switch_Units==0) && Data.Switch_Amt==0) {

            if (fromoption.equalsIgnoreCase("Growth") && (tooption.equalsIgnoreCase("Growth"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Growth_Growth_All);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Growth") && (tooption.equalsIgnoreCase("Dividend"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Growth_Div_All);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Dividend") && (tooption.equalsIgnoreCase("Dividend"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Div_Div_All);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Dividend") && (tooption.equalsIgnoreCase("Growth"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Div_Growth_All);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            }
        }
        // Units =0
        else if (Data.Switch_Units==0) {
            if (fromoption.equalsIgnoreCase("Growth") && (tooption.equalsIgnoreCase("Growth"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Growth_Growth_Amount);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Growth") && (tooption.equalsIgnoreCase("Dividend"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Growth_Div_Amount);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Dividend") && (tooption.equalsIgnoreCase("Dividend"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Div_Div_Amount);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Dividend") && (tooption.equalsIgnoreCase("Growth"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Div_Growth_Amount);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            }
        }
        else {
            if (fromoption.equalsIgnoreCase("Growth") && (tooption.equalsIgnoreCase("Growth"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Growth_Growth_Unit);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Growth") && (tooption.equalsIgnoreCase("Dividend"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Growth_Div_Unit);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Dividend") && (tooption.equalsIgnoreCase("Dividend"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Div_Div_Unit);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            } else if (fromoption.equalsIgnoreCase("Dividend") && (tooption.equalsIgnoreCase("Growth"))) {
                RequestSpecification redeem = given().log().all().spec(req).baseUri(Data.BaseURL)
                        .body(Div_Growth_Unit);
                redeem.when().post("/core/investor/switch")
                        .then().log().all().spec(respec);
            }
        }
    }
    @Test(priority = 8)
    public void Recent_Transaction() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .queryParam("holdingProfileId", Holdingid)
                .queryParam("page", "1")
                .queryParam("size", "10");
        RecentTransaction.Root response = res.when().get("/core/investor/recent-transactions")
                .then().spec(respec).extract().response().as(RecentTransaction.Root.class);
        int count = response.getData().size();
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < response.getData().get(i).getMf().size(); j++) {
                for (int k = 0; k < response.getData().get(i).getMf().get(j).getActions().size(); k++) {
                    if (response.getData().get(i).getMf().get(j).getFolio().equalsIgnoreCase(Data.FolioID) ==
                            (response.getData().get(i).getMf().get(j).getActions().get(k).equalsIgnoreCase("cancel"))) {
                        RT_refno = response.getData().get(i).getMf().get(j).getReferenceNo();
                        System.out.println(RT_refno);
                    }
                }
            }
        }
    }
/*
    @Test(priority = 9)
    public void Delete_API() {
        Map<String, String> del = new HashMap<String, String>();
        del.put("action", "cancel");
        del.put("referenceNo", RT_refno);

        RequestSpecification can=given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(del);
        can.when().post("/core/investor/recent-transactions")
                .then().log().all().spec(respec);
    }*/
}
