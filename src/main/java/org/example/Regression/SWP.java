package org.example.Regression;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.example.DBConnectiion.DBconnection;
import org.example.Login.Data;
import org.example.Pojo.*;
import org.example.Pojo.OTP.CommonOTP;
import org.example.Pojo.OTP.VerifyOtpRequest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class SWP {
    RequestSpecification req = new RequestSpecBuilder()
            //           .setBaseUri(Data.BaseURL)
            .addHeader("x-api-version", "2.0")
            .addHeader("channel-id", "12")
            .addHeader("x-fi-access-token", Data.Regression())
            .setContentType(ContentType.JSON).build();
    ResponseSpecification respec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON).build();

    public SWP() throws IOException {
    }

    String Holdingid,InvestorId,otp_refid, dbotp, DB_refid, Swp_ID;
    int Min_Installment,No_installments;     double Min_Amount;
    String goalid,folio,scheme_name,schemecode,Scheme_Frequency,Frequency;
    Date sdate,edate;    String StartDate,EndDate,EcsDate;

    @Test
    public void Holding_Profile() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL);
        HoldingProfile.Root hold_response = res.when().get("/core/investor/holding-profiles")
                .then().log().all().spec(respec).extract().response().as(HoldingProfile.Root.class);
        int size = hold_response.getData().size();  // Data Size
        for (int i = 0; i < size; i++) {
            if(hold_response.getData().get(i).getHoldingProfileId().equalsIgnoreCase(Data.HoldID)){
                Holdingid = hold_response.getData().get(i).getHoldingProfileId();
                System.out.println("Holding ID :" + Holdingid);
                for(int j=0;j<hold_response.getData().get(i).getInvestors().size();j++){
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
             if(response.getData().get(i).getFolio().equalsIgnoreCase(Data.SWP_Folio)){
                    schemecode = response.getData().get(i).getSchemeCode();
                    scheme_name=response.getData().get(i).getSchemeName();
                    goalid=response.getData().get(i).getGoalId();
                    folio=response.getData().get(i).getFolio();
                    System.out.println(folio);
                }
        }
    }

    @Test(priority = 2)
    public void product_search_mf_form() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .queryParam("page", 1)
                .queryParam("size", 100)
                .queryParam("schemeCodes", schemecode).log().params().log().uri();
        MFscheme.Root response = res.when().get("/core/product-search/mf/schemes")
                .then().log().all().spec(respec).extract().response().as(MFscheme.Root.class);
        for (int i = 0; i < response.getData().getContent().size(); i++) {
            for(int j = 0; j<response.getData().getContent().get(i).getSwpFrequencies().size(); j++)
            {
                Min_Installment=response.getData().getContent().get(i).getSwpFrequencies().get(j).getMinimum();
                Scheme_Frequency=response.getData().getContent().get(i).getSwpFrequencies().get(j).getFrequency();
                Min_Amount=response.getData().getContent().get(i).getSwpFrequencies().get(j).getAmounts().getMinimumAmount();
            }
        }
    }

    @Test(priority = 3)
    public void Installment_dates() {
        Map<String,Object> payload=new HashMap<String,Object>();
        payload.put("installments",Min_Installment);
        payload.put("frequency","monthly");
        payload.put("feature","SWP");
        payload.put("ecsDate",1);

        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(payload);
        InstallmentDates.Root response=res.when().post("/core/calculators/installment-dates")
                .then().log().body().spec(respec).extract().response().as(InstallmentDates.Root.class);
        No_installments= response.getData().getInstallments();
        int ecs_date=response.getData().getEcsDate();
        EcsDate=Integer.toString(ecs_date);
        Frequency= response.getData().getFrequency();
        sdate=response.getData().getStartDate();
        edate=response.getData().getEndDate();
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        StartDate=df.format(sdate);
        EndDate = df.format(edate);
    }
    @Test(priority = 4)
    public void Common_OTP() {
        Map<String, Object> otppayload = new HashMap<String, Object>();
        otppayload.put("type", "mobile_and_email");
        otppayload.put("idType", "folio");
        otppayload.put("referenceId", folio);
        otppayload.put("workflow", "swp");

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
    public void SWP_API() {
        Map<String,Object> payload=new HashMap<String,Object>();
        payload.put("holdingProfileId",Holdingid);
        payload.put("goalId",goalid);
        payload.put("folio",folio);
        payload.put("schemeCode",schemecode);
        payload.put("dayOrDate",EcsDate);
        payload.put("numberOfInstallments",No_installments);
        payload.put("frequency",Frequency);
        payload.put("amount",Integer.parseInt(Data.SWP_Amt));
        payload.put("startDate",StartDate);
        payload.put("endDate",EndDate);
        payload.put("otpReferenceId",DB_refid);

        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(payload);
        SWPResponse.Root response=res.when().post("/core/investor/current-swps")
                .then().log().all().spec(respec).extract().response().as(SWPResponse.Root.class);
        Swp_ID=response.getData().getSwpId();
        System.out.println(Swp_ID);
        System.out.println(response.getData().getSwpAction());
    }
}
