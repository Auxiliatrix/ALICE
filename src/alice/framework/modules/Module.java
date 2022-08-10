package alice.framework.modules;

import alice.framework.dependencies.Command;
import alice.framework.dependencies.DependencyFactory;
import alice.framework.main.Brain;
import alice.framework.main.Constants;
import alice.framework.utilities.EmbedFactory;
import discord4j.core.event.domain.Event;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public abstract class Module<E extends Event> {
	
	private Command<E> command;
		
	protected Module(Class<E> type) {
		load(type);
		this.command = buildCommand(DependencyFactory.<E>builder());
	}
	
	protected void load(Class<E> type) {
		Brain.gateway.on(type).subscribe(e -> handle(e)
//				.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(5)).doBeforeRetry(rs -> {
//					AliceLogger.error(String.format("Error propagated. Retrying %d of %d...",rs.totalRetriesInARow()+1,5));
//				}))
//				.doOnError(f -> {
//					AliceLogger.error("Fatal error propagated during task execution:");
//					f.printStackTrace();
//					Brain.gateway.logout().block();
//				})
				.block()
			);
	}
	
	public Mono<?> handle(E event) {
		return command.apply(event);
	}
	
	public abstract Command<E> buildCommand(DependencyFactory.Builder<E> dfb);
	
	public EmbedCreateSpec helpEmbed(Command<E> command) {
		if( command == null ) {
			return EmbedFactory.build(EmbedFactory.modErrorFormat("Command not found!"));
		}
		
		EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
			.color(Color.of(63, 79, 95))
			.author(String.format("[%s] %s", Constants.NAME, Constants.FULL_NAME), Constants.LINK, Brain.gateway.getSelf().block().getAvatarUrl())
			.title(String.format(":grey_question: %s", this.getClass().getName()));
		
		if( command.isPassive() ) {
			builder.addField(String.format("`%s`", command.getUsage()), String.format("> %s", command.getDescription()), false);
		} else {
			builder.addField(String.format("`%s%s`", MessageModule.PREFIX, command.getUsage()), String.format("> %s", command.getDescription()), false);
		}
		
		for( Command<E> subcommand : command.getDocumentedSubcommands() ) {
			if( subcommand.isPassive() ) {
				builder.addField(String.format("`%s`", subcommand.getUsage()), String.format("> %s", command.getDescription()), true);
			} else {
				builder.addField(String.format("`%s%s`", MessageModule.PREFIX, command.getUsage()), String.format("> %s", subcommand.getDescription()), true);
			}
		}
		
		return builder.build();
	}
	
	public EmbedCreateSpec helpEmbed(String usage) {
		return helpEmbed(command.getSubcommand(usage));
	}
	
	public EmbedCreateSpec helpEmbed() {
		return helpEmbed(command);
	}
}
