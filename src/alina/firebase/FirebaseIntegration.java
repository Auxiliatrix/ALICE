package alina.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

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
	
	protected static Map<String, Object> cacheMap = new HashMap<String, Object>();
	protected static Map<String, ValueEventListener> listenerMap = new HashMap<String, ValueEventListener>();
	
	public FirebaseIntegration(String databaseURL, String credentialPath) throws FileNotFoundException {
		dbURL = databaseURL;
		serviceAccount = new FileInputStream(credentialPath);
	}
	
	public <T> Mono<T> getData(String databasePath, Class<T> type) throws InterruptedException {
		return getData(databasePath, type, false);
	}
	
	public <T> Mono<T> getData(String databasePath, GenericTypeIndicator<T> type) throws InterruptedException {
		return getData(databasePath, type, false);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Mono<T> getData(String databasePath, Class<T> type, boolean cache) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			lock.release();
			return null;
		}
		
		if( cache ) {
			cache(databasePath, type);
		}
		
		return Mono.create(ms -> {
			if( cacheMap.containsKey(databasePath) ) {
				ms.success((T) cacheMap.get(databasePath));
			} else {
				DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
				ref.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						ms.success(dataSnapshot.getValue(type));
						ref.removeEventListener(this);
					}
	
					@Override
					public void onCancelled(DatabaseError error) {}
				});
			}
			lock.release();
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> Mono<T> getData(String databasePath, GenericTypeIndicator<T> type, boolean cache) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			lock.release();
			return null;
		}
		
		if( cache ) {
			cache(databasePath, type);
		}
		
		return Mono.create(ms -> {
			if( cacheMap.containsKey(databasePath) ) {
				ms.success((T) cacheMap.get(databasePath));
			} else {
				DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
				ref.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						ms.success(dataSnapshot.getValue(type));
						ref.removeEventListener(this);
					}
	
					@Override
					public void onCancelled(DatabaseError error) {}
				});
			}
			lock.release();
		});
	}
	
	public <T> Flux<T> subscribeData(String databasePath, Class<T> type) {
		return Flux.create(fs -> {
			try {
				addChangeListener(databasePath, type, t -> fs.next((T) t));
			} catch (InterruptedException e) {
				fs.error(e);
			}
		});
	}
	
	public <T> Flux<T> subscribeData(String databasePath, GenericTypeIndicator<T> type) {
		return Flux.create(fs -> {
			try {
				addChangeListener(databasePath, type, t -> fs.next((T) t));
			} catch (InterruptedException e) {
				fs.error(e);
			}
			lock.release();
		});
	}
	
	public void removeListener(String databasePath, ValueEventListener listener) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			lock.release();
		}
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
		ref.removeEventListener(listener);
		
		lock.release();
	}
	
	public <T> ValueEventListener addChangeListener(String databasePath, Class<T> type, Consumer<T> consumer) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			lock.release();
		}
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
		ValueEventListener listener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				T value = dataSnapshot.getValue(type);
				consumer.accept(value);
			}

			@Override
			public void onCancelled(DatabaseError error) {}
		};
		ref.addValueEventListener(listener);
		lock.release();
		return listener;
	}
	
	public <T> ValueEventListener addChangeListener(String databasePath, GenericTypeIndicator<T> type, Consumer<T> consumer) throws InterruptedException {
		lock.acquire();
		try {
			initialize();
		} catch(IOException e) {
			lock.release();
		}
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
		ValueEventListener listener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				T value = dataSnapshot.getValue(type);
				consumer.accept(value);
			}

			@Override
			public void onCancelled(DatabaseError error) {}
		};
		ref.addValueEventListener(listener);
		lock.release();
		return listener;
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
	
	public boolean uncache(String databasePath) throws InterruptedException {
		lock.acquire();

		try {
			initialize();
		} catch(IOException e) {
			lock.release();
			return false;
		}
		
		if( cacheMap.containsKey(databasePath) && listenerMap.containsKey(databasePath) ) {
			cacheMap.remove(databasePath);
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			ref.removeEventListener(listenerMap.remove(databasePath));
			return true;
		}
		
		lock.release();
		return false;
	}
	
	protected <T> boolean cache(String databasePath, Class<T> type) {
		if( !cacheMap.containsKey(databasePath)) {
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			ValueEventListener listener = new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					T data = dataSnapshot.getValue(type);
					cacheMap.put(databasePath, data);
				}

				@Override
				public void onCancelled(DatabaseError error) {}
			};
			ref.addValueEventListener(listener);
			listenerMap.put(databasePath, listener);
			return true;
		}
		return false;
	}
	
	protected <T> boolean cache(String databasePath, GenericTypeIndicator<T> type) {
		if( !cacheMap.containsKey(databasePath)) {
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databasePath);
			ValueEventListener listener = new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					T data = dataSnapshot.getValue(type);
					cacheMap.put(databasePath, data);
				}

				@Override
				public void onCancelled(DatabaseError error) {}
			};
			ref.addValueEventListener(listener);
			listenerMap.put(databasePath, listener);
			return true;
		}
		return false;
	}
	
}
