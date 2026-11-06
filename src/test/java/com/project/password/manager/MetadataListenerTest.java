package com.project.password.manager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.database.file.storage.UserRepository;
import com.project.password.manager.event.IEvent;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.Status;
import com.project.password.manager.model.UserRole;
import com.project.password.manager.model.database.file.storage.Metadata;
import com.project.password.manager.model.database.file.storage.User;
import com.project.password.manager.workspace.IWorkspace;
import com.project.password.manager.workspace.IWorkspaceLocator;

import junit.framework.TestCase;

public class MetadataListenerTest extends TestCase {

    public void testRepositorySaveInitializesMissingMetadataFields() throws Exception {
	Path root = Files.createTempDirectory("metadata-listener-save");
	try {
	    UserRepository repository = createRepository(root);
	    User user = newUser();
	    repository.save(user);

	    User persistedUser = repository.findById(user.getId());
	    assertNotNull(persistedUser);
	    assertNotNull(persistedUser.metadata().createdAt());
	    assertNotNull(persistedUser.metadata().updatedAt());
	    assertNotNull(persistedUser.metadata().lastAccessedAt());
	    assertEquals(Status.ACTIVE, persistedUser.metadata().status());
	} finally {
	    deleteRecursively(root);
	}
    }

    public void testRepositoryUpdateRefreshesUpdatedAt() throws Exception {
	Path root = Files.createTempDirectory("metadata-listener-update");
	try {
	    UserRepository repository = createRepository(root);
	    User user = newUser();
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

	    User persistedUser = repository.findById(user.getId());
	    assertNotNull(persistedUser);
	    assertEquals(originalCreatedAt, persistedUser.metadata().createdAt());
	    assertTrue(persistedUser.metadata().updatedAt().isAfter(originalUpdatedAt));
	    assertEquals(Status.ACTIVE, persistedUser.metadata().status());
	} finally {
	    deleteRecursively(root);
	}
    }

    private UserRepository createRepository(Path root) {
	File workspaceRoot = root.toFile();
	IWorkspace workspace = new IWorkspace() {
	    @Override
	    public IWorkspace getWorkspace() {
		return this;
	    }

	    @Override
	    public File getRoot() {
		return workspaceRoot;
	    }

	    @Override
	    public boolean workspaceExists() {
		return workspaceRoot.exists();
	    }

	    @Override
	    public boolean fileExists(File file) {
		return file.exists();
	    }

	    @Override
	    public void configureWorkSpace() {
	    }

	    @Override
	    public void createDirectories(String... folders) {
		for (String folder : folders) {
		    new File(workspaceRoot, folder).mkdirs();
		}
	    }
	};

	IWorkspaceLocator locator = new IWorkspaceLocator() {
	    @Override
	    public IWorkspace getWorkspace() {
		return workspace;
	    }

	    @Override
	    public File getWorkspaceRoot() {
		return workspaceRoot;
	    }

	    @Override
	    public File getUserWorkspaceLocation() {
		return new File(workspaceRoot, USERS_WORKSPACE_FOLDER);
	    }

	    @Override
	    public File getVaultWorkspaceLocation() {
		return new File(workspaceRoot, VAULTS_WORKSPACE_FOLDER);
	    }

	    @Override
	    public File getEntriesWorkspaceLocation() {
		return new File(workspaceRoot, ENTRIES_WORKSPACE_FOLDER);
	    }

	    @Override
	    public File getLogsWorkspaceLocation() {
		return new File(workspaceRoot, LOGS_WORKSPACE_FOLDER);
	    }

	    @Override
	    @NotNull
	    public File getTeamsWorkspaceLocation() {
		return new File(workspaceRoot, TEAMS_WORKSPACE_FOLDER);
	    }

	    @Override
	    @NotNull
	    public File getUserGroupsWorkspaceLocation() {
		return new File(workspaceRoot, USER_GROUPS_WORKSPACE_FOLDER);
	    }
	};
	ITransactionLogger logger = new ITransactionLogger() {
	    @Override
	    public void logEvent(IEvent event, String level) {
	    }

	    @Override
	    public void logRepositoryOperation(boolean databaseOperation, String repositoryName, String operation,
		    String entityType, String entityId, String status, String details) {
	    }
	};
	return new UserRepository(locator, logger);
    }

    private User newUser() {
	return new User("user-1", "alice", "verifier", "salt", "vault-1", List.of(), List.of(UserRole.USER), List.of(),
		new Metadata());
    }

    private void deleteRecursively(Path path) throws Exception {
	if (!Files.exists(path)) {
	    return;
	}
	try (var paths = Files.walk(path)) {
	    paths.sorted(Comparator.reverseOrder()).forEach(currentPath -> {
		try {
		    Files.deleteIfExists(currentPath);
		} catch (Exception exception) {
		    throw new RuntimeException(exception);
		}
	    });
	}
    }
}
