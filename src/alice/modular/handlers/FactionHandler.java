package alice.modular.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import alice.framework.actions.Action;
import alice.framework.actions.NullAction;
import alice.framework.handlers.CommandHandler;
import alice.framework.handlers.Documentable;
import alice.framework.main.Brain;
import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.Faction;
import alice.framework.structures.PermissionProfile;
import alice.framework.structures.QuantifiedPair;
import alice.framework.structures.TokenizedString;
import alice.framework.utilities.EmbedBuilders;
import alice.framework.utilities.EventUtilities;
import alice.modular.actions.MessageCreateAction;
import alice.modular.datamanagers.FactionDataManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;

public class FactionHandler extends CommandHandler implements Documentable {

	public static final int MAX_OFFICERS = 5;
	public static final int REPUTATION_THRESHOLD = 50;
	
	public FactionHandler() {
		super("Faction", false, PermissionProfile.getAnyonePreset().andFromUser().andNotDM());
	}
	
	@Override
	protected void execute(MessageCreateEvent event) {
		Action response = new NullAction();
		TokenizedString ts = new TokenizedString(event.getMessage().getContent());
		
		AtomicSaveFile guildData = Brain.guildIndex.get(EventUtilities.getGuildId(event));
		Guild guild = event.getGuild().block();
		Optional<User> user = event.getMessage().getAuthor();
		String ownId = event.getMessage().getAuthor().get().getId().asString();
		long userID = event.getMessage().getAuthor().get().getId().asLong();

		FactionDataManager fdm = new FactionDataManager(guildData, guild);
		
		String allegiance = fdm.getAllegiance(userID);
		String ownership = fdm.getOwnership(userID);
		
		
		if( ts.size() == 1 ) {
			if( allegiance == "" ) {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You do not currently belong to a faction!", EmbedBuilders.ERR_USAGE)));
			} else {
				response.addAction(new MessageCreateAction(event.getMessage().getChannel(), fdm.getFactionProfileConstructor(allegiance)));
			}
		} else {
			switch( ts.get(1).toLowerCase() ) {
				case "list":
					List<QuantifiedPair<String>> entries = fdm.getSortedFactions();
					int entriesSize = entries.size();
					int total = 0;
					for( QuantifiedPair<String> entry : entries ) {
						total += entry.value;
					}
					List<String> fieldHeaders = new ArrayList<String>();
					List<String> fieldBodies = new ArrayList<String>();
					for( int f=0; f< Math.min(12, entries.size()); f++ ) {
						if( entries.isEmpty() ) {
							break;
						}
						QuantifiedPair<String> entry = entries.get(f);
						try {
							fieldHeaders.add(String.format("%d. %s", f+1, entry.key));
							fieldBodies.add(String.format("Members:\t:clipboard: %d", entry.value));
						} catch( Exception e ) {
							entries.remove(f);
							f--;
							continue;
						}
					}
					response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getLeaderboardConstructor("Factions", fieldHeaders, fieldBodies, total, entriesSize)));
					break;
				case "profile":
					if( ts.size() < 3 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a faction name in quotes!", EmbedBuilders.ERR_USAGE)));
					} else if( !fdm.has(ts.get(2)) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That faction does not exist!", EmbedBuilders.ERR_USAGE)));
					} else {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), fdm.getFactionProfileConstructor(allegiance)));
					}
					break;
				case "create":
					if( ts.size() < 3 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a faction name in quotes!", EmbedBuilders.ERR_USAGE)));
					} else if( !ownership.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are already the leader of a faction!", EmbedBuilders.ERR_USAGE)));
					} else if( !allegiance.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are already in a faction!", EmbedBuilders.ERR_USAGE)));
					} else if( fdm.has(ts.get(2)) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That faction already exists!", EmbedBuilders.ERR_USAGE)));
					} else if( RoleAssignHandler.getRoleByName(guild, ts.get(2)) != null ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("A role with this name already exists!", EmbedBuilders.ERR_USAGE)));
					} else {
						if( !guildData.has("reputation_map") ) {
							guildData.put("reputation_map", new JSONObject());
						}
						if( !guildData.getJSONObject("reputation_map").has(userID+"") ) {
							guildData.modifyJSONObject("reputation_map", j -> j.put(userID+"", 1));
						}
						if( guildData.getJSONObject("reputation_map").getInt(userID+"") < REPUTATION_THRESHOLD && !PermissionProfile.hasPermission(user, event.getGuild(), Permission.ADMINISTRATOR)) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You do not have enough reputation to do this!", EmbedBuilders.ERR_PERMISSION)));
						} else {
							Faction newFaction = new Faction(ts.get(2), userID, event.getMessage().getTimestamp().toEpochMilli());
							fdm.addFaction(ts.get(2), newFaction);
							fdm.setAllegiance(userID, ts.get(2));
							fdm.setOwnership(userID, ts.get(2));
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("You have successfully created a new faction!")));
						}
					}
					break;
				case "disband":
					if( ownership.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are not the faction leader!", EmbedBuilders.ERR_PERMISSION)));
					} else if( ts.quotedOnly().size() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify your faction name in quotes to confirm!", EmbedBuilders.ERR_PERMISSION)));
					} else if( !allegiance.equalsIgnoreCase(ts.quotedOnly().get(0)) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify your faction name in quotes to confirm!", EmbedBuilders.ERR_PERMISSION)));
					} else {
						fdm.setOwnership(userID, "");
						fdm.setAllegiance(userID, "");
						Faction faction = fdm.getFaction(ts.get(2));
						for( long member : faction.members ) {
							fdm.setAllegiance(member, "");
						}
						fdm.removeFaction(ts.get(2));
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Faction disbanded successfully!")));
						// TODO: remove relevant roles
					}
					break;
				case "join":
					if( ts.size() < 3 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a faction name in quotes!", EmbedBuilders.ERR_USAGE)));
					} else if( !allegiance.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You already belong to a faction!", EmbedBuilders.ERR_USAGE)));
					}
					else if( !fdm.has(ts.get(2)) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That faction does not exist!", EmbedBuilders.ERR_USAGE)));
					} else if( fdm.isBanned(ts.get(2), userID) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You have been banned from this faction!", EmbedBuilders.ERR_PERMISSION)));
					}
					else {
						// TODO: add relevant role
						fdm.addMember(ts.get(2), Long.parseLong(ownId));
						fdm.setAllegiance(Long.parseLong(ownId), ts.get(2));
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Faction joined successfully!")));
					}
					break;
				case "leave":
					if( allegiance.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You do not belong to a faction!", EmbedBuilders.ERR_USAGE)));
					} else if( !ownership.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are the faction leader!", EmbedBuilders.ERR_PERMISSION)));
					} else {
						fdm.removeMember(allegiance, Long.parseLong(ownId));
						fdm.setAllegiance(Long.parseLong(ownId), "");
						if( fdm.isOfficer(allegiance, userID) ) {
							fdm.removeOfficer(allegiance, userID);
						}
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Faction left successfully!")));
					}
					break;
				case "color":
					if( ts.size() < 3 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a single integer RGB code!", EmbedBuilders.ERR_USAGE)));
					} else if( !fdm.isOfficer(allegiance, userID) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are not a faction officer!", EmbedBuilders.ERR_PERMISSION)));
					} else if( ts.getNumbers().size() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a single integer RGB code!", EmbedBuilders.ERR_USAGE)));
					} else if( ts.getNumbers().get(0) < 0 || ts.getNumbers().get(0) >= 16777216 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That integer is out of range!", EmbedBuilders.ERR_USAGE)));
					} else {
						fdm.setColor(allegiance, ts.getNumbers().get(0));
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Color changed successfully!")));
						// edit role color if applicable
					}
					break;
				case "description":
					if( ts.size() < 3 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must provide a description in quotes!", EmbedBuilders.ERR_USAGE)));
					} else if( !fdm.isOfficer(allegiance, userID) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are not a faction officer!", EmbedBuilders.ERR_PERMISSION)));
					} else if( ts.quotedOnly().size() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must provide a description in quotes!", EmbedBuilders.ERR_USAGE)));
					} else {
						fdm.setDescription(allegiance, ts.quotedOnly().get(0));
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Description changed successfully!")));
					}
					break;
				case "flag":
					if( ts.size() < 3 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a flag url!", EmbedBuilders.ERR_USAGE)));
					} else if( !fdm.isOfficer(allegiance, userID) ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are not a faction officer!", EmbedBuilders.ERR_PERMISSION)));
					} else {
						fdm.setFlag(allegiance, ts.get(2));
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("Flag changed successfully!")));
					}
					break;
				case "promote":
					if( ownership.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are not the faction leader!", EmbedBuilders.ERR_PERMISSION)));
					} else if( event.getMessage().getUserMentions().count().block() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a mentioned user!", EmbedBuilders.ERR_USAGE)));
					} else {
						User mentioned = event.getMessage().getUserMentions().blockFirst();
						long mentionedID = mentioned.getId().asLong();
						if( !fdm.isMember(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is not in this faction!", EmbedBuilders.ERR_USAGE)));
						} else if( fdm.isOfficer(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is already an officer!", EmbedBuilders.ERR_USAGE)));
						} else if( fdm.getOfficerCount(allegiance) >= MAX_OFFICERS ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("Your faction already has the maximum number of officers!", EmbedBuilders.ERR_PERMISSION)));
						} else {
							fdm.addOfficer(allegiance, mentionedID);
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("User promoted successfully!")));
						}
					}
					break;
				case "demote":
					if( ownership.equals("") ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You are not the faction leader!", EmbedBuilders.ERR_PERMISSION)));
					} else if( event.getMessage().getUserMentions().count().block() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a mentioned user!", EmbedBuilders.ERR_USAGE)));
					} else {
						User mentioned = event.getMessage().getUserMentions().blockFirst();
						long mentionedID = mentioned.getId().asLong();
						if( !fdm.isMember(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is not in your faction!", EmbedBuilders.ERR_USAGE)));
						} else if( !fdm.isOfficer(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is not an officer!", EmbedBuilders.ERR_USAGE)));
						} else if( fdm.isOwner(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is the owner!", EmbedBuilders.ERR_PERMISSION)));
						} else {
							fdm.removeOfficer(allegiance, mentionedID);
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("User demoted successfully!")));
						}
					}
					break;
				case "ban":
					if( !fdm.isOfficer(allegiance, userID) ) {
						
					} else if( event.getMessage().getUserMentions().count().block() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a mentioned user!", EmbedBuilders.ERR_USAGE)));
					} else { 
						User mentioned = event.getMessage().getUserMentions().blockFirst();
						long mentionedID = mentioned.getId().asLong();
						if( fdm.isOfficer(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is an officer!", EmbedBuilders.ERR_PERMISSION)));
						} else if( fdm.isBanned(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is already banned!", EmbedBuilders.ERR_PERMISSION)));
						} else {
							if( fdm.isMember(allegiance, mentionedID) ) {
								fdm.removeMember(allegiance, mentionedID);
								fdm.setAllegiance(userID, "");
							}
							fdm.addBanned(allegiance, mentionedID);
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("User banned successfully!")));
						}
					}
					break;
				case "pardon":
					if( !fdm.isOfficer(allegiance, userID) ) {
						
					} 
					if( event.getMessage().getUserMentions().count().block() == 0 ) {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("You must specify a mentioned user!", EmbedBuilders.ERR_USAGE)));
					} else {
						User mentioned = event.getMessage().getUserMentions().blockFirst();
						long mentionedID = mentioned.getId().asLong();
						if( !fdm.isBanned(allegiance, mentionedID) ) {
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getErrorConstructor("That user is not banned!", EmbedBuilders.ERR_USAGE)));
						} else {
							fdm.removeBanned(allegiance, mentionedID);
							response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getSuccessConstructor("User pardoned successfully!")));
						}
					}
					break;
				default:
					if( event.getMessage().getUserMentions().count().block() > 0 ) {
						User mentioned = event.getMessage().getUserMentions().blockFirst();
						long mentionedID = mentioned.getId().asLong();
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), fdm.getFactionProfileConstructor(fdm.getAllegiance(mentionedID))));
					} else {
						response.addAction(new MessageCreateAction(event.getMessage().getChannel(), EmbedBuilders.getHelpConstructor(user, this)));
					}
					break;
			}
		}
		
		response.toMono().block();
	}

	@Override
	public String getCategory() {
		return "Factions Plug-In";
	}

	@Override
	public String getDescription() {
		return "Allows users to create and join under a common banner.";
	}

	@Override
	public DocumentationPair[] getUsage() {
		return new DocumentationPair[] {
				// Informational
				new DocumentationPair(String.format("%s", invocation), "Informs you which faction you belong to."),
				new DocumentationPair(String.format("%s <@user>", invocation), "Reveals the allegiance of the selected user."),
				new DocumentationPair(String.format("%s list", invocation), "Displays a list of the current factions on your server."),
				new DocumentationPair(String.format("%s profile \"<faction>\"", invocation), "Displays information relevant to the indicated faction."),

				// Management
				new DocumentationPair(String.format("%s create \"<name>\"", invocation), "Creates a faction with the given name."),
				new DocumentationPair(String.format("%s disband", invocation), "Disbands a faction that you have created."),
				new DocumentationPair(String.format("%s join \"<faction>\"", invocation), "Joins the specified faction."),
				new DocumentationPair(String.format("%s leave", invocation), "Leaves your current faction."),
				
				// Modification
				// new DocumentationPair(String.format("%s grow", invocation), ""), // war update
				// new DocumentationPair(String.format("%s promote", invocation), ""),
				// new DocumentationPair(String.format("%s demote", invocation), ""),
				new DocumentationPair(String.format("%s color <rgb>", invocation), "Sets the color of your faction. If your faction has a role, sets the role color as well."),
				new DocumentationPair(String.format("%s description \"<description>\"", invocation), "Sets the description of your faction."),
				new DocumentationPair(String.format("%s flag <image_url>", invocation), "Sets the image url for your faction's flag."),	// empty string by default
				new DocumentationPair(String.format("%s promote <@user>", invocation), "Promotes the given user to an officer in your faction."),
				new DocumentationPair(String.format("%s demote <@user>", invocation), "Demotes the given officer to a user in your faction."),
				new DocumentationPair(String.format("%s ban <@user>", invocation), "Bans the given user from your faction."),
				new DocumentationPair(String.format("%s pardon <@user>", invocation), "Pardons the given user from your faction."),
		};
	}
}
