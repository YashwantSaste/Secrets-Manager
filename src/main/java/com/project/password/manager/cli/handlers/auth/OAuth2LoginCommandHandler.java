package com.project.password.manager.cli.handlers.auth;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

import com.google.inject.Inject;
import com.project.password.manager.auth.oauth2.DeviceCode;
import com.project.password.manager.auth.oauth2.OAuth2Session;
import com.project.password.manager.cli.commands.auth.OAuth2LoginCommand;
import com.project.password.manager.cli.handlers.CommandHandler;
import com.project.password.manager.cli.runtime.CliOutput;
import com.project.password.manager.cli.runtime.CliSession;
import com.project.password.manager.cli.runtime.CliTheme;
import com.project.password.manager.service.OAuth2LoginService;
import com.project.password.manager.util.BrowserUtils;
import com.project.password.manager.util.ValidationUtils;

public class OAuth2LoginCommandHandler implements CommandHandler<OAuth2LoginCommand.Request> {

    @NotNull
    private final OAuth2LoginService oauth2LoginService;
    @NotNull
    private final CliSession session;
    @NotNull
    private final CliOutput output;

    @Inject
    public OAuth2LoginCommandHandler(@NotNull OAuth2LoginService oauth2LoginService, @NotNull CliSession session, @NotNull CliOutput output) {
        this.oauth2LoginService = oauth2LoginService;
        this.session = session;
        this.output = output;
    }

    @Override
    public void handle(@NotNull OAuth2LoginCommand.Request request) {
        DeviceCode deviceCode = oauth2LoginService.initiateLogin();
        String launchUrl = resolveLaunchUrl(deviceCode);
        boolean browserOpened = BrowserUtils.openUrl(launchUrl);
        output.info(CliTheme.infoPanel("OAuth2 Device Login",
                resolveInstructionLine(deviceCode, browserOpened),
                CliTheme.key("verification url") + CliTheme.muted(" : ")
                        + CliTheme.secondary(resolveOrPlaceholder(launchUrl, "not provided")),
                CliTheme.key("user code") + CliTheme.muted(" : ")
                        + CliTheme.accent(resolveOrPlaceholder(deviceCode.getUserCode(), "not provided")),
                CliTheme.key("status") + CliTheme.muted(" : ") + CliTheme.secondary("waiting for provider approval")));
        emitCopyableInstructions(deviceCode, launchUrl, browserOpened);
        OAuth2Session oauth2Session = oauth2LoginService.login(deviceCode);
        session.open(oauth2Session.getUser().getId(), oauth2Session.getCliToken());
        output.info("Logged in as " + oauth2Session.getUser().getId() + " via OAuth2");
    }

    @NotNull
    private String resolveInstructionLine(@NotNull DeviceCode deviceCode, boolean browserOpened) {
        String message = deviceCode.getMessage();
        if (ValidationUtils.hasText(message)) {
            return CliTheme.secondary(Objects.requireNonNull(message));
        }
        if (browserOpened) {
            return CliTheme.secondary("Opened the provider login page in your browser. Approve access, then return to the CLI.");
        }
        return CliTheme.secondary("Complete the device authorization in your browser, then return to the CLI.");
    }

    private void emitCopyableInstructions(@NotNull DeviceCode deviceCode, String launchUrl, boolean browserOpened) {
        if (ValidationUtils.hasText(launchUrl)) {
            output.info((browserOpened ? "Opened browser: " : "Open this URL: ") + launchUrl);
        }
        String userCode = deviceCode.getUserCode();
        if (ValidationUtils.hasText(userCode)) {
            output.info("OAuth2 code: " + userCode);
            output.info("Copy the code and paste it for IDP to confirm");
        }
    }

    @NotNull
    private String resolveLaunchUrl(@NotNull DeviceCode deviceCode) {
        String verificationUrlComplete = deviceCode.getVerificationUrlComplete();
        if (ValidationUtils.hasText(verificationUrlComplete)) {
            return Objects.requireNonNull(verificationUrlComplete);
        }
        return resolveOrPlaceholder(deviceCode.getVerificationUrl(), "");
    }

    @NotNull
    private String resolveOrPlaceholder(String value, @NotNull String placeholder) {
        if (!ValidationUtils.hasText(value)) {
            return placeholder;
        }
        return value;
    }
}
