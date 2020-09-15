package alice.framework.structures;

import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;

import alice.framework.utilities.FileIO;

public class AtomicSaveFile {
	
	private String saveFileName;
	private AtomicReference<JSONObject> saveData;
	 
	public AtomicSaveFile(String saveFileName) {
		this.saveFileName = saveFileName;
		saveData = new AtomicReference<JSONObject>(new JSONObject(FileIO.readFromFile(saveFileName, "{}")));
	}
	 
	public JSONObject put(String key, Object value) {
		JSONObject updated = saveData.updateAndGet( (sd) -> { sd.put(key, value); return sd; } );
		FileIO.writeToFile(saveFileName, updated.toString());
		return updated;
	}
	
	public Object get(String key) {
		return saveData.get().get(key);
	}
}
