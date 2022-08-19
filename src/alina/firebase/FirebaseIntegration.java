package alina.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import reactor.core.publisher.Mono;

public class FirebaseIntegration {
		
	protected String dbURL;	
	protected FileInputStream serviceAccount;
	
	protected static String currentURL = "";
	
	protected FirebaseIntegration(String databaseURL, String credentialPath) throws FileNotFoundException {
		dbURL = databaseURL;
		serviceAccount = new FileInputStream("../emerge-3fc95-firebase-adminsdk-ikm88-b2397c34dc.json");
	}
	
	public <T> Mono<T> getData(String databasePath) {
		if( !currentURL.equals(dbURL) ) {
			try {
				initialize();
			} catch (IOException e) {
				return null;
			}
		}
		currentURL = dbURL;
			
		return Mono.create(ms -> {
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					T value = (T) dataSnapshot.getValue(new GenericTypeIndicator<T>() {});
					ms.success(value);
				}

				@Override
				public void onCancelled(DatabaseError error) {}
			});
		});
	}
	
	protected void initialize() throws IOException {
		FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.setDatabaseUrl("https://emerge-3fc95-default-rtdb.firebaseio.com")
				.build();

			FirebaseApp.initializeApp(options);
	}
	
}
