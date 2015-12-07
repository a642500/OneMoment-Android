package co.yishun.onemoment.app.data.compat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import co.yishun.onemoment.app.data.model.Moment;

/**
 * SQLite opener helper for CompatMoment.
 * <p>
 * Created by Carlos on 3/9/15.
 */
public class MomentDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = "MomentDatabaseHelper";
    private final Context mContext;

    public MomentDatabaseHelper(Context context) {
        super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        // do nothing to use realm
    }

    /**
     * own default value: LOCAL 's getPrefix(Context)
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Dao<Moment, Integer> dao = getDao(Moment.class);
            Log.i(TAG, "upgrade database from " + oldVersion + " to " + newVersion);
            switch (oldVersion) {
                case 1:
                    String renameTable = "ALTER TABLE `" + Contract.DATABASE_NAME + "` RENAME TO " + Contract.Moment.TABLE_NAME + " ;";//because I made wrong with naming table with database name.
                    Log.i(TAG, "rename table: " + renameTable);
                    dao.executeRaw(renameTable);
                    String addColumn = "ALTER TABLE " + Contract.Moment.TABLE_NAME + " ADD COLUMN owner VARCHAR DEFAULT 'LOC' ;";
                    Log.i(TAG, "add column: " + addColumn);
                    dao.executeRaw(addColumn);
//                case 2:
                    // switch to realm
//                    List<CompatMoment> allCompatMoments = dao.queryForAll();
//                    if (allCompatMoments.size() != 0) {
//                        Realm realm = Realm.getInstance(mContext);
//                        StreamSupport.stream(allCompatMoments).forEach(compatMoment -> {
//                            Moment moment = Moment.fromMomentProvider(compatMoment);
//                            realm.executeTransaction(realm1 -> realm1.copyToRealm(moment));
//                        });
//                    }
//                    TableUtils.dropTable(connectionSource, CompatMoment.class, true);
                    break;
                default:
                    throw new IllegalStateException(
                            "onUpgrade() with unknown oldVersion " + oldVersion);


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
