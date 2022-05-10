package com.myorg;

import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.DockerVolume;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigatewayv2.alpha.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.alpha.PayloadFormatVersion;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegrationProps;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static software.amazon.awscdk.BundlingOutput.ARCHIVED;

public class TwilioJavaHelperLambdaStack extends Stack {
    public TwilioJavaHelperLambdaStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public TwilioJavaHelperLambdaStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        List<String> sendSmsFnPackagingInstructions = Arrays.asList(
                "/bin/sh",
                "-c",
                "cd twilio-java-helper-send-sms " +
                        "&& mvn clean install " +
                        "&& cp /asset-input/twilio-java-helper-send-sms/target/twilio-java-helper-send-sms.jar /asset-output/"
        );

        BundlingOptions.Builder builderOptions = BundlingOptions.builder()
                .command(sendSmsFnPackagingInstructions)
                .image(Runtime.JAVA_11.getBundlingImage())
                .volumes(singletonList(
                        // Mount local .m2 repo to avoid download all the dependencies again inside the container
                        DockerVolume.builder()
                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                .containerPath("/root/.m2/")
                                .build()
                ))
                .user("root")
                .outputType(ARCHIVED);

        HashMap<String, String> environmentMap = new HashMap<>();
        environmentMap.put("TWILIO_ACCOUNT_SID", System.getenv("TWILIO_ACCOUNT_SID"));
        environmentMap.put("TWILIO_AUTH_TOKEN", System.getenv("TWILIO_AUTH_TOKEN"));

        Function sendSmsFunction = new Function(this, "TwilioJavaHelperSendSms", FunctionProps.builder()
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("lambda/", AssetOptions.builder()
                        .bundling(builderOptions
                                .command(sendSmsFnPackagingInstructions)
                                .build())
                        .build()))
                .handler("com.maskaravivek.TwilioJavaHelperSendSmsHandler")
                .memorySize(1024)
                .timeout(Duration.minutes(1))
                .environment(environmentMap)
                .logRetention(RetentionDays.ONE_WEEK)
                .build());

        HttpApi httpApi = new HttpApi(this, "twilio-java-helper-demo", HttpApiProps.builder()
                .apiName("twilio-java-helper-demo")
                .build());

        HttpLambdaIntegration httpLambdaIntegration = new HttpLambdaIntegration(
                "this",
                sendSmsFunction,
                HttpLambdaIntegrationProps.builder()
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                        .build()
        );

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/sendSms")
                .methods(singletonList(HttpMethod.POST))
                .integration(httpLambdaIntegration)
                .build()
        );

        new CfnOutput(this, "HttApi", CfnOutputProps.builder()
                .description("Url for Http Api")
                .value(httpApi.getApiEndpoint())
                .build());
    }
}
