# Secrets Manager CLI

This project is a Java-based interactive password manager CLI. It supports user signup and login, JWT-backed CLI sessions, user and team vault management, encrypted entry storage, configuration inspection, and a command architecture built with Picocli, JLine, and Guice.

The application starts an interactive shell from `Application.main(...)`, with command handlers resolved at runtime through Guice.

## What The Application Does

The current implementation supports:

- User signup, login, logout, session inspection, and session validation
- Automatic default vault creation on signup
- User role inspection and administration
- Team creation, inspection, and listing
- Team vault creation and listing
- Personal vault creation, listing, and default vault inspection
- Entry creation, listing, retrieval, update, deletion, and search
- Exact label lookup across one vault or all vaults accessible to the authenticated user
- Configuration property listing, lookup, and override from the CLI
- File-backed persistence by default, with SQL and NoSQL infrastructure present but not complete for all repository types

## Tech Stack

- Java
- Maven
- Picocli for command parsing
- JLine for the interactive shell
- Guice for dependency injection and authorization interception
- Auth0 Java JWT for token creation and validation
- Argon2 for password hashing
- AES/GCM for vault and entry payload encryption
- Jackson for payload serialization
- Caffeine for short-lived in-memory search index caching
- Log4j2 for logging

## Project Entry Point

The application starts in `src/main/java/com/project/password/manager/Application.java`:

```java
public static void main(String[] args) throws IOException {
    Workspace.configureWorkSpace();
    if (Configuration.getInstance().cliConfiguration().isEnabled()) {
        CliTheme.initialize();
        CLI.initCLI(args);
    } else {
        throw new InvalidAppModeException("CLI is not enabled for this instance.");
    }
}
```

The interactive shell uses the active CLI theme and renders a themed startup panel. A typical prompt looks like this:

```text
pm::vault >
```

Type `exit` to leave the shell.

## Current CLI Command Surface

Top-level commands:

- `login`
- `signup`
- `logout`
- `whoami`
- `ping`
- `user ...`
- `team ...`
- `config ...`
- `theme ...`
- `vault ...`
- `entry ...`
- `help`

### Auth Commands

Create a user and open a session:

```bash
signup <username> <password>
```

Authenticate and open a session:

```bash
login <username> <password>
```

Revoke the active session:

```bash
logout
```

Show the authenticated user:

```bash
whoami
```

Validate the current session:

```bash
ping
```

### User Role Commands

Inspect roles for a user:

```bash
user role list <username>
```

Grant a role:

```bash
user role grant <username> ADMIN
```

Revoke a role:

```bash
user role revoke <username> ADMIN
```

### Team Commands

List teams accessible to the authenticated user:

```bash
team list
```

Show one team:

```bash
team get <teamName>
```

Create a team:

```bash
team create <teamName>
```

By default, team creation can be restricted to admins through the team configuration.

### Team Vault Commands

List vaults for a team accessible to the authenticated user:

```bash
team vault list <teamName>
```

Create a vault for a team owned by the authenticated user:

```bash
team vault create <teamName> <vaultName>
```

### Config Commands

List configuration properties:

```bash
config list
```

Show one property:

```bash
config get app.cli.theme
```

Override one property:

```bash
config set app.cli.theme ocean
```

### Theme Commands

List supported themes:

```bash
theme list
```

Preview all themes or a specific theme:

```bash
theme preview
theme preview copper-dusk
```

Persist a theme for future runs:

```bash
theme set paper-retro
```

### Vault Commands

List all vaults accessible to the authenticated user:

```bash
vault list
vault list --show-ids
```

Create a personal vault:

```bash
vault create <vaultName>
```

Show the default vault:

```bash
vault default
vault default --show-ids
```

### Entry Commands

List entries in a vault. If `--vault` is omitted, the default vault is used:

```bash
entry list
entry list --vault "Work"
```

Create an entry:

```bash
entry create "github" "MySecret123!"
entry create --vault "Work" "github" "MySecret123!" --username "alice" --login-name "alice.dev" --url "https://github.com" --tag "dev,code" --note "main account"
```

Get entries by entry id or exact label:

```bash
entry get "github"
entry get --vault "Work" "github"
entry get --vault "Work" "<entry-id>"
```

Update an entry:

```bash
entry update --vault "Work" "<entry-id>" "github" "NewSecret123!" --username "alice" --login-name "alice.dev" --url "https://github.com" --tag "dev,critical" --note "rotated on 2026-04-22"
```

Delete an entry:

```bash
entry delete --vault "Work" "<entry-id>"
```

Search entries in a vault:

```bash
entry search "github"
entry search --vault "Work" "alice github"
```

## Authentication And Session Model

Authentication uses two layers:

- Password hashing with Argon2
- JWT access tokens persisted per user

Current flow:

1. `signup` creates a new user.
2. A default personal vault named `Default` is created automatically.
3. A JWT is issued and persisted.
4. The CLI session stores the authenticated `userId` and raw token in memory.

Protected handlers are annotated with `@RequireAuthorization`. Guice applies `TokenAuthorizationInterceptor`, which delegates to `TokenVerifier`.

`TokenVerifier` checks:

- a CLI session exists
- the user still exists
- the persisted token matches the in-memory session token
- the JWT still verifies and is not expired

If verification fails, the CLI session is cleared and the command fails with an unauthorized session error.

## Security And Access Model

The current security model uses:

- Argon2 password hashing for users
- AES/GCM encryption for vault and entry payloads
- JWT-backed CLI sessions for authentication
- Handler-level authorization through Guice interception
- Access checks for both personal vaults and team-scoped vaults

Important behavior:

- Expired JWTs invalidate the active CLI session
- Re-login after token expiry is supported by token re-issuance logic in `TokenService`
- Vault references can be either internal ids or vault names
- Entry operations are allowed only against vaults accessible to the current user
- Team vault creation is restricted to team owners

## Storage Model

The application is designed around repository interfaces and a factory that selects persistence based on configuration.

### Default Mode

By default, storage falls back to the local file system.

`DataRepositoryFactory` currently uses file-backed repositories when:

- `app.database.enabled=false`, or
- no supported database-backed implementation is available for the requested repository type

Current file-backed repositories include:

- users
- tokens
- vaults
- teams
- entries

### Current File Shapes

- Users are stored under `users/<userId>/user.json`
- Tokens are stored with the user workspace data
- Vaults are stored under `vaults/<vaultId>/<vaultId>.json`
- Teams are stored under `teams/<teamId>/team.json`
- Entries are stored per vault

Team files store owner and member user ids rather than embedding full user objects. The file-backed team model also keeps compatibility for older team files that may still contain embedded user objects and normalizes them to ids when reading.

### SQL And NoSQL Status

The project contains SQL and NoSQL-oriented infrastructure, but entry persistence is not fully implemented there yet.

Current state:

- User entity wiring exists for SQL and NoSQL paths
- General repository factory support exists
- Team and vault abstractions are wired into the service layer
- Entry repository support for SQL and NoSQL currently throws `UnsupportedOperationException`

For practical use today, assume file-backed storage is the supported path.

## Workspace And Configuration

The application reads configuration from a file named:

```text
password-manager.properties
```

This file is expected under a workspace directory in the OS user home:

```text
<user-home>/password-manager/password-manager.properties
```

On Windows this typically resolves to:

```text
C:\Users\<your-user>\password-manager\password-manager.properties
```

### Important Current Behavior

The application calls `Workspace.configureWorkSpace()` on startup.

That means the first run will automatically:

- create the workspace directory if it does not exist
- create `password-manager.properties` if it does not exist
- backfill missing default properties for existing workspaces when new config keys are introduced

### Minimal Recommended Properties

For local file-backed usage with HMAC JWTs, create `password-manager.properties` with values like these:

```properties
app.name=password-manager-cli
app.version=1.0.0
app.cli.theme=warm-retro

app.database.enabled=false
app.database.type=sql
app.database.vendor=postgres
app.database.host=localhost
app.database.port=5432
app.database.name=password_manager
app.database.username=username
app.database.password=password
app.database.ddl.mode=update

app.crypto.argon2.memory.kb=65536
app.crypto.argon2.iterations=3
app.crypto.argon2.parallelism=1
app.crypto.argon2.salt.length=16
app.crypto.argon2.hash.length=32

app.jwt.algorithm=HS256
app.jwt.issuer=password-manager-cli
app.jwt.access.expiration.ms=900000
app.jwt.refresh.expiration.ms=604800000
app.jwt.secret=replace-with-a-long-random-secret

app.aes.transformation=AES/GCM/NoPadding
app.aes.key.size=128
app.aes.tag.length=128
app.aes.iv.length=12

app.salt.iterations=65536
app.salt.key.size=256
```

## CLI Theme Customization

The CLI theme can be customized without code changes.

Supported themes:

- `warm-retro`
- `ocean`
- `copper-dusk`
- `paper-retro`
- `plain`

Precedence order:

1. Environment variable: `PM_CLI_THEME=<theme>`
2. Workspace property: `app.cli.theme=<theme>`

Examples:

```powershell
$env:PM_CLI_THEME = "plain"
java -jar target/password.manager-1.0.0-beta-standalone.jar
```

`plain` is useful for consoles that do not render ANSI styling well.

You can also inspect and persist themes from inside the CLI with:

```bash
theme list
theme preview paper-retro
theme set copper-dusk
```

## JWT Algorithm Configuration

Supported algorithms include:

- `HS256`
- `HS384`
- `HS512`
- `RS256`
- `RS384`
- `RS512`
- `ES256`
- `ES384`
- `ES512`

Configuration requirements:

- For `HS*` algorithms, `app.jwt.secret` must be set.
- For `RS*` and `ES*` algorithms, key files must exist under:

```text
<user-home>/password-manager/authentication/
```

and the following properties must point to the file names inside that folder:

- `app.jwt.private.key.path`
- `app.jwt.public.key.path`

## Output Shape

The CLI prints structured terminal views.

Current output fields include:

- user id
- vault id
- vault name
- team name
- team owners and members
- entry id
- label
- username
- login name
- URL
- tags
- notes
- creation time
- update time

Current output behavior:

- the interactive shell starts with a themed startup panel
- users, teams, vaults, and entries render as framed panels rather than flat key-value text
- status, hints, and command failures use themed cards for consistency
- when listing multiple teams, vaults, or entries, each item is printed as a separate panel block

## Architecture Overview

### Command Layer

Commands live under `src/main/java/com/project/password/manager/cli/commands`.

The CLI uses Picocli annotations for parsing and a root `BaseCommand` with subcommands.

### Handler Layer

Handlers live under `src/main/java/com/project/password/manager/cli/handlers`.

Each command delegates to a corresponding handler. This keeps parsing and business behavior separate.

Key patterns:

- `DelegatingCliCommand<TRequest, THandler>` builds a request object from CLI input
- handlers implement `CommandHandler<TRequest>`
- vault-scoped handlers resolve vault names or ids through `VaultService`
- authorized handlers rely on middleware instead of duplicating token checks

### Runtime Layer

Runtime support lives under `src/main/java/com/project/password/manager/cli/runtime`.

It is responsible for:

- binding handlers to commands
- invoking handlers
- managing CLI session state
- formatting command exceptions into user-facing failures

### Dependency Injection

Guice is the composition root.

Current Guice responsibilities:

- service construction
- repository provisioning
- interceptor registration for authorization
- CLI session and output singletons
- handler registry provisioning

Command-handler registration is provided through Guice as a singleton `CommandHandlerRegistry`.

### Service Layer

The primary business services are:

- `AuthService`
- `UserService`
- `TokenService`
- `TeamService`
- `VaultAccessService`
- `VaultService`
- `EntryService`
- `ConfigService`

Responsibilities:

- `AuthService`: signup and login orchestration
- `UserService`: user lookup and role administration
- `TokenService`: JWT issue, verify, cache, persist, revoke
- `TeamService`: team creation, lookup, access checks, and persistence
- `VaultAccessService`: personal and team vault access checks
- `VaultService`: personal vault creation, team vault creation, default vault resolution, accessible vault resolution
- `EntryService`: entry CRUD, exact reference lookup, vault-scoped search, in-memory search index maintenance
- `ConfigService`: property lookup, override, validation, masking, and persistence support for config handlers

## Entry Retrieval Semantics

`entry get` supports both id-based and label-based retrieval:

- If the argument is an existing entry id in the selected vault, it returns that entry.
- Otherwise it looks for exact label matches.
- If `--vault` is omitted, lookup spans all vaults accessible to the current user.
- When multiple entries share a label, they are sorted by `updatedAtEpochMs` descending, then by `createdAtEpochMs`, then by id.

This makes retrieval practical even when the user remembers a label but not the vault.

## Search Semantics

Search is intentionally different from `entry get`:

- `entry get` is exact reference lookup by id or exact label
- `entry search` is token-based search inside a single vault

The search index currently tokenizes:

- label
- username
- login name
- URL
- tags
- notes

## Build And Run

### Compile

```bash
mvn -DskipTests compile
```

### Package

```bash
mvn -DskipTests package
```

This produces a self-contained jar with dependencies under `target/`.

Example artifact:

```text
target/password.manager-1.0.0-beta-standalone.jar
```

### Run

The application bootstraps its own workspace on startup, so the first run will create:

```text
<user-home>/password-manager/
<user-home>/password-manager/password-manager.properties
```

If the user has Java installed, run the shaded jar directly:

```bash
java -jar target/password.manager-1.0.0-beta-standalone.jar
```

If the end user does not have Java installed, distribute a platform package with an embedded runtime instead of the jar.

Build a portable package on the release machine with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-app.ps1
```

That script uses `jpackage` to create a portable app-image under `dist/` for the current OS.

Default outputs by platform:

```text
Windows:
dist/password-manager-cli/
dist/password-manager-cli/password-manager-cli.exe

Linux:
dist/password-manager-cli/
dist/password-manager-cli.sh

macOS:
dist/password-manager-cli.app
dist/password-manager-cli.command
```

These launchers already include a bundled Java runtime, so the user does not need Java installed.

- Windows: run `password-manager-cli.exe`
- Linux: run `./password-manager-cli.sh`
- macOS: run `./password-manager-cli.command`

If you specifically want a single installer `.exe`, install WiX Toolset on the build machine and then run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1 -Type exe
```

Installer output:

```text
dist/password-manager-cli-1.0.0.exe
```

Important packaging note:

- The build machine still needs a JDK that includes `jpackage`.
- The build machine also needs WiX Toolset if you want an installer `.exe` or `.msi`.
- The end user does not need Java once you ship the packaged Windows app.

## Automated Build Pipeline

The repository includes a GitHub Actions workflow at `.github/workflows/build-windows-cli.yml`.

Pipeline behavior:

- It runs automatically on pushes to `master`.
- It also supports manual runs through `workflow_dispatch`.
- It builds portable packages for Windows, Linux, and macOS.
- It uses `scripts/package-app.ps1` on each runner OS.
- It uploads one artifact per OS.

## Testing

Current automated test coverage is still limited, but the repository now contains more than a smoke test.

Current test classes:

- `AppTest`
- `MetadataListenerTest`
- `ModelObjectMapperFactoryTest`

Run tests with:

```bash
mvn test
```

Manual CLI validation is still important for end-to-end behavior.

### Suggested Manual Test Flow

```bash
signup demo-user Demo@123
whoami
vault list
vault create Work
vault list --show-ids

team create demo-team
team list
team vault create demo-team Shared
team vault list demo-team

entry create "gmail" "Secret123!"
entry create --vault "Work" "github" "WorkSecret123!" --username "alice" --login-name "alice.dev" --url "https://github.com" --tag "dev,code" --note "primary work account"

entry list
entry list --vault "Work"
entry search --vault "Work" "github alice"
entry get "github"
```

To validate duplicate-label ordering:

```bash
entry create --vault "Work" "github" "OlderSecret"
entry list --vault "Work"
entry update --vault "Work" "<entry-id>" "github" "NewerSecret"
entry get "github"
```

Expected behavior:

- all exact-label matches are returned
- the most recently updated one appears first

## Known Limitations

- The root CLI uses an interactive shell; non-interactive automation is not the primary path right now.
- SQL and NoSQL entry repositories are not implemented yet.
- Automated test coverage is still thin compared to the service surface.
- The project relies on a user-home workspace and external property file rather than a resource-bundled config.

## Repository Layout

High-level structure:

```text
src/main/java/com/project/password/manager/
  Application.java
  argon/
  auth/
  cli/
    commands/
    handlers/
    runtime/
  configuration/
  database/
  encryption/
  exceptions/
  guice/
  middleware/
  model/
  service/
  util/

src/main/resources/
  log4j2.xml

src/test/java/com/project/password/manager/
  AppTest.java
  MetadataListenerTest.java
  ModelObjectMapperFactoryTest.java
```

## Recommended Next Improvements

- Add integration tests for signup, login, team creation, vault creation, team vault flows, and entry CRUD.
- Add dedicated SQL and NoSQL entry repository implementations.
- Add a non-interactive mode for scripting and CI use.
- Add CLI support for more team membership lifecycle operations.
