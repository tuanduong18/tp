package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ImportCommandTest {

    @TempDir
    Path tempDir;

    @Test
    public void execute_validFile_importsAndReturnsSummary() throws Exception {
        Path csv = tempDir.resolve("players.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion",
                "Alice,Top,Gold,Ahri"
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("Imported 1"),
                "Expected 'Imported 1' but got: " + feedback);
        assertTrue(feedback.contains("0 duplicate"),
                "Expected '0 duplicate' but got: " + feedback);
        assertTrue(feedback.contains("0 invalid"),
                "Expected '0 invalid' but got: " + feedback);
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_players_success() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Path out = tempDir.resolve("players.csv");
        CommandResult r = new ExportCommand(ExportCommand.Target.PLAYERS, out).execute(model);
        assertTrue(Files.exists(out));
        assertTrue(r.getFeedbackToUser().contains("Exported player data"));
    }

    @Test
    public void execute_fileNotFound_throwsCommandException() {
        Path nonExistent = tempDir.resolve("does_not_exist.csv");
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(nonExistent);

        CommandException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                CommandException.class, () -> cmd.execute(model)
        );
        assertTrue(thrown.getMessage().contains("file not found"),
                "Expected message to contain 'file not found' but got: " + thrown.getMessage());
    }

    @Test
    public void execute_duplicatePlayers_countsDuplicates() throws Exception {
        Path csv = tempDir.resolve("duplicates.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion",
                "Alice,Top,Gold,Ahri",
                "Alice,Top,Gold,Ahri"
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("Imported 1"));
        assertTrue(feedback.contains("1 duplicate"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_invalidRows_showsSampleErrors() throws Exception {
        Path csv = tempDir.resolve("invalid.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion",
                "Alice,Top,InvalidRank,Ahri",
                "Bob,Mid,Gold,Yasuo"
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("Imported 1"));
        assertTrue(feedback.contains("1 invalid"));
        assertTrue(feedback.contains("Examples of invalid rows") || feedback.contains("line"));
    }

    @Test
    public void execute_emptyFile_throwsCommandException() throws Exception {
        Path empty = tempDir.resolve("empty.csv");
        Files.write(empty, List.of());

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(empty);

        assertThrows(CommandException.class, () -> cmd.execute(model));
    }

    @Test
    public void execute_playersExport_success() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Path out = tempDir.resolve("players.csv");
        CommandResult r = new ExportCommand(ExportCommand.Target.PLAYERS, out).execute(model);

        assertTrue(Files.exists(out));
        assertTrue(r.getFeedbackToUser().contains("Exported player data"));
    }

    @Test
    public void execute_teamsExport_success() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Path out = tempDir.resolve("teams.csv");
        CommandResult r = new ExportCommand(ExportCommand.Target.TEAMS, out).execute(model);

        assertTrue(Files.exists(out));
        assertTrue(r.getFeedbackToUser().contains("Exported team data"));
    }

    @Test
    public void equals_sameObject_returnsTrue() {
        Path path = tempDir.resolve("test.csv");
        ImportCommand cmd = new ImportCommand(path);
        assertTrue(cmd.equals(cmd));
    }

    @Test
    public void equals_differentPath_returnsFalse() {
        ImportCommand cmd1 = new ImportCommand(tempDir.resolve("a.csv"));
        ImportCommand cmd2 = new ImportCommand(tempDir.resolve("b.csv"));
        assertTrue(!cmd1.equals(cmd2));
    }

    @Test
    public void equals_samePath_returnsTrue() {
        Path path = tempDir.resolve("test.csv");
        ImportCommand cmd1 = new ImportCommand(path);
        ImportCommand cmd2 = new ImportCommand(path);
        assertTrue(cmd1.equals(cmd2));
    }

    @Test
    public void execute_teams_success() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Path out = tempDir.resolve("teams.csv");
        CommandResult r = new ExportCommand(ExportCommand.Target.TEAMS, out).execute(model);
        assertTrue(Files.exists(out));
        assertTrue(r.getFeedbackToUser().contains("Exported team data"));
    }

    @Test
    public void execute_ioError_wrappedAsCommandException() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        // Pass a directory as "file" -> Files.write will throw, exercising catch block
        Command cmd = new ExportCommand(ExportCommand.Target.PLAYERS, tempDir);
        assertThrows(CommandException.class, () -> cmd.execute(model));
    }

    @Test
    public void execute_invalidRole_showsInvalidRow() throws Exception {
        Path csv = tempDir.resolve("invalidRole.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion",
                "Alice,Coach,Gold,Ahri", // invalid role
                "Bob,Mid,Gold,Yasuo"
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("Imported 1"),
                "Expected 1 valid import, but got: " + feedback);
        assertTrue(feedback.contains("1 invalid"),
                "Expected 1 invalid, but got: " + feedback);
        assertTrue(feedback.toLowerCase().contains("role") || feedback.contains("line"),
                "Expected message to reference invalid role");
    }

    @Test
    public void execute_invalidRank_showsInvalidRow() throws Exception {
        Path csv = tempDir.resolve("invalidRank.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion",
                "Alice,Top,GodTier,Ahri" // invalid rank
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("0 duplicate"));
        assertTrue(feedback.contains("1 invalid"));
        assertTrue(feedback.toLowerCase().contains("rank must be one of the following"),
                "Expected feedback to mention rank validity, but got:\n" + feedback);
    }


    @Test
    public void execute_invalidChampion_showsInvalidRow() throws Exception {
        Path csv = tempDir.resolve("invalidChampion.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion",
                "Alice,Top,Gold,gg" // invalid champion name
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("0 duplicate"));
        assertTrue(feedback.contains("1 invalid"),
                "Expected one invalid champion row.");
        assertTrue(feedback.toLowerCase().contains("champion")
                        || feedback.toLowerCase().contains("gg"),
                "Expected feedback mentioning invalid champion");
    }

    @Test
    public void execute_multipleInvalidRows_countsAll() throws Exception {
        Path csv = tempDir.resolve("multiInvalid.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion",
                "Alice,Coach,Gold,Ahri", // invalid role
                "Bob,Mid,God,Ahri", // invalid rank
                "Carl,Top,Gold,gg", // invalid champion
                "Dave,Mid,Gold,Yasuo" // valid
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("Imported 1"));
        assertTrue(feedback.contains("3 invalid"),
                "Expected three invalid rows counted.");
        assertTrue(feedback.contains("Examples of invalid rows"));
    }

    @Test
    public void execute_invalidHeader_throwsCommandException() throws Exception {
        Path csv = tempDir.resolve("invalidHeader.csv");
        Files.write(csv, List.of(
                "WrongHeader1,WrongHeader2,WrongHeader3,WrongHeader4",
                "Alice,Top,Gold,Ahri"
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        assertThrows(CommandException.class, () -> cmd.execute(model),
                "Expected CommandException for invalid header.");
    }

    @Test
    public void execute_winsLossesHeader_validExtendedImport() throws Exception {
        Path csv = tempDir.resolve("winsLosses.csv");
        Files.write(csv, List.of(
                "Name,Role,Rank,Champion,Wins,Losses",
                "Alice,Top,Gold,Ahri,3,1"
        ));

        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        ImportCommand cmd = new ImportCommand(csv);
        CommandResult result = cmd.execute(model);

        String feedback = result.getFeedbackToUser();
        assertTrue(feedback.contains("Imported 1"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }


}
