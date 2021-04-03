package alice.framework.structures;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Faction {
				
	// TODO: FIX LATER
	
	public String name;
	public String description;
	public int color;
	public String flag;
	public long leader;
	public long role;
	public List<Long> officers;
	public List<Long> members;
	public List<Long> banned;
	public long established;
	
	public Faction(String name, long leader, long role, long established) {
		this.name = name;
		this.description = "";
		this.color = 7506394;
		this.flag = "";
		this.leader = leader;
		this.role = role;
		this.officers = new ArrayList<Long>();
		officers.add(leader);
		this.members = new ArrayList<Long>();
		members.add(leader);
		this.banned = new ArrayList<Long>();
		this.established = established;
	}
	
	public Faction(JSONObject jFaction) {
		this(jFaction.getString("name"), jFaction.getLong("leader"), jFaction.getLong("role"), jFaction.getLong("established"));
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
		color = jFaction.getInt("color");
		description = jFaction.getString("description");
		flag = jFaction.getString("flag");
		this.members = members;
		this.officers = officers;
		this.banned = bannedList;
	}
	
	public Faction withDescription(String description) {
		this.description = description;
		return this;
	}
	
	public Faction withColor(int color) {
		this.color = color;
		return this;
	}
	
	public Faction withFlag(String flag) {
		this.flag = flag;
		return this;
	}
	
	public Faction withOfficers(List<Long> officers) {
		this.officers = officers;
		return this;
	}
	
	public Faction withMembers(List<Long> members) {
		this.members = members;
		return this;
	}
	
	public Faction withBanned(List<Long> banned) {
		this.banned = banned;
		return this;
	}
	
	public String getEstablishedString() {
		return LocalDateTime.ofEpochSecond(established/1000, 0, ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
	
	public JSONObject toJSONObject() {
		JSONObject factionObject = new JSONObject();
		
		factionObject.put("name", name);
		factionObject.put("description", description);
		factionObject.put("color", color);
		factionObject.put("flag", flag);
		factionObject.put("leader", leader);
		factionObject.put("role", role);
		factionObject.put("officers", new JSONArray(officers));
		factionObject.put("members", new JSONArray(members));
		factionObject.put("banned", new JSONArray(banned));
		factionObject.put("established", established);
		
		return factionObject;
	}

}
