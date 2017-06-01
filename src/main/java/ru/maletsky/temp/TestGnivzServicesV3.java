package ru.maletsky.temp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestGnivzServicesV3 {
    private static final String host = "10.31.0.10",
            protocol = "http://",
            port = ":14000",
            serverUrl = protocol + host + port;

    private static final String VERSION = "/v3";
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String filenName = "/home/cybertron/Work/fls/fns_project/testing/v3/test_log_18_05_2017.txt";

    public static void main(String[] args) throws JSONException, InterruptedException, IOException {
        while (true) {
            println(filenName, "-------------------REGISTRATION-------------------");
            //rf
            String testOrgId = "152b1336-63b9-4059-935a-56af5204302f";


            String jsonResponse = postRegRf();
            String rfUserId = getStringValue(jsonResponse, "queryId");
            String orgId = getRegistrationCode(rfUserId);
            getRegRf();
            if (!orgId.isEmpty()) {
                getRegRfById(orgId);
            }
            println(filenName, "Fake orgId = 152b1336-63b9-4059-935a-56af5204302f");
            getRegRfById("152b1336-63b9-4059-935a-56af5204302f");

            //foreign_agent
            println(filenName, "-------------------FOREIGN_AGENT-------------------");
            jsonResponse = postRegForeignAgent();
            String foreignAUserId = getStringValue(jsonResponse, "queryId");
            orgId = getRegistrationCode(foreignAUserId);
            getRegForeignAgent();
            if (!orgId.isEmpty()) {
                getRegForeignAgentById(orgId);
            }
            println(filenName, "Fake orgId = 152b1336-63b9-4059-935a-56af5204302f");
            getRegForeignAgentById("152b1336-63b9-4059-935a-56af5204302f");


            //foreign

            println(filenName, "-------------------FOREIGN-------------------");
            jsonResponse = postRegForeign();
            String foreignUserId = getStringValue(jsonResponse, "queryId");
            orgId = getRegistrationCode(foreignUserId);
            getRegForeign();
            if (!orgId.isEmpty()) {
                getRegForeignById(orgId);
            }
            println(filenName, "Fake orgId = 152b1336-63b9-4059-935a-56af5204302f");
            getRegForeignById("152b1336-63b9-4059-935a-56af5204302f");


            //lp
           println(filenName, "-------LP-----------LP-----------LP------");
            jsonResponse = postRegLp(testOrgId);
            String lpId = getStringValue(jsonResponse, "queryId");
            orgId = getRegistrationCode(lpId);
            if (!orgId.isEmpty()) {
                getRegLpByUserId(orgId);
            }

            println(filenName, "------------KIZ----------KIZ----------KIZ----------");
            //KIZ
            int i;


            for (i = 11; i <= 65; i++) {
                jsonResponse = postBaseState(String.valueOf(i));
                if (jsonResponse.isEmpty()) continue;

                String baseStateId = getStringValue(jsonResponse, "queryId");
                runStatusRequestKiz(baseStateId);
            }


            for (i = 1; i <= 8; i++) {
                jsonResponse = postBaseStateLp("30" + String.valueOf(i));
                if (jsonResponse.isEmpty()) continue;

                String baseStateLpId = getStringValue(jsonResponse, "queryId");
                runStatusRequestKiz(baseStateLpId);
            }
            getKizState("12345678901234asdfghjklzxce");
            getKizHistory("12345678901234asdfghjklzxce");

            println(filenName, "---------------REESTR---------------");
            getFullReestr();
            getOrgReestr("5408130693");

            getFullGRIPReestr();
            getGRIPReestr("5408130693");

            getFullRAFPReestr();
            getRAFPReestr("5408130693");

            getFullDUESReestr();
            getDUESReestr("5408130693");

            getFullProdLicensesReestr();
            getProdLicensesReestr("4025075206");

            getFullPharmLicensesReestr();
            getPharmLicensesReestr("5408130693");

            getFullESKLPReestr();
            getESKLPReestr("ЛС-000612");

            getFullGS1Reestr();
            getOrgGS1Reestr("4600007000193");
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
        String jsonResponse;
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
            int code = getIntValue(jsonResponse, "code");
            switch (code) {
                case 0:
                case 1:
                    return getStringValue(jsonResponse, "registrationCode");
                case 2:
                    print(filenName, "code: 2. Указанная сущность не может быть идентифицирована (не зарегистрирована)\n");
                    println(filenName, " ");
                    return "";
                case 10:
                    print(filenName, "code: 10. Отсутствие сведений в ЕГРЮЛ / ЕГРИП\n");
                    println(filenName, " ");
                    return "";
                case 11:
                    print(filenName, "code: 11. Отсутствие сведений в государственном реестре аккредитованных филиалов\n");
                    println(filenName, " ");
                    return "";
                case 15:
                    print(filenName, "code: 15. Несоответствие ФИО данным реестров ЕГРЮЛ / ЕГРИП\n");
                    println(filenName, " ");
                    return "";
                case 16:
                    print(filenName, "code: 16. Несоответствие ФИО данным государственного реестра аккредитованных филиалов\n");
                    println(filenName, " ");
                    return "";
                case 20:
                    print(filenName, "code: 20. Отсутствие сведений о лицензии на осуществление деятельности\n");
                    println(filenName, " ");
                    return "";
                case 21:
                    print(filenName, "code: 21. Отсутствие сведений о лицензии на фармацевтическую деятельность\n");
                    println(filenName, " ");
                    return "";
                case 30:
                    print(filenName, "code: 30. Отсутствие сведений в Государственном реестре лекарственных средств Минздрава России\n");
                    println(filenName, " ");
                    return "";
                case 40:
                    print(filenName, "code: 40. Отсутствие оригиналов документов, подтверждающих право представления интересов иностранного держателя регистрационного удостоверения\n");
                    println(filenName, " ");
                    return "";
                case 50:
                    print(filenName, "code: 50. Несоответствие данных ЕСКЛП\n");
                    println(filenName, " ");
                    return "";
                case 60:
                    print(filenName, "code: 60. Ошибка сверки данных ЕСКЛП и GS1\n");
                    println(filenName, " ");
                    return "";
                case 100:
                    print(filenName, "code: 100. Частичное завершение\n");
                    println(filenName, " ");
                    return "";
            }
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
        final String resource = VERSION + "/registration/rf";

        try {
            JSONObject data = new JSONObject();
            data.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
            data.put("first_name", "Elon");
            data.put("middle_name", "Reeve");
            data.put("last_name", "Mask");
            data.put("inn", "1123456782");

            response = getPOSTResponse(resource, data);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegRf() throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/rf";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegRfById(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/rf/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    ///foreign_agent
    private static String postRegForeignAgent() throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/foreign_agent";

        try {
            JSONObject data = new JSONObject();
            data.put("op_date", "2018-04-18T16:42:13.3855742+03:00");
            data.put("first_name", "Elon2");
            data.put("middle_name", "Reeve2");
            data.put("last_name", "Mask2");
            data.put("inn", "1123456783");

            response = getPOSTResponse(resource, data);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeignAgent() throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/foreign_agent";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeignAgentById(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/foreign_agent/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    ///foreign
    private static String postRegForeign() throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/foreign";

        try {
            JSONObject data = new JSONObject();
            data.put("op_date", "2016-11-17T00:00:00Z");
            data.put("itin", "1123456782");
            data.put("regNum", "1113");
            data.put("regDate", "2017-03-03");

            response = getPOSTResponse(resource, data);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeign() throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/foreign";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegForeignById(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/foreign/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    ///lp
    private static String postRegLp(String orgId) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/lp";

        try {
            JSONObject root = new JSONObject();
            root.put("system_subj_id", "152b1336-63b9-4059-935a-56af5204302f");
            root.put("op_date", "2016-11-17T00:00:00Z");
            JSONArray listLpReg = new JSONArray();
            JSONObject first = new JSONObject();
            JSONObject second = new JSONObject();
            JSONObject third = new JSONObject();

            first.put("regDate", "2017-01-12");
            first.put("gtin", "23478347564653");
            first.put("regNum", "1113");
            second.put("regDate", "2017-01-12");
            second.put("gtin", "23478347564654");
            second.put("regNum", "1114");
            third.put("regDate", "2017-01-12");
            third.put("gtin", "23478347564655");
            third.put("regNum", "1115");
            listLpReg.put(first);
            listLpReg.put(second);
            listLpReg.put(third);
            root.put("lp_registration", listLpReg);
            response = getPOSTResponse(resource, root);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegLpByUserId(String lpId) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/lp/" + lpId;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRegStatus(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/status/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    private static String getRegResult(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/result/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    private static String getRegError(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/registration/error/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    private static JSONArray getKIZList(String state) throws JSONException {
        JSONArray listKiz = new JSONArray();
        JSONObject listKizObject1 = new JSONObject();
        JSONObject listKizObject2 = new JSONObject();
        JSONObject listKizObject3 = new JSONObject();
        listKizObject1.put("sign_type", 0);
        listKizObject1.put("sign", "12345678901234asdfghjklzxce");
        listKizObject2.put("sign_type", 0);
        listKizObject2.put("sign", "12345678901234asdfghjklzxcv");
        listKizObject3.put("sign_type", 0);
        listKizObject3.put("sign", "12345678901234asdfghjklzxcf");

        JSONObject meta1 = new JSONObject();
        JSONObject meta2 = new JSONObject();
        JSONObject meta3 = new JSONObject();

        switch (state) {
            case "11":
            case "12":
            case "17":
            case "18":
            case "22":
            case "23":
            case "24":
            case "25":
            case "28":
            case "52":
            case "61":
            case "62":
            case "63":
            case "64":
            case "301":
            case "308":
                listKiz.put(listKizObject1);
                listKiz.put(listKizObject2);
                listKiz.put(listKizObject3);
                return listKiz;
            case "31":
            case "32":
                meta1.put("hs_code", "1234");
                meta1.put("cost", 11122);
                listKizObject1.put("metadata", meta1);
                meta2.put("hs_code", "1235");
                meta2.put("cost", 11122);
                listKizObject2.put("metadata", meta2);
                meta3.put("hs_code", "1236");
                meta3.put("cost", 11122);
                listKizObject3.put("metadata", meta3);
                listKiz.put(listKizObject1);
                listKiz.put(listKizObject2);
                listKiz.put(listKizObject3);
                return listKiz;
            case "33":
                meta1.put("hs_code", "1234");
                listKizObject1.put("metadata", meta1);
                meta2.put("hs_code", "1235");
                listKizObject2.put("metadata", meta2);
                meta3.put("hs_code", "1236");
                listKizObject3.put("metadata", meta3);
                listKiz.put(listKizObject1);
                listKiz.put(listKizObject2);
                listKiz.put(listKizObject3);
                return listKiz;
            case "51":
                meta1.put("price", 11122);
                meta1.put("vat_value", 1112);
                meta1.put("date", "2017-01-12");
                meta1.put("hs_code", "1234");
                meta1.put("cash_doc_num", "12313");
                meta1.put("cash_doc_time", "2016-11-17T00:00:00Z");

                listKizObject1.put("metadata", meta1);
                meta2.put("price", 11122);
                meta2.put("vat_value", 1112);
                meta2.put("date", "2017-01-12");
                meta2.put("hs_code", "1234");
                meta2.put("cash_doc_num", "12312");
                meta2.put("cash_doc_time", "2016-11-17T00:00:00Z");
                listKizObject2.put("metadata", meta2);
                meta3.put("price", 11122);
                meta3.put("vat_value", 1112);
                meta3.put("date", "2017-01-12");
                meta3.put("hs_code", "1234");
                meta3.put("cash_doc_num", "12311");
                meta3.put("cash_doc_time", "2016-11-17T00:00:00Z");
                listKizObject3.put("metadata", meta3);
                listKiz.put(listKizObject1);
                listKiz.put(listKizObject2);
                listKiz.put(listKizObject3);
                return listKiz;
            case "65":
                meta1.put("sgtin_new", "12345678901234asdfgdfklzxce");
                listKizObject1.put("metadata", meta1);
                meta2.put("sgtin_new", "12765678901234asdfghjklzxce");
                listKizObject2.put("metadata", meta2);
                meta3.put("sgtin_new", "12345678912234asdfghjklzxce");
                listKizObject3.put("metadata", meta3);
                listKiz.put(listKizObject1);
                listKiz.put(listKizObject2);
                listKiz.put(listKizObject3);
                return listKiz;
            case "305":
                meta1.put("date", "2017-01-12");
                meta1.put("hs_code", "1234");
                meta1.put("prescription_num", "1234");
                meta1.put("prescription_time", "2017-01-12");
                listKizObject1.put("metadata", meta1);
                listKiz.put(listKizObject1);
                return listKiz;
            case "306":
                meta1.put("hs_code", "1234");
                meta1.put("use_date", "2017-01-12");
                JSONArray docs = new JSONArray();
                JSONObject doc = new JSONObject();
                doc.put("use_doc_type", "doctype");
                doc.put("use_doc_num", "docnum");
                doc.put("use_doc_time", "2017-01-12");
                meta1.put("docs", docs);
                listKizObject1.put("metadata", meta1);
                listKiz.put(listKizObject1);
                return listKiz;
            case "307":
                meta1.put("hs_code", "1234");
                meta1.put("reason", 1);
                listKizObject1.put("metadata", meta1);
                listKiz.put(listKizObject1);
                return listKiz;

            default: return null;
        }
    }

    ///KIZ
    private static String postBaseState(String state) throws IOException {
        String response = "";
        final String resource = VERSION + "/kiz/basestate/" + state;

        JSONObject root = new JSONObject();
        JSONObject properties;
        JSONObject packObject = new JSONObject();
        JSONObject address = new JSONObject();
        try {
            packObject.put("aoguid", "f19b2584-516c-48bc-ba86-22255abce068");
            packObject.put("houseguid", "db18804c-d62a-4845-9f25-46add04b25f9");
            packObject.put("room", "1020");

            address.put("country_code", "RU");
            address.put("postal_code", "123456");
            address.put("region", "178");
            address.put("city", "uiu");
            address.put("locality", "fgh");
            address.put("street", "asdf");
            address.put("house", "2");
            address.put("corpus", "3");
            address.put("litera", "a");
            address.put("room", "78");
            switch (state) {
                case "11":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("packing_address", packObject);
                    properties.put("order_type", 1);
                    properties.put("owner_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("hs_code", "1234");
                    properties.put("gtin", "23478347564655");
                    properties.put("batch", "1234");
                    properties.put("exp_date", "2017-01-12");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "12":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("control_address", packObject);
                    properties.put("doc_type", 1);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    properties.put("hs_code", "1234");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "17":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("owner_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("owner_address", packObject);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    properties.put("invoice_num", "159292");
                    properties.put("invoice_date", "2017-01-12");
                    properties.put("hs_code", "1234");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "18":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("owner_address", packObject);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    properties.put("hs_code", "1234");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "22":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("control_id", "520013bb-8bd3-4c34-991c-52696d78568a");
                    properties.put("control_address", address);
                    properties.put("hs_code", "1234");
                    properties.put("batch", "1234");
                    properties.put("exp_date", "2017-01-12");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "23":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("seller_id", "520013bb-8bd3-4c34-991c-52696d23568a");
                    properties.put("consumer_id", "520013bb-8aa3-4c34-991c-52696d23568a");

                    properties.put("shipper_address", address);
                    properties.put("invoice_num", "159292");
                    properties.put("invoice_date", "2017-01-12");
                    properties.put("cost", 123123);
                    properties.put("hs_code", "1234");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "24":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("seller_id", "520013bb-8bd3-4c34-991c-52696d23568a");
                    properties.put("shipper_address", address);
                    properties.put("warehouse_address", packObject);
                    properties.put("cost", 123123);
                    properties.put("hs_code", "1234");
                    properties.put("delivery_id", "520013bb-8aa1-4c34-991c-52696d23568a");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "25":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("control_type", 3);
                    properties.put("doc_type", 1);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    JSONObject gtdObject = new JSONObject();
                    gtdObject.put("customs_code", "asdfg");
                    gtdObject.put("reg_date", "2017-01-12");
                    gtdObject.put("reg_number", "123");
                    properties.put("gtd_info", gtdObject);
                    properties.put("hs_code", "1234");
                    properties.put("delivery_id", "520013bb-8aa1-4c34-991c-52696d23568a");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "28":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("importer_address", packObject);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    properties.put("hs_code", "1234");
                    properties.put("delivery_id", "520013bb-8aa1-4c34-991c-52696d23568a");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "31":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("accept_type", 0);
                    properties.put("shipment_type", 1);
                    properties.put("shipper_address", packObject);
                    properties.put("consignee_id", "520013bb-8aa1-4c34-991c-52696d235611");
                    properties.put("consignee_address", packObject);
                    properties.put("source_type", 1);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "32":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("receiving_type", 1);
                    properties.put("seller_id", "520013bb-8aa1-4c34-991c-52696d235611");
                    properties.put("buyer_address", packObject);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "33":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("shipper_address", packObject);
                    properties.put("consignee_address", packObject);
                    properties.put("storage_type", 1);
                    properties.put("doc_num", "1");
                    properties.put("doc_date", "2017-01-12");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "51":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("withdrawal_type", 1);
                    properties.put("subject_address", packObject);
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "52":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("withdrawal_type", 5);
                    properties.put("subject_address", packObject);
                    properties.put("doc_num", "12313");
                    properties.put("doc_date", "2017-01-12");
                    properties.put("hs_code", "1234");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "61":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    JSONObject aggAddress = new JSONObject();
                    aggAddress.put("address_type", 0);
                    aggAddress.put("fias_address", packObject);
                    aggAddress.put("address", address);
                    properties.put("aggregation_address", packObject);
                    properties.put("sscc", "as78d34dg32y2qw11h");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "62":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "63":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "64":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("sscc", "as78d34dg32y2qw11h");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "65":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78568e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
            }
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String postBaseStateLp(String state) throws IOException {
        String response = "";
        final String resource = VERSION + "/kiz/lp/" + state;

        JSONObject root = new JSONObject();
        JSONObject subjectAddress = new JSONObject();
        JSONObject destructionAddress = new JSONObject();
        JSONObject properties;
        try {
            subjectAddress.put("aoguid", "f19b2584-516c-48bc-ba86-22255abce068");
            subjectAddress.put("houseguid", "db18804c-d62a-4845-9f25-46add04b25f9");
            subjectAddress.put("room", "1020");
            destructionAddress.put("aoguid", "f19b2584-516c-48bc-ba86-22255abce069");
            destructionAddress.put("houseguid", "db18804c-d62a-4845-9f25-46add04b25f0");
            destructionAddress.put("room", "8c951b9e-14fb-4f64-ad0f-09ad4e857ea2");

            switch (state) {
                case "301":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52696d78569e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("control_type", 1);
                    properties.put("hs_code", "1234");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "304":
                    JSONObject object = new JSONObject();
                    object.put("halt_date", "2017-01-12");
                    object.put("halt_type", 0);
                    object.put("halt_reason", 0);
                    object.put("halt_doc_num", "1");
                    object.put("halt_doc_date", "2017-01-12");
                    object.put("reg_num", "12012017");
                    object.put("reg_date", "2017-01-12");
                    object.put("gtin", "23478347564655");
                    object.put("batch", "1234");
                    response = getPOSTResponse(resource, object);
                    return response;
                case "305":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52691d78569e");
                    properties.put("withdrawal_type", 2);
                    properties.put("subject_address", subjectAddress);
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "306":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52691d78569e");
                    properties.put("withdrawal_type", 3);
                    properties.put("subject_address", subjectAddress);
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "307":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52691d78569e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("withdrawal_type", 4);
                    properties.put("subject_address", subjectAddress);
                    JSONObject destruction = new JSONObject();
                    destruction.put("inn", "1123456782");
                    destruction.put("kpp", "123456789");
                    properties.put("destruction_org_id", destruction);
                    properties.put("destruction_address", subjectAddress);
                    properties.put("contract_num", 1);
                    properties.put("contract_date", "2017-01-12");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
                case "308":
                    properties = new JSONObject();
                    properties.put("system_subj_id", "520013bb-8bd3-4c34-991c-52691d78569e");
                    properties.put("op_date", "2016-11-17T00:00:00Z");
                    properties.put("destruction_method", 1);
                    properties.put("subject_address", subjectAddress);
                    JSONObject destruction308 = new JSONObject();
                    destruction308.put("inn", "1123456782");
                    destruction308.put("kpp", "123456789");
                    properties.put("destruction_org_id", destruction308);
                    properties.put("act_num", 1);
                    properties.put("act_date", "2017-01-12");
                    root.put("kizs", getKIZList(state));
                    root.put("properties", properties);
                    response = getPOSTResponse(resource, root);
                    return response;
            }
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getKizState(String kizId) throws IOException {
        String response = "";
        final String resource = VERSION + "/kiz/" + kizId;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    private static String getKizHistory(String kizId) throws IOException {
        String response = "";
        final String resource = VERSION + "/kiz/" + kizId + "/history";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    //KIZ status/result/error
    private static String getKizStatus(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/kiz/status/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    private static String getKizResult(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/kiz/result/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }

    private static String getKizError(String id) throws IOException {
        String response = "";
        final String resource = VERSION + "/kiz/error/" + id;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response.toString();
    }


    //////////REESTR
    private static String getFullReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/egrul";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getOrgReestr(String regNum) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/egrul/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getGRIPReestr(String regNum) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/egrip/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getFullGRIPReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/egrip";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getRAFPReestr(String regNum) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/rafp/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getFullRAFPReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/rafp";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getDUESReestr(String regNum) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/dues/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getFullDUESReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/dues";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getProdLicensesReestr(String regNum) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/prod_licenses/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getFullProdLicensesReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/prod_licenses";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getPharmLicensesReestr(String regNum) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/pharm_licenses/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getFullPharmLicensesReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/pharm_licenses";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getESKLPReestr(String regNum) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/esklp/" + regNum;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getFullESKLPReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/esklp";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getFullGS1Reestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/gs1";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }



    private static String getOrgGS1Reestr(String gtin) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/gs1/" + gtin;

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getAllLisenceReestr() throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/licence";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
        }

        return response;
    }

    private static String getLisenceReestr(String inn) throws IOException {
        String response = "";
        final String resource = VERSION + "/reestr/licence";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(filenName, "Internal error\n\n");
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

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ss:SSS");

        println(filenName, "Date: " + dateFormat.format(date));
        println(filenName, "REQUEST");
        println(filenName, connection.getRequestMethod() + " " + resource + " HTTP/1.1");
        if (method.equals(POST)) {
            println(filenName, "BODY");
            println(filenName, data);
        }

        long startTime = System.currentTimeMillis();
        connection.connect();
        println(filenName, "RESPONSE");
        try {
            println(filenName, "Code: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            if (connection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                elapsedTime = System.currentTimeMillis() - startTime;
                println(filenName, "Elapsed time: " + elapsedTime + "ms");

                String inputLine;
                response = new StringBuffer();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }

                bufferedReader.close();

                String jsonResponseMessage = response.toString();
                if (jsonResponseMessage.contains("\\u") && jsonResponseMessage.contains("errorDescription")) {
                    jsonResponseMessage = getStringValue(jsonResponseMessage, "errorDescription");
                    jsonResponseMessage = "{\"errorDescription\": " + jsonResponseMessage + "}";
                }

                println(filenName, "Response message: " + jsonResponseMessage + "\n\n");
            } else {
                println(filenName, "\n\n");
            }
        } catch (IOException e) {
            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= 5000) {
                println(filenName, "Request timeout: " + elapsedTime + "ms\n\n");
            } else {
                println(filenName, "Internal error\n\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private static void writeln(String fileName, String text) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            try {
                out.println(text);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(String fileName, String text) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            try {
                out.print(text);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void print(String fileName, String newText) throws IOException {
        exists(fileName);
        StringBuilder sb = new StringBuilder();
        String oldFile = read(fileName);
        sb.append(oldFile);
        sb.append(newText);
        write(fileName, sb.toString());
    }

    private static void println(String fileName, String newText) throws IOException {
        exists(fileName);
        StringBuilder sb = new StringBuilder();
        String oldFile = read(fileName);
        sb.append(oldFile);
        sb.append(newText);
        writeln(fileName, sb.toString());
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    private static void exists(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }
}
