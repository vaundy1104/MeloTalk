-- ==========================
-- 初期化（削除順番注意）
-- ==========================
DELETE FROM playlist_songs;
DELETE FROM playlists;
DELETE FROM comments;
DELETE FROM likes;
DELETE FROM songs;
DELETE FROM albums;
DELETE FROM artists;
DELETE FROM users;

/* rolesテーブル */
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_GENERAL');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

-- ==========================
-- Users
-- ==========================

INSERT INTO users (user_id, email, password, user_name, role_id, enabled) VALUES
(1, 'sakura@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 'さくら', 1, true),
(2, 'hiroshi@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 'ヒロシ', 2, true),
(3, 'jpop@example.com', 'password', 'JPOPファン', 1, false),
(4, 'yumi@example.com', 'password', 'ユミ', 1, false);

-- ==========================
-- Artists
-- ==========================
INSERT INTO artists (artist_id, artist_name) VALUES
('RADWIMPS', 'RADWIMPS'),
('YONEZU', '米津玄師'),
('YOASOBI', 'YOASOBI');

-- ==========================
-- Albums
-- ==========================
INSERT INTO albums (album_id, album_title, artist_id, release_date, album_cover_url) VALUES
(1, 'First Love', 'RADWIMPS', '1999-03-10', 'https://example.com/firstlove.jpg'),
(2, 'BOOTLEG', 'YONEZU', '2017-11-01', 'https://example.com/bootleg.jpg'),
(3, 'THE BOOK', 'YOASOBI', '2021-01-06', 'https://example.com/thebook.jpg');

-- ==========================
-- Songs
-- ==========================
INSERT INTO songs (song_id, song_title, artist_id, album_id, genre, release_date, lyrics, spotify_url, youtube_url) VALUES
('3HCGX2gmPLu6ttkbYvPGTR', 'すずめ', 'RADWIMPS', 1, 'J-Pop', '1998-12-09', '七回目のベルで受話器を取った君...', 'https://open.spotify.com/track/automatic', 'https://youtube.com/watch?v=automatic'),
('s2', 'Lemon', 'YONEZU', 2, 'J-Pop', '2018-02-27', '夢ならばどれほどよかったでしょう...', 'https://open.spotify.com/track/lemon', 'https://youtube.com/watch?v=lemon'),
('s3', '夜に駆ける', 'YOASOBI', 3, 'J-Pop', '2019-12-15', '沈むように溶けてゆくように...', 'https://open.spotify.com/track/yorunikakeru', 'https://youtube.com/watch?v=yorunikakeru');

-- ==========================
-- Comments
-- ==========================
INSERT INTO comments (comment_id, user_id, song_id, parent_comment_id, comment_text) VALUES
(1, 1, '3HCGX2gmPLu6ttkbYvPGTR', NULL, 'この曲めっちゃ最高！イントロから鳥肌立った。'),
(2, 2, '3HCGX2gmPLu6ttkbYvPGTR', NULL, '歌詞がすごく心に響く…涙出た'),
(3, 3, 's3', NULL, '夜に駆けるもいい曲だよね！'),
(4, 4, '3HCGX2gmPLu6ttkbYvPGTR', 1, '私も同じ感想！'),
(5, 1, '3HCGX2gmPLu6ttkbYvPGTR', 2, 'ほんとそれ、共感しかない…'),
(6, 2, '3HCGX2gmPLu6ttkbYvPGTR', 4, 'イントロ最高だった！');

-- ==========================
-- Playlists
-- ==========================
INSERT INTO playlists (playlist_id, user_id, playlist_name, description) VALUES
(1, 1, '私のお気に入りJ-Pop', '気分が上がる曲まとめ'),
(2, 2, '切ない系ソングス', '夜にぴったりのプレイリスト');

-- ==========================
-- Playlist Songs
-- ==========================
INSERT INTO playlist_songs (playlist_song_id, playlist_id, song_id, position) VALUES
('ps1', 1, '3HCGX2gmPLu6ttkbYvPGTR', 1),
('ps2', 1, 's3', 2),
('ps3', 2, 's2', 1);
