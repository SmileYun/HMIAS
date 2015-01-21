package com.cqupt.hmi.model.threaten;

import com.cqupt.hmi.core.ioc.CCAppException;
import com.cqupt.hmi.model.threaten.CanMsgCache.Segment.LEVEL;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class CanMsgCache {
    private ConcurrentHashMap<Integer, HashMap<String, Object>> mCache;

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    static CanMsgCache mInstanceCache;

    private int maxID;

    private CanMsgCache(int capacity) {
        maxID = capacity;

        mCache = new ConcurrentHashMap<Integer, HashMap<String, Object>>();
        for (int i = 1; i <= capacity; i++) {
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("canID", new byte[2]);
            hm.put("data", new byte[8]);
            hm.put("flag", 0);
            mCache.put(i, hm);
        }
    }

    /**
     *
     * 缓存默认大小为 4
     * @return
     * CanMsgCache  实例
     */
    public static CanMsgCache getCacheInstance() {
        if (mInstanceCache == null) {
            synchronized (CanMsgCache.class) {
                if (mInstanceCache == null) {
                    mInstanceCache = new CanMsgCache(4);
                }
            }
        }
        return mInstanceCache;
    }

    /**
     *
     * 指定缓存大小 
     * @param capacity
     * @return
     * CanMsgCache 实例
     */
    public static CanMsgCache getCacheInstance(int capacity) {
        if (mInstanceCache == null) {
            synchronized (CanMsgCache.class) {
                if (mInstanceCache == null) {
                    mInstanceCache = new CanMsgCache(capacity);
                }
            }
        }
        return mInstanceCache;
    }

    /**
     *
     * 根据CanMsg 数据和标志位更新缓存表
     *
     * @param level
     *         		要更新的级别  <p/>
     *            {@code LEVEL.HIGH, LEVEL.MIDDLE, LEVEL.LOW, LEVEL.SAFE} <br>by</br> {@code getLevel()}
     * @param canID
     *            帧ID
     * @param data
     *            数据
     * @param flag
     *            标志位
     * @param
     */
    public void update(int level, int canID, byte[] data, int flag) {
        if (level < 1 || level > maxID) {
            try {
                throw new CCAppException("id out of bound ! pleas check!");

            } catch (CCAppException e) {
                e.printStackTrace();
                return;
            }
        }
        lock.writeLock().lock();
        try {
            HashMap<String, Object> m = mCache.get(level);
            m.put("canID", canID);
            m.put("data", data);
            m.put("flag", flag);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void update(Segment s) {
        if (s.getLevel() < 0 || s.getLevel() > maxID) {
            try {
                throw new CCAppException("id out of bound ! pleas check!");
            } catch (CCAppException e) {
                e.printStackTrace();
                return;
            }
        }
        lock.writeLock().lock();
        try {
            HashMap<String, Object> m = mCache.get(s.getLevel());
            m.put("canID", s.getCanID());
            m.put("data", s.getData());
            m.put("flag", s.getFlag());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     *
     * 根据id 查询 值
     *
     * @param id
     * @return HashMap<String,Object> ["data", "flag"]
     */
    public HashMap<String, Object> query(int id) {
        lock.readLock().lock();
        try {
            if (mCache.contains(id)) {
                return mCache.get(id);
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     *
     * 返回优先级别最高场景
     *
     * @return HashMap<String,Object>, 如果不存在, 则返回null
     */
    public HashMap<String, Object> queryHighLevel() {
        ConcurrentHashMap<Integer, HashMap<String, Object>> _Cache;
        lock.readLock().lock();
        try {
            _Cache = new ConcurrentHashMap<Integer, HashMap<String, Object>>(mCache);
        } finally {
            lock.readLock().unlock();
        }
        for (int i = 1; i <= maxID; i++) {
            HashMap<String, Object> m = _Cache.get(i);

            if ((Integer) m.get("flag") == 1) {

                return m;
            }
        }
        return null;
    }

    /**
     *
     * 返回优先级别最高场景
     *
     * @return Segment, 如果不存在, 则返回null
     */
    public Segment queryHighLevelReturnSg() {
        ConcurrentHashMap<Integer, HashMap<String, Object>> _Cache;
        Segment _s = new Segment();
        lock.readLock().lock();
        try {
            _Cache = new ConcurrentHashMap<Integer, HashMap<String, Object>>(mCache);
        } finally {
            lock.readLock().unlock();
        }
        for (int i = 1; i <= maxID; i++) {
            HashMap<String, Object> m = _Cache.get(i);
            LEVEL l = LEVEL.SAFE;
            if ((Integer) m.get("flag") == 1) {
                switch (i) {
                    case 1:
                        l = LEVEL.HIGH;
                        break;
                    case 2:
                        l = LEVEL.MIDDLE;
                        break;
                    case 3:
                        l = LEVEL.LOW;
                        break;
                    case 4:
                        l = LEVEL.SAFE;
                        break;
                }
                if (l != LEVEL.SAFE)
                    _s.setLevel(l);
                _s.setCanID((Integer) m.get("canID"));
                _s.setData((byte[]) m.get("data"));
                update(i, (Integer) m.get("canID"), (byte[]) m.get("data"), 0);
                return _s;
            }
        }
        return null;
    }

    public void cleanAll() {
        lock.writeLock().lock();
        try {
            mCache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void cleanByID(int id) {
        lock.writeLock().lock();
        try {
            mCache.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 		    +--------------------------+
     * Segment	| id | canId | data | flag |
     * 		    |--------------------------| 
     * 		    | id | HashMap<Str,Object> |
     * 			+--------------------------+
     */
    public static final class Segment {

        private LEVEL level = LEVEL.SAFE;

        private int canID = 0;

        private byte[] data = null;

        private int flag = 0;

        public static enum LEVEL{
            HIGH, MIDDLE, LOW, SAFE;
            public int getLevel(){
                return this.ordinal()+1;
            }
        }

        public int getCanID() {
            return canID;
        }

        public void setCanID(int canID) {
            this.canID = canID;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public int getLevel() {
            return level.ordinal()+1;
        }

        public void setLevel(LEVEL level) {
            this.level = level;
        }
    }

}
