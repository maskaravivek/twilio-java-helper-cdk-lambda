package com.maskaravivek;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.amazonaws.services.lambda.runtime.Context;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.HashMap;

public class TwilioJavaHelperSendSmsHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
        final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
        context.getLogger().log("Input: " + event.getBody());

        SendSmsRequest sendSmsRequest = gson.fromJson(event.getBody(), SendSmsRequest.class);

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(new PhoneNumber(sendSmsRequest.getPhoneNumber()),
                new PhoneNumber("+15017250604"),
                sendSmsRequest.getMessage()).create();

        System.out.println(message.getSid());

        SendSmsResponse sendSmsResponse = new SendSmsResponse(message.getSid());

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        response.setBody(gson.toJson(sendSmsResponse));
        return response;
    }
}
