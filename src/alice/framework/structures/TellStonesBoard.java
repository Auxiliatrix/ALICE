package alice.framework.structures;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

public class TellStonesBoard {
	
	public enum Piece {
		CROWN,
		SHIELD,
		SWORD,
		FLAG,
		KNIGHT,
		HAMMER,
		SCALE,
		EMPTY
	}
	
	public static final Piece CROWN = Piece.CROWN;
	public static final Piece SHIELD = Piece.SHIELD;
	public static final Piece SWORD = Piece.SWORD;
	public static final Piece FLAG = Piece.FLAG;
	public static final Piece KNIGHT = Piece.KNIGHT;
	public static final Piece HAMMER = Piece.HAMMER;
	public static final Piece SCALE = Piece.SCALE;
	public static final Piece EMPTY = Piece.EMPTY;
	
	public Set<Piece> pool;
	public Piece[] line;
	public boolean[] hidden;
	
	public String playerA;
	public int playerAScore;
	public String playerB;
	public int playerBScore;
	public String turn;
	
	public TellStonesBoard(String playerA, String playerB) {
		pool = Set.of(Piece.values());
		line = new Piece[] { EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY };
		hidden = new boolean[] {false, false, false, false, false, false, false};
		this.playerA = playerA;
		this.playerB = playerB;
		this.playerAScore = 0;
		this.playerBScore = 0;
		turn = playerA;
	}
	
	public TellStonesBoard(String savedLine, String savedHidden, String playerA, String playerB, String turn, int playerAScore, int playerBScore) {
		pool = new HashSet<Piece>();
		line = new Piece[7];
		String[] savedLineTokens = savedLine.split(",");
		for( int f=0; f<7; f++ ) {
			String savedPiece = savedLineTokens[f];
			for( Piece piece : Piece.values() ) {
				if( piece.name().equals(savedPiece) ) {
					pool.remove(piece);
					line[f] = piece;
					break;
				}
			}
		}
		hidden = new boolean[7];
		for( int f=0; f<7; f++ ) {
			hidden[f] = savedHidden.charAt(f) == 't';
		}
		this.playerA = playerA;
		this.playerB = playerB;
		this.turn = turn;
		this.playerAScore = playerAScore;
		this.playerBScore = playerBScore;
	}
	
	public TellStonesBoard(JSONObject board) {
		this( board.getString("saved_line"), board.getString("saved_hidden"), board.getString("player_a"), board.getString("player_b"), board.getString("turn"), board.getInt("player_a_score"), board.getInt("player_b_score"));
	}
	
	public JSONObject toJSONObject() {
		JSONObject jo = new JSONObject();
		
		StringBuilder savedLine = new StringBuilder();
		for( Piece piece : line ) {
			savedLine.append(String.format("%s,", piece.name()));
		}
		jo.put("saved_line", savedLine.toString());
		
		StringBuilder savedHidden = new StringBuilder();
		for( boolean h : hidden ) {
			savedHidden.append(h ? "t" : "f");
		}
		jo.put("saved_hidden", savedHidden.toString());
		
		jo.put("player_a", playerA);
		jo.put("player_b", playerB);
		jo.put("turn", turn);
		jo.put("player_a_score", playerAScore);
		jo.put("player_b_score", playerBScore);
		return jo;
	}
	
	public boolean place(Piece piece, int place) {
		if( place < 0 || place >= 7 || pool.contains(piece) || line[place] != EMPTY ) {
			return false;
		}
		for( Piece poolPiece : pool ) {
			if( poolPiece == piece ) {
				return false;
			}
		}
		
		line[place] = piece;
		pool.remove(piece);
		return true;
	}
	
	public boolean swap(int placeA, int placeB) {
		if( placeA < 0 || placeA >= 7 || line[placeA] == EMPTY || placeB < 0 || placeB >= 7 || line[placeB] == EMPTY ) {
			return false;
		}
		
		Piece tempPiece = line[placeA];
		line[placeA] = line[placeB];
		line[placeB] = tempPiece;
		
		boolean tempBoolean = hidden[placeA];
		hidden[placeA] = hidden[placeB];
		hidden[placeB] = tempBoolean;
		return true;
	}
	
	public boolean hide(int place) {
		if( place < 0 || place >= 7 || line[place] == EMPTY ) {
			return false;
		}
		
		hidden[place] = true;
		return true;
	}
	
	public Piece getPiece(String name) {
		for( Piece piece : Piece.values() ) {
			if( piece.name().equalsIgnoreCase(name) ) {
				return piece;
			}
		}
		return null;
	}
	
	public void switchTurn() {
		turn = turn == playerA ? playerB : playerA;
	}
}
