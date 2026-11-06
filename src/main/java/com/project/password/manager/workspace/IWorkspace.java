/*
 * Copyright 2026 Polarion AG
 */
package com.project.password.manager.workspace;

import java.io.File;

import org.jetbrains.annotations.NotNull;

public interface IWorkspace {

    @NotNull
    IWorkspace getWorkspace();

    @NotNull
    File getRoot();

    boolean workspaceExists();

    boolean fileExists(@NotNull File file);

    void configureWorkSpace();

    void createDirectories(@NotNull String... folders);

}
