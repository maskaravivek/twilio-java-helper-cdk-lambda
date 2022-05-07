package com.maskaravivek;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class TwilioJavaHelperLambdaApp {
    public static void main(final String[] args) {
        App app = new App();

        new TwilioJavaHelperLambdaStack(app, "TwilioJavaHelperLambdaStack", StackProps.builder()
                .build());

        app.synth();
    }
}

