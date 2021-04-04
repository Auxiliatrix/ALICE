package alice.modular.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.features.Documentable;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Handler;
import alice.framework.handlers.MessageHandler;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class ModuleCommandHandler extends CommandHandler implements Documentable {

	public ModuleCommandHandler() {
		super("Modules", false, PermissionProfile.getAdminPreset().andNotDM());
		aliases.add("mod");
	}

	@Override
	public boolean isEnabled(boolean whitelist, Mono<Guild> guild) {
		return true;
	}
	
	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		
		Set<String> enabledSet = new HashSet<String>();
		Set<String> disabledSet = new HashSet<String>();
		
		for( Handler<?> h : Brain.handlers.get() ) {
			if( h instanceof MessageHandler && h instanceof Documentable ) {
				boolean enabled = ((MessageHandler) h).isEnabled(event.getGuild());
				if( ((Documentable) h).getCategory().equals(DEVELOPER.name()) && !PermissionProfile.isDeveloper(event.getMessage().getAuthor())) {
					continue;
				}
				if( enabled ) {
					enabledSet.add(h.getName());
				} else {
					disabledSet.add(h.getName());
				}
			}
		}
		
		String[] enabledArray = enabledSet.toArray(new String[enabledSet.size()]);;
		String[] disabledArray = disabledSet.toArray(new String[disabledSet.size()]);

		Arrays.sort(enabledArray);
		Arrays.sort(disabledArray);
		
		List<String> enabledList = new ArrayList<String>(Arrays.asList(enabledArray));
		List<String> disabledList = new ArrayList<String>(Arrays.asList(disabledArray));
		
		if( ts.size() < 2 ) {
			response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getModulesConstructor(enabledList, disabledList)));
		} else if( ts.size() < 3 ) {
			switch( ts.get(1).toLowerCase() ) {
				case "enabled":
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getModulesConstructor(enabledList, null)));
					break;
				case "disabled":
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getModulesConstructor(null, disabledList)));
					break;
				default:
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor(event.getMessage().getAuthor(), this)));
					break;
			}
		} else {
			MessageHandler module = Brain.getDocumentableByName(ts.get(2));
			switch( ts.get(1).toLowerCase() ) {
			case "enable":
				if( module == null ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("Module not found!", EmbedBuilders.ERR_USAGE)));
				} else if( module.isEnabled(event.getGuild()) ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor(String.format("%s Module is already enabled!", module.getName()))));
				} else if( ((Documentable) module).getCategory().equals(DEVELOPER.name()) && !PermissionProfile.isDeveloper(event.getMessage().getAuthor()) ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("This module cannot be enabled!", EmbedBuilders.ERR_PERMISSION)));
				} else {
					if( guildData.has(String.format("module_disable_%s", module.getName())) ) {
						guildData.remove(String.format("module_disable_%s", module.getName()));
					}
					guildData.put(String.format("module_enable_%s", module.getName()), true);
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor(String.format("%s Module enabled successfully!", module.getName()))));
				}
				break;
			case "disable":
				if( module == null ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("Module not found!", EmbedBuilders.ERR_USAGE)));
				} else if( module == this ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("This module cannot be disabled!", EmbedBuilders.ERR_PERMISSION)));
				} else if( ((Documentable) module).getCategory().equals(DEVELOPER.name()) && !PermissionProfile.isDeveloper(event.getMessage().getAuthor()) ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("This module cannot be disabled!", EmbedBuilders.ERR_PERMISSION)));
				} else if( !module.isEnabled(event.getGuild()) ) {
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor(String.format("%s Module is already disabled!", module.getName()))));
				} else {
					if( guildData.has(String.format("module_enable_%s", module.getName())) ) {
						guildData.remove(String.format("module_enable_%s", module.getName()));
					}
					guildData.put(String.format("module_disable_%s", module.getName()), true);
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor(String.format("%s Module disabled successfully!", module.getName()))));
				}
				break;
			default:
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor(event.getMessage().getAuthor(), this)));
				break;
			}
		}
		
		response.toMono().block();
	}

	@Override
	public String getCategory() {
		return ADMIN.name();
	}

	@Override
	public String getDescription() {
		return "Allows server admins to enable and disable various modules.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
			new DocumentationPair(String.format("%s", invocation), "Lists out all active modules and their states."),
			new DocumentationPair(String.format("%s enabled", invocation), "Lists out all enabled modules."),
			new DocumentationPair(String.format("%s disabled", invocation), "Lists out all disabled modules."),
			new DocumentationPair(String.format("%s enable <Module>", invocation), "Enables the selected module."),
			new DocumentationPair(String.format("%s disable <Module>", invocation), "Disables the selectd module."),
		};
	}

}
