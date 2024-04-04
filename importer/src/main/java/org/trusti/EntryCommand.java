package org.trusti;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import org.trusti.git.GitCommand;
import org.trusti.http.HttpCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {HttpCommand.class, GitCommand.class})
public class EntryCommand {
}
