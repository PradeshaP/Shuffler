import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shuffler.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "audio_files";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_PLAY_COUNT = "play_count";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_PATH + " TEXT PRIMARY KEY, " +
                COLUMN_PLAY_COUNT + " INTEGER DEFAULT 0)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getAllAudioFiles() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_PLAY_COUNT + " DESC", null);
    }

    public void insertOrUpdateAudioFile(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_NAME + " (" + COLUMN_PATH + ") VALUES (?)", new Object[]{path});
    }

    public void incrementPlayCount(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_PLAY_COUNT + " = " + COLUMN_PLAY_COUNT + " + 1 WHERE " + COLUMN_PATH + " = ?", new Object[]{path});
    }
}