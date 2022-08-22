package alina.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FirebaseIntegration {
		
	protected String dbURL;	
	protected FileInputStream serviceAccount;
	
	protected static String currentURL = "";
	protected static Semaphore lock = new Semaphore(1);
	
	public FirebaseIntegration(String databaseURL, String credentialPath) throws FileNotFoundException {
		dbURL = databaseURL;
		serviceAccount = new FileInputStream(credentialPath);
	}
	
	public <T> Mono<T> getData(String databasePath, Class<T> type) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			return null;
		}
		
		return Mono.create(ms -> {
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			ref.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					ms.success(dataSnapshot.getValue(type));
				}

				@Override
				public void onCancelled(DatabaseError error) {}
			});
			lock.release();
		});
	}
	
	public <T> Flux<T> subscribeData(String databasePath, Class<T> type) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			return null;
		}
		
		return Flux.create(fs -> {
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			ref.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					fs.next(dataSnapshot.getValue(type));
				}

				@Override
				public void onCancelled(DatabaseError error) {}
			});
			lock.release();
		});
	}
	
	public <T> Mono<T> getData(String databasePath, GenericTypeIndicator<T> type) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			return null;
		}
		
		return Mono.create(ms -> {
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			ref.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					ms.success(dataSnapshot.getValue(type));
				}

				@Override
				public void onCancelled(DatabaseError error) {}
			});
			lock.release();
		});
	}
	
	public <T> Flux<T> subscribeData(String databasePath, GenericTypeIndicator<T> type) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			return null;
		}
		
		return Flux.create(fs -> {
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			ref.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					fs.next(dataSnapshot.getValue(type));
				}

				@Override
				public void onCancelled(DatabaseError error) {}
			});
			lock.release();
		});
	}
	
	protected boolean initialize() throws IOException {
		if( !currentURL.equals(dbURL) ) {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.setDatabaseUrl(dbURL)
				.build();

			FirebaseApp.initializeApp(options);
		}
		currentURL = dbURL;
		return false;
	}
	
}
