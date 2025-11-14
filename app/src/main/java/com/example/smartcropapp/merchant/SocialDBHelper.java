package com.example.smartcropapp.merchant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartcropapp.merchant.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Social DB helper: users, posts, likes, comments, followers, messages
 */
public class SocialDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "social_app.db";
    private static final int DB_VER = 1;

    public SocialDBHelper(Context ctx){ super(ctx, DB_NAME, null, DB_VER); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, email TEXT, location TEXT, crops TEXT, bio TEXT, photo_uri TEXT)");
        db.execSQL("CREATE TABLE posts(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, image_uri TEXT, caption TEXT, expected_price TEXT, created_at INTEGER)");
        db.execSQL("CREATE TABLE likes(id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER)");
        db.execSQL("CREATE TABLE comments(id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER, comment TEXT, created_at INTEGER)");
        db.execSQL("CREATE TABLE followers(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, follower_id INTEGER)");
        db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT, from_id INTEGER, to_id INTEGER, message TEXT, created_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS posts");
        db.execSQL("DROP TABLE IF EXISTS likes");
        db.execSQL("DROP TABLE IF EXISTS comments");
        db.execSQL("DROP TABLE IF EXISTS followers");
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }

    // ----- Users -----
    public long insertUser(String name, String phone, String email, String location, String crops, String bio, String photoUri){
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("phone", phone); v.put("email", email);
        v.put("location", location); v.put("crops", crops); v.put("bio", bio); v.put("photo_uri", photoUri);
        return getWritableDatabase().insert("users", null, v);
    }
    public Cursor getUserCursor(long id){ return getReadableDatabase().rawQuery("SELECT * FROM users WHERE id=?", new String[]{String.valueOf(id)}); }
    public User getUserById(long id){
        Cursor c = getUserCursor(id);
        User u = null;
        if(c.moveToFirst()){
            u = new User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7));
        }
        c.close(); return u;
    }
    public List<User> getAllUsers(){
        List<User> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM users", null);
        while(c.moveToNext()){
            out.add(new User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7)));
        }
        c.close(); return out;
    }
    public int updateUser(long id, String name, String phone, String email, String location, String crops, String bio, String photoUri){
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("phone", phone); v.put("email", email);
        v.put("location", location); v.put("crops", crops); v.put("bio", bio); v.put("photo_uri", photoUri);
        return getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)});
    }

    // ----- Posts -----
    public long insertPost(long userId, String imageUri, String caption, String price, long ts){
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("image_uri", imageUri); v.put("caption", caption); v.put("expected_price", price); v.put("created_at", ts);
        return getWritableDatabase().insert("posts", null, v);
    }
    public List<Post> getFeed(){
        List<Post> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT p.id, p.user_id, p.image_uri, p.caption, p.expected_price, p.created_at, u.name, u.photo_uri, u.location, u.phone FROM posts p LEFT JOIN users u ON p.user_id=u.id ORDER BY p.created_at DESC", null);
        while(c.moveToNext()){
            Post p = new Post(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getString(4), c.getLong(5));
            p.userName = c.getString(6); p.userPhoto = c.getString(7); p.userLocation = c.getString(8); p.userPhone = c.getString(9);
            out.add(p);
        }
        c.close(); return out;
    }
    public List<Post> getPostsByUser(long userId){
        List<Post> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT p.id, p.user_id, p.image_uri, p.caption, p.expected_price, p.created_at, u.name, u.photo_uri FROM posts p LEFT JOIN users u ON p.user_id=u.id WHERE p.user_id=? ORDER BY p.created_at DESC", new String[]{String.valueOf(userId)});
        while(c.moveToNext()){
            Post p = new Post(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getString(4), c.getLong(5));
            p.userName = c.getString(6); p.userPhoto = c.getString(7);
            out.add(p);
        }
        c.close(); return out;
    }

    // ----- Likes -----
    public void likePost(long postId, long userId){
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM likes WHERE post_id=? AND user_id=?", new String[]{String.valueOf(postId), String.valueOf(userId)});
        if(c.moveToFirst()){ c.close(); return; }
        c.close();
        ContentValues v = new ContentValues(); v.put("post_id", postId); v.put("user_id", userId);
        getWritableDatabase().insert("likes", null, v);
    }
    public void unlikePost(long postId, long userId){
        getWritableDatabase().delete("likes", "post_id=? AND user_id=?", new String[]{String.valueOf(postId), String.valueOf(userId)});
    }
    public int getLikeCount(long postId){
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM likes WHERE post_id=?", new String[]{String.valueOf(postId)});
        int v = 0; if(c.moveToFirst()) v = c.getInt(0); c.close(); return v;
    }
    public boolean isLikedBy(long postId, long userId){
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM likes WHERE post_id=? AND user_id=?", new String[]{String.valueOf(postId), String.valueOf(userId)});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }
    public List<User> getLikesUsers(long postId){
        List<User> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT u.id, u.name, u.phone, u.photo_uri FROM likes l JOIN users u ON l.user_id=u.id WHERE l.post_id=?", new String[]{String.valueOf(postId)});
        while(c.moveToNext()){
            out.add(new User(c.getLong(0), c.getString(1), c.getString(2), null, null, null, null, c.getString(3)));
        }
        c.close(); return out;
    }

    // ----- Comments -----



    /**
     * Insert a comment for postId by userId
     */
    public long addComment(long postId, long userId, String text, long createdAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("post_id", postId);
        cv.put("user_id", userId);
        cv.put("comment", text);
        cv.put("created_at", createdAt);
        return db.insert("comments", null, cv);
    }

    // Get all comments for a post
    public List<Comment> getComments(long postId) {
        List<Comment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT c.id, c.comment, c.user_id, u.name, u.photo_uri " +
                "FROM comments c LEFT JOIN users u ON c.user_id=u.id " +
                "WHERE c.post_id=? ORDER BY c.created_at ASC", new String[]{String.valueOf(postId)});
        while (c.moveToNext()) {
            list.add(new Comment(
                    c.getLong(0),          // comment id
                    c.getString(1),        // text
                    c.getLong(2),          // userId
                    c.getString(3),        // userName
                    c.getString(4)         // userPhoto
            ));
        }
        c.close();
        return list;
    }
    // ----- Followers -----
    public void follow(long followeeId, long followerId){
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM followers WHERE user_id=? AND follower_id=?", new String[]{String.valueOf(followeeId), String.valueOf(followerId)});
        if(c.moveToFirst()){ c.close(); return; }
        c.close();
        ContentValues v = new ContentValues(); v.put("user_id", followeeId); v.put("follower_id", followerId);
        getWritableDatabase().insert("followers", null, v);
    }
    public void unfollow(long followeeId, long followerId){
        getWritableDatabase().delete("followers", "user_id=? AND follower_id=?", new String[]{String.valueOf(followeeId), String.valueOf(followerId)});
    }
    public boolean isFollowing(long followeeId, long followerId){
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM followers WHERE user_id=? AND follower_id=?", new String[]{String.valueOf(followeeId), String.valueOf(followerId)});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }
    public List<User> getFollowers(long userId){
        List<User> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT u.id, u.name, u.phone, u.photo_uri FROM followers f JOIN users u ON f.follower_id=u.id WHERE f.user_id=?", new String[]{String.valueOf(userId)});
        while(c.moveToNext()){
            out.add(new User(c.getLong(0), c.getString(1), c.getString(2), null, null, null, null, c.getString(3)));
        }
        c.close(); return out;
    }

    // ----- Messages -----
    public long sendMessage(long fromId, long toId, String message, long ts){
        ContentValues v = new ContentValues(); v.put("from_id", fromId); v.put("to_id", toId); v.put("message", message); v.put("created_at", ts);
        return getWritableDatabase().insert("messages", null, v);
    }
    // simple fetch
    public List<String> getMessagesBetween(long a, long b){
        List<String> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT m.message, u.name FROM messages m LEFT JOIN users u ON m.from_id=u.id WHERE (m.from_id=? AND m.to_id=?) OR (m.from_id=? AND m.to_id=?) ORDER BY m.created_at ASC", new String[]{String.valueOf(a),String.valueOf(b),String.valueOf(b),String.valueOf(a)});
        while(c.moveToNext()){
            out.add(c.getString(1) + ": " + c.getString(0));
        }
        c.close(); return out;
    }
}
