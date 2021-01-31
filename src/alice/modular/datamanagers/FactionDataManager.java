package alice.modular.datamanagers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import alice.framework.structures.AtomicSaveFile;
import alice.framework.structures.Faction;
import alice.framework.structures.QuantifiedPair;
import alice.framework.utilities.EmbedBuilders;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;

public class FactionDataManager {
	
	private AtomicSaveFile guildDataReference;
	private Guild guild;
	
	public FactionDataManager(AtomicSaveFile guildData, Guild guild) {
		this.guildDataReference = guildData;
		if( !guildData.has("faction_map") ) {
			guildData.put("faction_map", new JSONObject());
		}
		this.guild = guild;
	}
	
	public Faction getFaction(String factionName) {
		if( !guildDataReference.getJSONObject("faction_map").has(factionName.toLowerCase()) ) {
			return null;
		} else {
			JSONObject jFaction = guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase());
			List<Long> members = new ArrayList<Long>();
			for( Object member : jFaction.getJSONArray("members") ) {
				members.add((long) member);
			}
			List<Long> officers = new ArrayList<Long>();
			for( Object officer : jFaction.getJSONArray("officers") ) {
				officers.add((long) officer);
			}
			List<Long> bannedList = new ArrayList<Long>();
			for( Object banned : jFaction.getJSONArray("banned") ) {
				bannedList.add((long) banned);
			}
			return new Faction (jFaction.getString("name"), jFaction.getLong("leader"), jFaction.getLong("established"))
								.withColor(jFaction.getInt("color"))
								.withDescription(jFaction.getString("description"))
								.withFlag(jFaction.getString("flag"))
								.withMembers(members)
								.withOfficers(officers)
								.withBanned(bannedList);
		}
	}
	
	public void addFaction(String factionName, Faction faction) {
		guildDataReference.modifyJSONObject("faction_map", j -> j.put(factionName.toLowerCase(), faction.toJSONObject()));
	}
		
	public void removeFaction(String factionName) {
		guildDataReference.modifyJSONObject("faction_map", j -> j.remove(factionName.toLowerCase()));
	}
	
	public List<QuantifiedPair<String>> getSortedFactions() {
		List<QuantifiedPair<String>> entries = new ArrayList<QuantifiedPair<String>>();
		for( String key : new HashSet<String>(guildDataReference.getJSONObject("faction_map").keySet()) ) {
			entries.add(new QuantifiedPair<String>(guildDataReference.getJSONObject("faction_map").getJSONObject(key).getString("name"), guildDataReference.getJSONObject("faction_map").getJSONObject(key.toLowerCase()).getJSONArray("members").length()));
		}
		entries.sort( (a, b) -> a.compareTo(b) );
		return entries;
	}
	
	public Consumer<EmbedCreateSpec> getFactionProfileConstructor(String factionName) {
		Faction faction = getFaction(factionName.toLowerCase());
		int rank = 0;
		User leader = guild.getMemberById(Snowflake.of(faction.leader)).block();
		User[] officers = new User[faction.officers.size()];
		for( int f=0; f<officers.length; f++ ) {
			officers[f] = guild.getMemberById(Snowflake.of(faction.officers.get(f))).block();
		}
		List<QuantifiedPair<String>> entries = getSortedFactions();
		for( int f=0; f<entries.size(); f++ ) {
			if( entries.get(f).key.equals(faction.name) ) {
				rank = f+1;
				break;
			}
		}
		return EmbedBuilders.getFactionProfileConstructor(faction, leader, officers, rank);
	}
	
	public boolean has(String factionName) {
		return guildDataReference.getJSONObject("faction_map").has(factionName.toLowerCase());
	}
	
	public void setDescription(String factionName, String description) {
		guildDataReference.modifyJSONObject("faction_map", j -> j.put(factionName.toLowerCase(), j.getJSONObject(factionName.toLowerCase()).put("description", description)));
	}
	
	public void setColor(String factionName, int color) {
		guildDataReference.modifyJSONObject("faction_map", j -> j.put(factionName.toLowerCase(), j.getJSONObject(factionName.toLowerCase()).put("color", color)));
	}
	
	public void setFlag(String factionName, String flag) {
		guildDataReference.modifyJSONObject("faction_map", j -> j.put(factionName.toLowerCase(), j.getJSONObject(factionName.toLowerCase()).put("flag", flag)));
	}
	
	private void addOfficerConsumer(JSONObject factionMap, String factionName, long officer) {
		JSONArray officers = factionMap.getJSONObject(factionName.toLowerCase()).getJSONArray("officers");
		officers.put(officer);
		factionMap.getJSONObject(factionName.toLowerCase()).put("officers", officers);
	}
	
	public void addOfficer(String factionName, long officer) {		
		guildDataReference.modifyJSONObject("faction_map", j -> addOfficerConsumer(j, factionName, officer));
	}
	
	private void removeOfficerConsumer(JSONObject factionMap, String factionName, long officer) {
		int index = -1;
		JSONArray officers = factionMap.getJSONObject(factionName.toLowerCase()).getJSONArray("officers");
		for( int f=0; f<officers.length(); f++ ) {
			if( officers.getLong(f) == officer ) {
				index = f;
			}
		}
		officers.remove(index);
		factionMap.getJSONObject(factionName.toLowerCase()).put("officers", officers);
	}
	
	public void removeOfficer(String factionName, long officer) {
		guildDataReference.modifyJSONObject("faction_map", j -> removeOfficerConsumer(j, factionName, officer));
	}
	
	private void addMemberConsumer(JSONObject factionMap, String factionName, long member) {
		JSONArray members = factionMap.getJSONObject(factionName.toLowerCase()).getJSONArray("members");
		members.put(member);
		factionMap.getJSONObject(factionName.toLowerCase()).put("members", members);
	}
	
	public void addMember(String factionName, long member) {		
		guildDataReference.modifyJSONObject("faction_map", j -> addMemberConsumer(j, factionName, member));
	}
	
	private void removeMemberConsumer(JSONObject factionMap, String factionName, long member) {
		int index = -1;
		JSONArray members = factionMap.getJSONObject(factionName.toLowerCase()).getJSONArray("members");
		for( int f=0; f<members.length(); f++ ) {
			if( members.getLong(f) == member ) {
				index = f;
			}
		}
		members.remove(index);
		factionMap.getJSONObject(factionName.toLowerCase()).put("members", members);
	}
	
	public void removeMember(String factionName, long member) {
		guildDataReference.modifyJSONObject("faction_map", j -> removeMemberConsumer(j, factionName, member));
	}
	
	private void addBannedConsumer(JSONObject factionMap, String factionName, long banned) {
		JSONArray bannedList = factionMap.getJSONObject(factionName.toLowerCase()).getJSONArray("banned");
		bannedList.put(banned);
		factionMap.getJSONObject(factionName.toLowerCase()).put("banned", bannedList);
	}
	
	public void addBanned(String factionName, long banned) {		
		guildDataReference.modifyJSONObject("faction_map", j -> addBannedConsumer(j, factionName, banned));
	}
	
	private void removeBannedConsumer(JSONObject factionMap, String factionName, long banned) {
		int index = -1;
		JSONArray bannedList = factionMap.getJSONObject(factionName.toLowerCase()).getJSONArray("banned");
		for( int f=0; f<bannedList.length(); f++ ) {
			if( bannedList.getLong(f) == banned ) {
				index = f;
			}
		}
		bannedList.remove(index);
		factionMap.getJSONObject(factionName.toLowerCase()).put("banned", bannedList);
	}
	
	public void removeBanned(String factionName, long banned) {
		guildDataReference.modifyJSONObject("faction_map", j -> removeBannedConsumer(j, factionName, banned));
	}
	
	public String getAllegiance(long user) {
		String key = String.format("%d_faction_allegiance", user);
		if( !guildDataReference.has(key) ) {
			setAllegiance(user, "");
		}
		return guildDataReference.getString(key);
	}
	
	public void setAllegiance(long user, String factionName) {
		guildDataReference.put(String.format("%d_faction_allegiance", user), factionName.toLowerCase());
	}
	
	public String getOwnership(long user) {
		String key = String.format("%d_faction_ownership", user);
		if( !guildDataReference.has(key) ) {
			setOwnership(user, "");
		}
		return guildDataReference.getString(key);
	}
	
	public void setOwnership(long user, String factionName) {
		guildDataReference.put(String.format("%d_faction_ownership", user), factionName.toLowerCase());
	}
	
	public boolean isOwner(String factionName, long user) {
		return guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase()).getLong("leader") == user;
	}
	
	public boolean isOfficer( String factionName, long user ) {
		JSONArray officers = guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase()).getJSONArray("officers");
		for( Object officer : officers ) {
			if( (Long) officer == user ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isMember( String factionName, long user ) {
		JSONArray members = guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase()).getJSONArray("members");
		for( Object member : members ) {
			if( (Long) member == user ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isBanned( String factionName, long user ) {
		JSONArray bannedList = guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase()).getJSONArray("banned");
		for( Object banned : bannedList ) {
			if( (Long) banned == user ) {
				return true;
			}
		}
		return false;
	}
	
	public int getOfficerCount( String factionName ) {
		return guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase()).getJSONArray("officers").length();
	}
	
	public int getMemberCount( String factionName ) {
		return guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase()).getJSONArray("members").length();
	}
	
	public int getBannedCount( String factionName ) {
		return guildDataReference.getJSONObject("faction_map").getJSONObject(factionName.toLowerCase()).getJSONArray("banned").length();
	}
}
