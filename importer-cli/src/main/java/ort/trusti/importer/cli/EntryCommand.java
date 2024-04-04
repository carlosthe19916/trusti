package ort.trusti.importer.cli;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import ort.trusti.importer.cli.git.GitCommand;
import ort.trusti.importer.cli.http.HttpCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {HttpCommand.class, GitCommand.class})
public class EntryCommand {


}
