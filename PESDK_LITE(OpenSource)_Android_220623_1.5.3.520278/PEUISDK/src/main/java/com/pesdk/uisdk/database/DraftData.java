package com.pesdk.uisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pesdk.api.IVirtualImageInfo;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.util.DraftFileUtils;
import com.vecore.base.lib.utils.LogUtil;

import java.util.ArrayList;


/**
 * 草稿箱
 */
public class DraftData {

    private DraftData() {

    }

    private final static String TABLE_NAME = "draftInfo";
    private final static String ID = "_id";
    private final static String CREATE_TIME = "_ctime";
    private final static String VER = "_ver";
    private final static String DATA = "_data";

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + ID
                + " INTEGER PRIMARY KEY," + CREATE_TIME + " LONG ," + VER
                + " INTEGER  ," + DATA + " TEXT )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static DraftData instance = null;

    public static DraftData getInstance() {

        if (null == instance) {
            instance = new DraftData();
        }
        return instance;
    }

    private Context mContext;

    public void initilize(Context context) {
        if (null == root) {
            mContext = context.getApplicationContext();
            root = new DatabaseRoot(mContext);
        }
    }

    private static final String TAG = "DraftData";

    /**
     * @param info
     * @return
     */
    public long update(VirtualIImageInfo info) {
        SQLiteDatabase db = root.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(VER, info.getVer());
            cv.put(DATA, info.getBasePath());
            cv.put(CREATE_TIME, info.getCreateTime());
            info.saveToGSONConfig();
            long re = db.update(TABLE_NAME, cv, ID + " = ? ", new String[]{Integer.toString(info.getId())});
            db.close();
            return re;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * 临时草稿实时更新
     *
     * @param info
     * @return
     */
    public int insertOrReplace(VirtualIImageInfo info) {
        if (null == root) {
            return -1;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(VER, info.getVer());
            cv.put(DATA, info.getBasePath());
            cv.put(CREATE_TIME, info.getCreateTime());
            info.saveToGSONConfig();
            long re = 0;
            if (info.getId() == VirtualIImageInfo.ERROR_DB_ID) {
                re = db.insert(TABLE_NAME, null, cv);
            } else {
                re = db.update(TABLE_NAME, cv, ID + " =  ? ", new String[]{info.getId() + ""});
            }
            db.close();
            return (int) re;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * 全部草稿箱视频
     *
     * @param vType 区分临时视频和草稿视频
     * @return
     */
    public ArrayList<IVirtualImageInfo> getAll(int vType) {
        ArrayList<IVirtualImageInfo> list = new ArrayList<>();
        SQLiteDatabase db = root.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, CREATE_TIME + " desc ");
        if (null != c) {
            try {
                while (db.isOpen() && c.moveToNext()) {
                    VirtualIImageInfo infoImp = readItem(c);
                    if (null != infoImp) {
                        if (vType == VirtualIImageInfo.INFO_TYPE_DRAFT && infoImp.getDraftType() == VirtualIImageInfo.INFO_TYPE_NORMAL_1) {
                            //兼容历史：未处理 主动杀进程遗漏的列表 当成已经主动存为草稿的流程处理 (仅是草稿箱列表增加了一些新内容,不影响其他)
                            list.add(infoImp);
                        } else if (infoImp.getDraftType() == vType) {
                            list.add(infoImp);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            c.close();
        }
        return list;
    }

    /**
     * 获取数据库最后一条数据
     */
    public VirtualIImageInfo queryLast() {
        VirtualIImageInfo shortVideoInfoImp = null;
        if (root == null) {
            LogUtil.w(TAG, "queryLast: context is null");
            return null;
        }
        SQLiteDatabase db = root.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, CREATE_TIME + " desc", "0, 1");
        if (null != c) {
            if (c.moveToFirst()) {
                shortVideoInfoImp = readItem(c);
            }
            c.close();
        }
        return shortVideoInfoImp;
    }

    /**
     * 读取单行数据
     *
     * @param c
     * @return
     */
    private VirtualIImageInfo readItem(Cursor c) {
        String data = c.getString(3);
        VirtualIImageInfo imageInfo = DraftFileUtils.toShortInfo(data); //json方式  推荐
        if (null != imageInfo) {
            imageInfo.setId(c.getInt(0));
        }
        return imageInfo;
    }

    private int delete(SQLiteDatabase db, int id) {
        return db.delete(TABLE_NAME, ID + " = ?", new String[]{Integer.toString(id)});
    }

    public VirtualIImageInfo queryOne(int id) {
        VirtualIImageInfo shortVideoInfoImp = null;
        if (root == null) {
            return null;
        }
        SQLiteDatabase db = root.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, ID + " = ?", new String[]{id + ""}, null, null, null, null);
        if (null != c) {
            if (c.moveToFirst()) {
                shortVideoInfoImp = readItem(c);
            }
            c.close();
        }
        return shortVideoInfoImp;

    }

    /**
     * 删除草稿箱视频
     *
     * @param id
     * @return
     */
    public int delete(int id) {
        if (null != root) {
            SQLiteDatabase db = root.getWritableDatabase();
            int re = delete(db, id);
            db.close();
            return re;
        }
        return -1;
    }


    /**
     * 删除所有的
     */
    public void allDelete() {
        if (null != root) {
            SQLiteDatabase db = root.getWritableDatabase();
            db.execSQL("delete from " + TABLE_NAME);
            db.close();
        }
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        if (null != root) {
            root.close();
        }
        instance = null;
    }


}
