package org.example.Regression;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.example.DBConnectiion.DBconnection;
import org.example.Login.Data;
import org.example.Pojo.CurrentSWP;
import org.example.Pojo.InstallmentDates;
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

public class SWP_Edit {
    RequestSpecification req = new RequestSpecBuilder()
            //           .setBaseUri(Data.BaseURL)
            .addHeader("x-api-version", "2.0")
            .addHeader("channel-id", "12")
            .addHeader("x-fi-access-token", Data.Regression())
            .setContentType(ContentType.JSON).build();
    ResponseSpecification respec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON).build();

    public SWP_Edit() throws IOException {
    }

    String otp_refid, dbotp, DB_refid, Swp_ID,folio;   String Edit_StartDate,Edit_EndDate;
    int Edit_EcsDate,Edit_Installments;
    Date sdate,edate;
    @Test
    public void SWP() {
        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .queryParam("holdingProfileId", Data.HoldID)
                .queryParam("page", "1")
                .queryParam("size", "20")
                .queryParam("revert", true);

        CurrentSWP.Root response=  res.when().get("/core/investor/current-swps")
                .then().log().body().spec(respec).extract().response().as(CurrentSWP.Root.class);
        int size=response.getData().getSchemes().size();
        Swp_ID=response.getData().getSchemes().get(size-1).getSwpId();
        folio =response.getData().getSchemes().get(size-1).getFolio();
        System.out.println(Swp_ID);
    }
    @Test(priority = 1)
    public void Installment_dates_Edit() {
        Map<String,Object> payload=new HashMap<String,Object>();
        payload.put("installments",Integer.parseInt(Data.SWP_Installment));
        payload.put("frequency","monthly");
        payload.put("feature","SWP");
        payload.put("ecsDate",Integer.parseInt(Data.SWP_Date));

        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(payload);
        InstallmentDates.Root response=res.when().post("/core/calculators/installment-dates")
                .then().log().body().spec(respec).extract().response().as(InstallmentDates.Root.class);

        Edit_EcsDate=response.getData().getEcsDate();
        Edit_Installments=response.getData().getInstallments();
        sdate=response.getData().getStartDate();
        edate=response.getData().getEndDate();
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Edit_StartDate=df.format(sdate);
        Edit_EndDate = df.format(edate);
        System.out.println(Edit_StartDate);
        System.out.println(Edit_EndDate);
        System.out.println(Edit_EcsDate);
    }
    @Test(priority = 2)
    public void Common_OTP_Edit() {
        Map<String, Object> otppayload = new HashMap<String, Object>();
        otppayload.put("type", "mobile_and_email");
        otppayload.put("idType", "folio");
        otppayload.put("referenceId", folio);
        otppayload.put("workflow", "swp_edit");

        RequestSpecification commonotp = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(otppayload);
        CommonOTP.Root responce = commonotp.when().post("/core/investor/common/otp")
                .then().log().all().spec(respec).extract().response().as(CommonOTP.Root.class);
        otp_refid = responce.getData().getOtpReferenceId();
    }

    @Test(priority = 3)
    public void DB_Connection_Edit() throws SQLException {
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
    @Test(priority = 4)
    public void Verify_OTP_Edit() {
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

    @Test(priority = 5)
    public void SWP_Edit(){
        Map<String,Object> payload_Edit=new HashMap<String,Object>();
        payload_Edit.put("holdingProfileId",Data.HoldID);
        payload_Edit.put("swpId",Swp_ID);
        payload_Edit.put("change_installments",Edit_Installments);
        payload_Edit.put("change_amount",Integer.parseInt(Data.SWP_Amt));
        payload_Edit.put("change_swp_date",Edit_EcsDate);
        payload_Edit.put("changeStartDate",Edit_StartDate);
        payload_Edit.put("changeEndDate",Edit_EndDate);
        payload_Edit.put("otpReferenceId",DB_refid);

        RequestSpecification res = given().log().all().spec(req).baseUri(Data.BaseURL)
                .body(payload_Edit);
        res.when().put("/core/investor/current-swps")
                .then().log().body().spec(respec);
    }
}
