package ru.maletsky.temp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestGnivzServicesV2 {
    private static final String host = "10.31.0.10",
            protocol = "http://",
            port = ":14000",
            serverUrl = protocol + host + port;

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String filenName = "/home/sergey/Documents/work/Projects/FNS_Audit/testing/reports/test_log_24_04_2017_evening.txt";

    public static void main(String[] args) throws JSONException, InterruptedException, IOException {
        while (true) {
            println(filenName, "-------------------REGISTRATION-------------------");
            //rf

            String jsonResponse = postRegRf();
            String rfUserId = getStringValue(jsonResponse, "queryId");
            String orgId = getRegistrationCode(rfUserId);
            getRegRf();
            getRegRfById(orgId);

            //foreign_agent
            println(filenName, "-------------------FOREIGN_AGENT-------------------");
            jsonResponse = postRegForeignAgent();
            String foreignAUserId = getStringValue(jsonResponse, "queryId");
            orgId = getRegistrationCode(foreignAUserId);
            getRegForeignAgent();
            getRegForeignAgentById(orgId);


            //foreign
            println(filenName, "-------------------FOREIGN-------------------");
            jsonResponse = postRegForeign();
            String foreignUserId = getStringValue(jsonResponse, "queryId");
            orgId = getRegistrationCode(foreignUserId);
            getRegForeign();
            getRegForeignById(orgId);

            //lp
            println(filenName, "-------LP-----------LP-----------LP------");
            jsonResponse = postRegLp(rfUserId);
            String lpId = getStringValue(jsonResponse, "queryId");
            orgId = getRegistrationCode(lpId);
            getRegLpByUserId(orgId);

            println(filenName, "------------KIZ----------KIZ----------KIZ----------");
            //KIZ
            int i;


            for (i = 1; i <= 11; i++) {
                jsonResponse = postBaseState(String.valueOf(i));
                String baseStateId = getStringValue(jsonResponse, "queryId");
                runStatusRequestKiz(baseStateId);
            }


            for (i = 1; i <= 8; i++) {
                jsonResponse = postBaseStateLp("30" + String.valueOf(i));
                String baseStateLpId = getStringValue(jsonResponse, "queryId");
                runStatusRequestKiz(baseStateLpId);
            }
            getKizState("00000000553231");
            getKizHistory("00000000553231");

/*            println(filenName, "---------------REESTR---------------");
            String jsonResponse = getFullReestr();
            getOrgReestr("");

            jsonResponse = getFullGS1Reestr();
            getOrgGS1Reestr("");

            jsonResponse = getAllLisenceReestr();
            getLisenceReestr("");*/
        }
    }

    private static String getStringValue(String jsonResponse, String key) throws JSONException {
        String value = "";
        if (!jsonResponse.isEmpty()) {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            value = jsonObject.getString(key);
        }
        return value;
    }

    private static int getIntValue(String jsonResponse, String key) throws JSONException {
        int value = 0;
        if (!jsonResponse.isEmpty()) {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            value = jsonObject.getInt(key);
        }

        return value;
    }

    private static String getRegistrationCode(String queryId) throws JSONException, InterruptedException, IOException {
        String jsonResponse = "";
        String registrationCode = "";
        int status = 1;
        int countStatus = 0;
        while (status == 1 && countStatus < 5) {
            jsonResponse = getRegStatus(queryId);
            status = getIntValue(jsonResponse, "status");
            countStatus++;
            Thread.currentThread().sleep(1000);
            if (countStatus == 4) {
                println(filenName, "Core doesn't respond. Operation status is 1 always (tried 5 time every 1 second).");
            }
        }

        if (status == 2) {
            jsonResponse = getRegResult(queryId);
            return getStringValue(jsonResponse, "registrationCode");
        } else {
            getRegError(queryId);
        }

        return "";
    }

    private static void runStatusRequestKiz(String queryId) throws JSONException, InterruptedException, IOException {
        if (queryId.isEmpty()) {
            return;
        }
        String jsonResponse = "";
        int status = 1;
        int countStatus = 0;
        while (status == 1 && countStatus < 5) {
            jsonResponse = getKizStatus(queryId);
            status = getIntValue(jsonResponse, "status");
            countStatus++;
            Thread.currentThread().sleep(1000);

            if (countStatus == 4) {
                println(filenName, "Core doesn't respond. Operation status is 1 always (tried 5 time every 1 second).");
            }

            if (status == 2) {
                getKizResult(queryId);
            } else {
                getKizError(queryId);
            }
        }
    }

    private static String postRegRf() throws IOException {
        String response = "";
        final String resource = "/v2/registration/rf";

        try {
            JSONObject data = new JSONObject();
            data.put("name", "Mask");
            data.put("type", 1);
            data.put("inn", "1123456782");
            data.put("kpp", "012345678");
            data.put("regNum", "asdf");
            data.put("regDate", 1122334455);
            response = getPOSTResponse(resource, data);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegRf() throws IOException {
        String response = "";
        final String resource = "/v2/registration/rf";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegRfById(String id) throws IOException {
        String response = "";
        final String resource = "/v2/registration/rf/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    ///foreign_agent
    private static String postRegForeignAgent() throws IOException {
        String response = "";
        final String resource = "/v2/registration/foreign_agent";

        try {
            JSONObject root = new JSONObject();
            JSONArray listLpReg = new JSONArray();
            JSONObject first = new JSONObject();
            JSONObject second = new JSONObject();
            JSONObject third = new JSONObject();
            first.put("regNum", "1111");
            first.put("regDate", 11223344);
            second.put("regNum", "1112");
            second.put("regDate", 11223345);
            third.put("regNum", "1113");
            third.put("regDate", 11223346);
            listLpReg.put(first);
            listLpReg.put(second);
            listLpReg.put(third);
            root.put("name", "Mask");
            root.put("type", 1);
            root.put("inn", "1123452342");
            root.put("kpp", "012345678");
            root.put("regNum", "asdf");
            root.put("regDate", 1122334455);
            root.put("lp_registration", listLpReg);
            response = getPOSTResponse(resource, root);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeignAgent() throws IOException {
        String response = "";
        final String resource = "/v2/registration/foreign_agent";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeignAgentById(String id) throws IOException {
        String response = "";
        final String resource = "/v2/registration/foreign_agent/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    ///foreign
    private static String postRegForeign() throws IOException {
        String response = "";
        final String resource = "/v2/registration/foreign";

        try {
            JSONObject root = new JSONObject();
            JSONArray listLpReg = new JSONArray();
            JSONObject first = new JSONObject();
            JSONObject second = new JSONObject();
            JSONObject third = new JSONObject();
            first.put("regNum", "1111");
            first.put("regDate", 1122334455);
            second.put("regNum", "1112");
            second.put("regDate", 1122334456);
            third.put("regNum", "1113");
            third.put("regDate", 1122334457);
            listLpReg.put(first);
            listLpReg.put(second);
            listLpReg.put(third);
            root.put("name", "Mask2");
            root.put("directorName", "aq23456789");
            root.put("phone", "+79111111111");
            root.put("email", "asdf@mas.com");
            root.put("attorneyDocs", "100100");
            root.put("lp_registration", listLpReg);
            response = getPOSTResponse(resource, root);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeign() throws IOException {
        String response = "";
        final String resource = "/v2/registration/foreign";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeignById(String id) throws IOException {
        String response = "";
        final String resource = "/v2/registration/foreign/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    ///lp
    private static String postRegLp(String id) throws IOException {
        String response = "";
        final String resource = "/v2/registration/" + id + "/lp";

        try {
            JSONObject root = new JSONObject();
            JSONArray listLpReg = new JSONArray();
            JSONObject first = new JSONObject();
            JSONObject second = new JSONObject();
            JSONObject third = new JSONObject();
            first.put("regDate", 1122334455);
            first.put("gtin", "GTIN1");
            first.put("regNum", "RegNum1");
            second.put("regDate", 1122334456);
            second.put("gtin", "GTIN2");
            second.put("regNum", "RegNum2");
            third.put("regDate", 1122334457);
            third.put("gtin", "GTIN3");
            third.put("regNum", "RegNum3");
            listLpReg.put(first);
            listLpReg.put(second);
            listLpReg.put(third);
            root.put("lp_registration", listLpReg);
            response = getPOSTResponse(resource, root);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegLpByUserId(String userid) throws IOException {
        String response = "";
        final String resource = "/v2/registration/lp/" + userid;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getRegStatus(String id) throws IOException {
        String response = "";
        final String resource = "/v2/registration/status/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    private static String getRegResult(String id) throws IOException {
        String response = "";
        final String resource = "/v2/registration/result/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    private static String getRegError(String id) throws IOException {
        String response = "";
        final String resource = "/v2/registration/error/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    ///KIZ
    private static String postBaseState(String state) throws IOException {
        String response = "";
        final String resource = "/v2/kiz/basestate/" + state;

        JSONObject root = new JSONObject();
        JSONArray listKiz = new JSONArray();
        JSONObject properties;
        JSONObject packObject = new JSONObject();
        JSONObject packWareObject = new JSONObject();
        try {
            listKiz.put("00000000553233");
            listKiz.put("00000000553232");
            listKiz.put("00000000553231");
            packObject.put("aoguid", "f19b2584-516c-48bc-ba86-22255abce068");
            packObject.put("houseguid", "db18804c-d62a-4845-9f25-46add04b25f9");
            packObject.put("room", "1020");
            packWareObject.put("aoguid", "s237824gkhjls237824gkhjls237824g34jl");
            packWareObject.put("houseguid", "h237824gkhjlh237824gkhjlh2378hggkhjl");
            packWareObject.put("room", "78");
            switch (state) {
                case "1":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004083");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("packing_address", packObject);
                    properties.put("order_type", 1);
                    properties.put("owner_id", "00000000003774");
                    properties.put("batch", "4c95bdda-8e1f-4c32-b");
                    properties.put("exp_date", "2017-04-18T16:42:13.3865743+03:00");
                    properties.put("gtin", "00000000004055");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "2":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004084");
                    properties.put("op_date", "2017-04-18T16:43:13.3855742+03:00");
                    properties.put("control_address", packObject);
                    properties.put("doc_type", 1);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("gtin", "00000000004056");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "3":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004085");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("seller_id", "12312312312312");
                    properties.put("consumer_id", "12312312312312");
                    properties.put("shipper_address", packObject);
                    properties.put("invoice_num", 159292);
                    properties.put("invoice_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("cost", 15929);
                    properties.put("gtin", "00000000004057");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "4":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004086");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("seller_id", "12312312312312");
                    properties.put("shipper_address", packObject);
                    properties.put("warehouse_address", packWareObject);
                    properties.put("cost", 15929);
                    properties.put("gtin", "00000000004058");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "5":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004087");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("control_type", 3);
                    properties.put("doc_type", 1);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("gtd_info", "tgjh23478");
                    properties.put("gtin", "00000000004059");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "6":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004088");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("importer_address", packObject);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("gtin", "00000000004060");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "7":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004089");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("owner_id", "00000000003774");
                    properties.put("owner_address", packObject);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("invoice_num", 15929);
                    properties.put("invoice_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("gtin", "00000000004061");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "8":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004090");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("receiving_type", 1);
                    properties.put("seller_id", "12312312312312");
                    properties.put("buyer_address", packObject);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("cost", 15929);
                    properties.put("gtin", "00000000004062");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "9":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004091");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("shipment_type", 1);
                    properties.put("shipper_address", packObject);
                    properties.put("consignee_id", "00000000004001");
                    properties.put("consignee_address", packWareObject);
                    properties.put("source_type", 1);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("cost", 15929);
                    properties.put("gtin", "00000000004063");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "10":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004092");
                    properties.put("withdrawal_type", 1);
                    properties.put("subject_address", packObject);
                    properties.put("op_date", "0001-01-01T00:00:00Z");

                    JSONArray saleArray = new JSONArray();
                    JSONArray docsArray = new JSONArray();
                    JSONObject saleObject = new JSONObject();
                    JSONObject docsObject = new JSONObject();
                    docsObject.put("cash_doc_num", 123);
                    docsObject.put("cash_doc_time", "2017-04-18T16:42:13.3855742+03:00");
                    docsArray.put(docsObject);

                    saleObject.put("sign", "00000000054191");
                    saleObject.put("price", 15929);
                    saleObject.put("date", "2017-04-18T16:42:13.3855742+03:00");
                    saleObject.put("docs", docsArray);
                    saleArray.put(saleObject);
                    properties.put("sales", saleArray);
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                default:
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004093");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("withdrawal_type", 1);
                    properties.put("subject_address", packObject);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("gtin", "00000000004064");
                    root.put("kizs", listKiz);
                    root.put("properties", properties);
                    response = getPOSTResponse("/v2/kiz/basestate/100", root);
                    return response;
            }
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String postBaseStateLp(String state) throws IOException {
        String response = "";
        final String resource = "/v2/kiz/lp/" + state;

        JSONObject root = new JSONObject();
        JSONArray listKiz = new JSONArray();
        JSONObject subjectAddress = new JSONObject();
        JSONObject destructionAddress = new JSONObject();
        JSONObject properties;
        try {
            listKiz.put("00000000553233");
            listKiz.put("00000000553232");
            listKiz.put("00000000553231");
            subjectAddress.put("aoguid", "f19b2584-516c-48bc-ba86-22255abce068");
            subjectAddress.put("houseguid", "db18804c-d62a-4845-9f25-46add04b25f9");
            subjectAddress.put("room", "8c951b9e-14fb-4f64-ad0f-09ad4e857ea3");
            destructionAddress.put("aoguid", "f19b2584-516c-48bc-ba86-22255abce069");
            destructionAddress.put("houseguid", "db18804c-d62a-4845-9f25-46add04b25f0");
            destructionAddress.put("room", "8c951b9e-14fb-4f64-ad0f-09ad4e857ea2");
            root.put("kizs", listKiz);

            switch (state) {
                case "301":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004188");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("control_type", 2);
                    properties.put("gtin", "00000000004162");
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "302":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004189");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("control_type", 1);
                    properties.put("gtin", "00000000004163");
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "303":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004190");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("control_type", 1);
                    properties.put("doc_type", 1);
                    properties.put("doc_num", 122);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("gtin", "00000000004164");
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "304":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004191");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("withdrawal_type", 1);
                    properties.put("subject_address", subjectAddress);
                    properties.put("cost", 15929);

                    JSONArray saleArray = new JSONArray();
                    JSONArray docsArray = new JSONArray();
                    JSONObject saleObject = new JSONObject();
                    JSONObject docsObject = new JSONObject();
                    docsObject.put("cash_doc_num", 123);
                    docsObject.put("cash_doc_time", "2017-04-18T16:42:13.3855742+03:00");
                    docsArray.put(docsObject);

                    saleObject.put("sign","00000000054191");
                    saleObject.put("price", 15929);
                    //saleObject.put("vat_value", 16.12);
                    saleObject.put("date", "2017-04-18T16:42:13.3855742+03:00");
                    saleObject.put("docs", docsArray);
                    saleArray.put(saleObject);
                    properties.put("sales", saleArray);
                    properties.put("doc_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("gtin", "00000000004165");
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "305":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004192");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("withdrawal_type", 1);
                    properties.put("subject_address", subjectAddress);

                    saleArray = new JSONArray();
                    docsArray = new JSONArray();
                    saleObject = new JSONObject();
                    docsObject = new JSONObject();
                    docsObject.put("prescription_num", 123);
                    docsObject.put("prescription_time", "2017-04-18T16:42:13.3855742+03:00");
                    docsArray.put(docsObject);

                    saleObject.put("sign","00000000054192");
                    saleObject.put("date", "2017-04-18T16:42:13.3855742+03:00");
                    saleObject.put("docs", docsArray);
                    saleArray.put(saleObject);

                    properties.put("uses", saleArray);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "306":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004193");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("withdrawal_type", 1);
                    properties.put("subject_address", subjectAddress);

                    saleArray = new JSONArray();
                    docsArray = new JSONArray();
                    saleObject = new JSONObject();
                    docsObject = new JSONObject();
                    docsObject.put("use_doc_type", "1231312312123");
                    docsObject.put("use_doc_num", 123);
                    docsObject.put("use_doc_time", "2017-04-18T16:42:13.3855742+03:00");
                    docsArray.put(docsObject);

                    saleObject.put("sign","00000000054193");
                    saleObject.put("date", "2017-04-18T16:42:13.3855742+03:00");
                    saleObject.put("docs", docsArray);
                    saleArray.put(saleObject);

                    properties.put("uses", saleArray);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "307":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004194");
                    properties.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("withdrawal_type", 1);
                    properties.put("subject_address", subjectAddress);

                    JSONObject destruction_org = new JSONObject();
                    JSONObject ul = new JSONObject();
                    ul.put("inn", "1234567890");
                    ul.put("kpp", "123456789");
                    destruction_org.put("ul", ul);
                    destruction_org.put("fl", "123456789012");

                    properties.put("destruction_org_id", destruction_org);
                    properties.put("destruction_address", destructionAddress);
                    properties.put("contract_num", 1234);
                    properties.put("contract_date", "2017-03-18T16:42:13.3855742+03:00");

                    saleArray = new JSONArray();
                    saleObject = new JSONObject();

                    saleObject.put("sign","00000000054199");
                    saleObject.put("reason", 1);
                    saleArray.put(saleObject);

                    properties.put("transfers", saleArray);
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "308":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "00000000004195");
                    properties.put("op_date", "2017-04-18T12:42:13.3865743+03:00");
                    properties.put("destruction_method", 1);
                    properties.put("subject_address", subjectAddress);

                    destruction_org = new JSONObject();
                    ul = new JSONObject();
                    ul.put("inn", "1234567890");
                    ul.put("kpp", "123456789");
                    destruction_org.put("ul", ul);
                    destruction_org.put("fl", "123456789012");
                    properties.put("destruction_org_id", destruction_org);
                    properties.put("act_num", 159);
                    properties.put("act_date", "2017-04-18T16:42:13.3855742+03:00");
                    properties.put("gtin", "00000000014189");
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                default:
                    throw new Exception("Incorrect state value; state = " + state);
            }
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getKizState(String kizId) throws IOException {
        String response = "";
        final String resource = "/v2/kiz/" + kizId;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    private static String getKizHistory(String kizId) throws IOException {
        String response = "";
        final String resource = "/v2/kiz/" + kizId + "/history";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    //KIZ status/result/error
    private static String getKizStatus(String id) throws IOException {
        String response = "";
        final String resource = "/v2/kiz/status/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    private static String getKizResult(String id) throws IOException {
        String response = "";
        final String resource = "/v2/kiz/result/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }

    private static String getKizError(String id) throws IOException {
        String response = "";
        final String resource = "/v2/kiz/error/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response.toString();
    }


    //////////REESTR
    private static String getFullReestr() throws IOException {
        String response = "";
        final String resource = "/v2/reestr/grls";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getOrgReestr(String regNum) throws IOException {
        String response = "";
        final String resource = "/v2/reestr/grls/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getFullGS1Reestr() throws IOException {
        String response = "";
        final String resource = "/v2/reestr/gs1";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getOrgGS1Reestr(String gtin) throws IOException {
        String response = "";
        final String resource = "/v2/reestr/gs1/" + gtin;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getAllLisenceReestr() throws IOException {
        String response = "";
        final String resource = "/v2/reestr/licence";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getLisenceReestr(String inn) throws IOException {
        String response = "";
        final String resource = "/v2/reestr/licence";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName,"Internal error\n\n");
        }

        return response;
    }

    private static String getGETResponse(String resource) throws IOException {
        HttpURLConnection connection = getConnection(GET, resource);

        return getResponse(connection, resource, new String(), GET);
    }

    private static String getPOSTResponse(String resource, JSONObject data) throws IOException {
        HttpURLConnection connection = getConnection(POST, resource);

        OutputStream os = connection.getOutputStream();
        os.write(data.toString().getBytes("UTF-8"));
        os.close();

        return getResponse(connection, resource, data.toString(), POST);
    }

    private static HttpURLConnection getConnection(String method, String resource) throws IOException {
        URL url = new URL(serverUrl + resource);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod(method);

        if (method.equals(POST)) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
        }

        return connection;
    }

    private static String getResponse(HttpURLConnection connection, String resource, String data, String method) throws IOException {
        StringBuffer response = new StringBuffer();
        long elapsedTime = 0;

        Date date = new Date( );
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ss:SSS");

        println(filenName,"Date: " + dateFormat.format(date));
        println(filenName,"REQUEST");
        println(filenName,connection.getRequestMethod() + " " + resource + " HTTP/1.1");
        if(method.equals(POST)) {
            println(filenName, "BODY");
            println(filenName, data);
        }

        long startTime = System.currentTimeMillis();
        connection.connect();
        println(filenName,"RESPONSE");
        try {
            println(filenName,"Code: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            if (connection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                elapsedTime = System.currentTimeMillis() - startTime;
                println(filenName,"Elapsed time: " + elapsedTime + "ms");

                String inputLine;
                response = new StringBuffer();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }

                bufferedReader.close();

                println(filenName,"Response message: " + response.toString() + "\n\n");
            } else {
                println(filenName, "\n\n"); 
            }
        } catch (IOException e) {
            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= 5000) {
                println(filenName,"Request timeout: " + elapsedTime + "ms\n\n");
            } else {
                println(filenName,"Internal error\n\n");
            }
        }

        return response.toString();
    }

    private static void write(String fileName, String text) {
        File file = new File(fileName);
        try {
            if(!file.exists()){
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            try {
                out.println(text);
            } finally {
                out.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void println(String fileName, String newText) throws IOException {
        exists(fileName);
        StringBuilder sb = new StringBuilder();
        String oldFile = read(fileName);
        sb.append(oldFile);
        sb.append(newText);
        write(fileName, sb.toString());
    }

    private static String read(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        exists(fileName);
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    private static void exists(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()){
            file.createNewFile();
        }
    }
}
