package com.project.password.manager;

import com.project.password.manager.cli.CLI;
import com.project.password.manager.cli.runtime.CliTheme;
import com.project.password.manager.configuration.application.Configuration;
import com.project.password.manager.exceptions.InvalidAppModeException;
import com.project.password.manager.workspace.Workspace;

public class Application {

    public static void main(String[] args) {
        Workspace.getInstance().configureWorkSpace();
        if (Configuration.getInstance().cliConfiguration().isEnabled()) {
            CliTheme.initialize();
            CLI.initCLI(args);
        } else {
            throw new InvalidAppModeException("CLI is not enabled for this instance.");
        }
    }
}
