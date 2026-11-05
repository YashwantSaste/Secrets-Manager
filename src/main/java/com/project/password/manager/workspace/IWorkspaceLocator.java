/*
 * Copyright 2026 Polarion AG
 */
package com.project.password.manager.workspace;

import java.io.File;

import org.jetbrains.annotations.NotNull;

public interface IWorkspaceLocator {

//    default void doWorkspaceExist() {
//
//    }

    @NotNull
    File getUserWorkspaceLocation();

    @NotNull
    File getVaultWorkspaceLocation();
}
