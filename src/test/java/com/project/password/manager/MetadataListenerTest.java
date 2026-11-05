package com.project.password.manager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.configuration.application.Configuration;
import com.project.password.manager.database.file.storage.UserRepository;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.logging.WorkspaceTransactionLogger;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.Status;
import com.project.password.manager.model.UserRole;
import com.project.password.manager.model.database.file.storage.Metadata;
import com.project.password.manager.model.database.file.storage.User;
import com.project.password.manager.workspace.IWorkspace;
import com.project.password.manager.workspace.IWorkspaceLocator;

import junit.framework.TestCase;

public class MetadataListenerTest extends TestCase {

    public void testRepositoryUpdateRefreshesUpdatedAt() throws Exception {
        Path workspace = Files.createTempDirectory("metadata-listener-update");
        try {
            File mockWorkspace = workspace.toFile();
            ITransactionLogger logger = new WorkspaceTransactionLogger(Configuration.getInstance().appConfiguration(), workspace.toFile());
            IWorkspaceLocator locator = new IWorkspaceLocator() {
                @Override
                @NotNull
                public File getWorkspaceRoot() {
                    return mockWorkspace;
                }

                @Override
                @NotNull
                public IWorkspace getWorkspace() {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException();
                }

                @Override
                @NotNull
                public File getUserWorkspaceLocation() {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException();
                }

                @Override
                @NotNull
                public File getVaultWorkspaceLocation() {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException();
                }

                @Override
                @NotNull
                public File getEntriesWorkspaceLocation() {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException();
                }
            };
            UserRepository repository = new UserRepository(locator, logger);
            User user = new User("user-1", "alice", "verifier", "salt", "vault-1", new ArrayList<>(),
                    List.of(UserRole.USER), new Metadata());
            LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 4, 27, 12, 0, 0);
            LocalDateTime originalUpdatedAt = LocalDateTime.of(2026, 4, 27, 12, 1, 0);

            user.metadata().setCreatedAt(originalCreatedAt);
            user.metadata().setUpdatedAt(originalUpdatedAt);
            user.metadata().setLastAccessedAt(originalCreatedAt);
            user.metadata().setVersion("1.0");
            user.metadata().setStatus(Status.ACTIVE);
            repository.save(user);

            user.setName("alice-updated");
            repository.update(user.getId(), user);

            IUser persistedUser = repository.findById("user-1");
            assertNotNull(persistedUser);
            assertEquals(originalCreatedAt, persistedUser.metadata().createdAt());
            assertTrue(persistedUser.metadata().updatedAt().isAfter(originalUpdatedAt));
            assertEquals(Status.ACTIVE, persistedUser.metadata().status());
        } finally {
            deleteRecursively(workspace);
        }
    }

    private void deleteRecursively(Path path) throws Exception {
        if (!Files.exists(path)) {
            return;
        }
        Files.walk(path)
                .sorted((left, right) -> right.compareTo(left))
                .forEach(currentPath -> {
                    try {
                        Files.deleteIfExists(currentPath);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }
}
